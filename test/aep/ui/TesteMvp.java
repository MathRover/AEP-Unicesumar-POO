package aep.ui;

import aep.dominio.*;
import aep.persistencia.RepositorioEmMemoria;
import aep.servico.BancoAlimentos;

import javax.swing.*;
import java.awt.Component;
import java.awt.Container;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public final class TesteMvp {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(TesteMvp::testar);
        System.out.println("CRUD Swing, recebimento, entrega e cenários inválidos verificados.");
    }

    private static void testar() {
        BancoAlimentos banco = new BancoAlimentos(
            Clock.fixed(Instant.parse("2026-11-12T12:00:00Z"), ZoneOffset.UTC), new RepositorioEmMemoria<>());
        Alimento arroz = new Alimento(1, "Arroz", "Grãos", "kg");
        Instituicao cozinha = new Instituicao(1, "Cozinha", "33333333000133", "cozinha@example.com", "", "Rua A");
        JPanel tela = Aplicacao.conteudo(banco, List.of(arroz), List.of(cozinha));

        botao(tela, "Salvar").doClick(0);
        verificar(banco.listarDoadores().isEmpty(), "Campos vazios foram aceitos.");
        texto(tela, "nome", "Doador de teste");
        texto(tela, "documento", "123.456.789-01");
        texto(tela, "email", "teste@example.com");
        botao(tela, "Salvar").doClick(0);
        JTable doadores = componente(tela, JTable.class, "tabelaDoadores");
        verificar(doadores.getRowCount() == 1, "Cadastro não apareceu na tabela.");
        verificar(banco.consultarDoador(1).documento().equals("12345678901"), "CPF não foi normalizado.");

        doadores.setRowSelectionInterval(0, 0);
        texto(tela, "nome", "Doador atualizado");
        botao(tela, "Salvar").doClick(0);
        verificar(banco.consultarDoador(1).nome().equals("Doador atualizado"), "Edição falhou.");
        erro(() -> banco.cadastrarDoador("Outro", Doador.Tipo.PF, "12345678901", "outro@example.com", ""),
            "já cadastrado");
        doadores.setRowSelectionInterval(0, 0);
        botao(tela, "Excluir selecionado").doClick(0);
        verificar(doadores.getRowCount() == 0, "Exclusão não atualizou a tabela.");
        erro(() -> banco.consultarDoador(1), "não encontrado");

        texto(tela, "nome", "Doador do lote");
        texto(tela, "documento", "23456789012");
        texto(tela, "email", "lote@example.com");
        botao(tela, "Salvar").doClick(0);
        texto(tela, "codigoLote", "ARZ-001");
        texto(tela, "quantidadeRecebida", "10,5");
        botao(tela, "Registrar recebimento").doClick(0);
        JTable estoque = componente(tela, JTable.class, "tabelaEstoque");
        verificar(estoque.getRowCount() == 1, "Recebimento não apareceu no estoque.");
        verificar(saldo(banco).compareTo(new BigDecimal("10.5")) == 0, "Saldo inicial incorreto.");
        texto(tela, "quantidadeEntregue", "3");
        botao(tela, "Registrar entrega").doClick(0);
        verificar(saldo(banco).compareTo(new BigDecimal("7.5")) == 0, "A entrega não baixou o saldo.");
        texto(tela, "quantidadeEntregue", "8");
        botao(tela, "Registrar entrega").doClick(0);
        verificar(banco.listarEntregas().size() == 1, "Entrega sem saldo foi registrada.");
        erro(() -> banco.excluirDoador(2), "com doações");
        erro(() -> banco.registrarDoacao(2, banco.hoje(),
            new Lote("INV-001", arroz, banco.hoje().minusDays(1), BigDecimal.ONE, false)),
            "anterior ao recebimento");
        banco.agendarColeta(1, banco.hoje());
        erro(() -> banco.agendarColeta(1, banco.hoje()), "já tem uma coleta");

        banco.registrarDoacao(2, banco.hoje().minusDays(2), List.of(
            new Lote("VENCIDO", arroz, banco.hoje().minusDays(1), BigDecimal.TEN, false),
            new Lote("BLOQUEADO", arroz, banco.hoje().plusDays(1), BigDecimal.TEN, true)
        ));
        erro(() -> banco.registrarEntrega(cozinha, List.of(
            new LoteEntrega("ARZ-001", BigDecimal.ONE), new LoteEntrega("VENCIDO", BigDecimal.ONE))),
            "vencido");
        erro(() -> banco.registrarEntrega(cozinha, new LoteEntrega("BLOQUEADO", BigDecimal.ONE)), "bloqueado");
        verificar(saldo(banco).compareTo(new BigDecimal("7.5")) == 0, "Uma entrega inválida alterou o estoque.");
        verificar(banco.listarEntregas().size() == 1, "Entrega inválida foi registrada.");
    }

    private static BigDecimal saldo(BancoAlimentos banco) {
        return banco.listarLotes().get(0).saldo();
    }

    private static void erro(Runnable acao, String mensagem) {
        try {
            acao.run();
        } catch (RegraNegocioException erro) {
            verificar(erro.getMessage().contains(mensagem), "Mensagem inesperada: " + erro.getMessage());
            return;
        }
        throw new AssertionError("Esperava falha: " + mensagem);
    }

    private static void texto(Container tela, String nome, String valor) {
        componente(tela, JTextField.class, nome).setText(valor);
    }

    private static JButton botao(Container tela, String texto) {
        return componente(tela, JButton.class, texto);
    }

    private static <T extends Component> T componente(Container raiz, Class<T> tipo, String identificador) {
        T resultado = procurar(raiz, tipo, identificador);
        if (resultado == null) throw new AssertionError("Componente ausente: " + identificador);
        return resultado;
    }

    private static <T extends Component> T procurar(Container raiz, Class<T> tipo, String identificador) {
        for (Component filho : raiz.getComponents()) {
            String nome = filho instanceof JButton botao ? botao.getText() : filho.getName();
            if (tipo.isInstance(filho) && identificador.equals(nome)) return tipo.cast(filho);
            if (filho instanceof Container grupo) {
                T resultado = procurar(grupo, tipo, identificador);
                if (resultado != null) return resultado;
            }
        }
        return null;
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }
}
