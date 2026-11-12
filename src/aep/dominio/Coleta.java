package aep.dominio;

import java.time.LocalDate;

public record Coleta(int id, Doacao doacao, LocalDate dataAgendada) {
    public Coleta {
        if (doacao == null || dataAgendada == null) {
            throw new RegraNegocioException("A coleta precisa de doação e data.");
        }
    }
}
