package aep.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class Lote {
    private final String codigo;
    private final Alimento alimento;
    private final LocalDate validade;
    private final BigDecimal quantidade;
    private final BigDecimal saldo;
    private final boolean bloqueado;

    public Lote(String codigo, Alimento alimento, LocalDate validade, BigDecimal quantidade, boolean bloqueado) {
        this(codigo, alimento, validade, Validacao.quantidade(quantidade), quantidade, bloqueado);
    }

    private Lote(String codigo, Alimento alimento, LocalDate validade, BigDecimal quantidade,
                 BigDecimal saldo, boolean bloqueado) {
        this.codigo = Validacao.texto(codigo, "o código do lote");
        if (alimento == null || validade == null) {
            throw new RegraNegocioException("Informe o alimento e a validade.");
        }
        this.alimento = alimento;
        this.validade = validade;
        this.quantidade = quantidade;
        this.saldo = saldo;
        this.bloqueado = bloqueado;
    }

    public String codigo() { return codigo; }
    public Alimento alimento() { return alimento; }
    public LocalDate validade() { return validade; }
    public BigDecimal quantidade() { return quantidade; }
    public BigDecimal saldo() { return saldo; }
    public boolean bloqueado() { return bloqueado; }

    public String situacao(LocalDate data) {
        if (validade.isBefore(data)) return "Vencido";
        if (bloqueado) return "Bloqueado";
        if (saldo.signum() == 0) return "Esgotado";
        return "Disponível";
    }

    public Lote retirar(BigDecimal quantidadeRetirada, LocalDate data) {
        Validacao.quantidade(quantidadeRetirada);
        if (validade.isBefore(data)) throw new RegraNegocioException("Lote " + codigo + " vencido.");
        if (bloqueado) throw new RegraNegocioException("Lote " + codigo + " bloqueado.");
        if (saldo.compareTo(quantidadeRetirada) < 0) {
            throw new RegraNegocioException("Estoque insuficiente no lote " + codigo + ".");
        }
        return new Lote(codigo, alimento, validade, quantidade, saldo.subtract(quantidadeRetirada), bloqueado);
    }

    @Override
    public String toString() { return codigo + " · " + alimento; }
}
