package aep.dominio;

public record Usuario(int id, String nome, String email, Perfil perfil, boolean ativo) {
    public Usuario {
        nome = Validacao.texto(nome, "o usuário");
        email = Validacao.email(email);
        if (perfil == null) throw new RegraNegocioException("Selecione um perfil.");
    }
}
