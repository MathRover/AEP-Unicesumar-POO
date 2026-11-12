package aep.dominio;

import java.math.BigDecimal;

public final class Validacao {
    private Validacao() {
    }

    public static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("Preencha " + campo + ".");
        }
        return valor.strip();
    }

    public static String email(String valor) {
        String email = texto(valor, "o e-mail");
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new RegraNegocioException("E-mail inválido.");
        }
        return email;
    }

    public static BigDecimal quantidade(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0 || valor.stripTrailingZeros().scale() > 3) {
            throw new RegraNegocioException("Use uma quantidade positiva com até três casas decimais.");
        }
        return valor;
    }
}
