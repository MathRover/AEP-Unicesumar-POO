package aep.dominio;

public abstract class CadastroContato {
    private final int id;
    private final String nome;
    private final String email;
    private final String telefone;

    protected CadastroContato(int id, String nome, String email, String telefone) {
        this.id = id;
        this.nome = Validacao.texto(nome, "o nome");
        this.email = Validacao.email(email);
        this.telefone = telefone == null ? "" : telefone.strip();
    }

    public final int id() { return id; }
    public final String nome() { return nome; }
    public final String email() { return email; }
    public final String telefone() { return telefone; }

    public abstract String documento();

    @Override
    public final String toString() {
        return nome + " (" + documento() + ")";
    }
}
