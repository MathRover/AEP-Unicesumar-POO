package aep.dominio;

public record Perfil(int id, String nome, String descricao) {
    public Perfil {
        nome = Validacao.texto(nome, "o perfil");
    }
}
