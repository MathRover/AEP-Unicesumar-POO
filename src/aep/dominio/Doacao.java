package aep.dominio;

import java.time.LocalDate;
import java.util.List;

public record Doacao(int id, int idDoador, LocalDate recebimento, List<String> codigosLotes) {
    public Doacao {
        if (recebimento == null || codigosLotes == null || codigosLotes.isEmpty()) {
            throw new RegraNegocioException("A doação precisa de data de recebimento e lotes.");
        }
        codigosLotes = List.copyOf(codigosLotes);
    }
}
