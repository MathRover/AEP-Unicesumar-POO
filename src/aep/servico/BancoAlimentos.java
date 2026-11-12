package aep.servico;

import aep.dominio.*;
import aep.persistencia.Repositorio;
import aep.persistencia.RepositorioEmMemoria;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BancoAlimentos {
    private final Clock relogio;
    private final Repositorio<Integer, Doador> doadores;
    private final Repositorio<String, Lote> lotes = new RepositorioEmMemoria<>();
    private final Repositorio<Integer, Doacao> doacoes = new RepositorioEmMemoria<>();
    private final Repositorio<Integer, Entrega> entregas = new RepositorioEmMemoria<>();
    private final Repositorio<Integer, Coleta> coletasPorDoacao = new RepositorioEmMemoria<>();
    private int proximoDoador = 1;

    public BancoAlimentos(Clock relogio, Repositorio<Integer, Doador> doadores) {
        this.relogio = relogio;
        this.doadores = doadores;
        proximoDoador = doadores.listar().stream().mapToInt(Doador::id).max().orElse(0) + 1;
    }

    public LocalDate hoje() { return LocalDate.now(relogio); }
    public List<Doador> listarDoadores() { return doadores.listar(); }
    public List<Lote> listarLotes() { return lotes.listar(); }
    public List<Entrega> listarEntregas() { return entregas.listar(); }

    public Doador consultarDoador(int id) {
        return doadores.buscar(id).orElseThrow(() -> new RegraNegocioException("Doador não encontrado."));
    }

    public Doador cadastrarDoador(String nome, Doador.Tipo tipo, String documento, String email, String telefone) {
        Doador doador = new Doador(proximoDoador, nome, tipo, documento, email, telefone);
        salvarDoador(doador);
        proximoDoador++;
        return doador;
    }

    public Doador atualizarDoador(int id, String nome, Doador.Tipo tipo, String documento, String email, String telefone) {
        consultarDoador(id);
        Doador doador = new Doador(id, nome, tipo, documento, email, telefone);
        salvarDoador(doador);
        return doador;
    }

    private void salvarDoador(Doador doador) {
        boolean duplicado = doadores.listar().stream().anyMatch(outro ->
            outro.id() != doador.id() && outro.documento().equals(doador.documento()));
        if (duplicado) throw new RegraNegocioException("CPF ou CNPJ já cadastrado.");
        doadores.salvar(doador.id(), doador);
    }

    public void excluirDoador(int id) {
        consultarDoador(id);
        if (doacoes.listar().stream().anyMatch(doacao -> doacao.idDoador() == id)) {
            throw new RegraNegocioException("Doador com doações registradas.");
        }
        doadores.remover(id);
    }

    public Doacao registrarDoacao(int idDoador, LocalDate recebimento, Lote lote) {
        return registrarDoacao(idDoador, recebimento, List.of(lote));
    }

    public Doacao registrarDoacao(int idDoador, LocalDate recebimento, List<Lote> recebidos) {
        consultarDoador(idDoador);
        if (recebimento == null || recebimento.isAfter(hoje())) {
            throw new RegraNegocioException("Informe uma data de recebimento até hoje.");
        }
        if (recebidos == null || recebidos.isEmpty()) {
            throw new RegraNegocioException("Informe pelo menos um lote.");
        }
        Map<String, Lote> novos = new LinkedHashMap<>();
        for (Lote lote : recebidos) {
            if (lote.validade().isBefore(recebimento)) {
                throw new RegraNegocioException("A validade do lote não pode ser anterior ao recebimento.");
            }
            if (lote.saldo().compareTo(lote.quantidade()) != 0) {
                throw new RegraNegocioException("O recebimento exige um lote com saldo inicial.");
            }
            if (lotes.buscar(lote.codigo()).isPresent() || novos.putIfAbsent(lote.codigo(), lote) != null) {
                throw new RegraNegocioException("Código de lote já cadastrado: " + lote.codigo());
            }
        }
        Doacao doacao = new Doacao(doacoes.listar().size() + 1, idDoador, recebimento, List.copyOf(novos.keySet()));
        novos.forEach(lotes::salvar);
        doacoes.salvar(doacao.id(), doacao);
        return doacao;
    }

    public Coleta agendarColeta(int idDoacao, LocalDate dataAgendada) {
        Doacao doacao = doacoes.buscar(idDoacao)
            .orElseThrow(() -> new RegraNegocioException("Doação não encontrada."));
        if (coletasPorDoacao.buscar(idDoacao).isPresent()) {
            throw new RegraNegocioException("A doação já tem uma coleta.");
        }
        Coleta coleta = new Coleta(coletasPorDoacao.listar().size() + 1, doacao, dataAgendada);
        coletasPorDoacao.salvar(idDoacao, coleta);
        return coleta;
    }

    public Entrega registrarEntrega(Instituicao instituicao, LoteEntrega item) {
        return registrarEntrega(instituicao, List.of(item));
    }

    public Entrega registrarEntrega(Instituicao instituicao, List<LoteEntrega> itens) {
        LocalDate data = hoje();
        Entrega entrega = new Entrega(entregas.listar().size() + 1, instituicao, data, itens);
        Map<String, Lote> baixas = new LinkedHashMap<>();
        for (LoteEntrega item : entrega.itens()) {
            String codigo = item.codigoLote();
            if (baixas.containsKey(codigo)) {
                throw new RegraNegocioException("O mesmo lote só pode aparecer uma vez na entrega.");
            }
            Lote lote = lotes.buscar(codigo)
                .orElseThrow(() -> new RegraNegocioException("Lote não encontrado: " + codigo));
            baixas.put(codigo, lote.retirar(item.quantidadeEntregue(), data));
        }
        baixas.forEach(lotes::salvar);
        entregas.salvar(entrega.id(), entrega);
        return entrega;
    }
}
