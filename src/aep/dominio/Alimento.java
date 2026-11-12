package aep.dominio;

public record Alimento(int id, String nome, String categoria, String unidadeMedida) {
    public Alimento {
        nome = Validacao.texto(nome, "o alimento");
        categoria = Validacao.texto(categoria, "a categoria");
        unidadeMedida = Validacao.texto(unidadeMedida, "a unidade");
    }

    @Override
    public String toString() { return nome + " (" + unidadeMedida + ")"; }
}
