package aep.dominio;

import java.math.BigDecimal;

public record LoteEntrega(String codigoLote, BigDecimal quantidadeEntregue) {
    public LoteEntrega {
        codigoLote = Validacao.texto(codigoLote, "o código do lote");
        Validacao.quantidade(quantidadeEntregue);
    }
}
