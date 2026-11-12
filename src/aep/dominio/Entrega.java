package aep.dominio;

import java.time.LocalDate;
import java.util.List;

public record Entrega(int id, Instituicao instituicao, LocalDate data, List<LoteEntrega> itens) {
    public Entrega {
        if (instituicao == null || data == null || itens == null || itens.isEmpty()) {
            throw new RegraNegocioException("A entrega precisa de instituição, data e lotes.");
        }
        itens = List.copyOf(itens);
    }
}
