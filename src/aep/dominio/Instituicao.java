package aep.dominio;

public final class Instituicao extends CadastroContato {
    private final String cnpj;
    private final String endereco;

    public Instituicao(int id, String nome, String cnpj, String email, String telefone, String endereco) {
        super(id, nome, email, telefone);
        this.cnpj = Validacao.texto(cnpj, "o CNPJ");
        if (!this.cnpj.matches("[0-9]{14}")) {
            throw new RegraNegocioException("O CNPJ deve ter 14 dígitos.");
        }
        this.endereco = Validacao.texto(endereco, "o endereço");
    }

    @Override
    public String documento() { return cnpj; }

    public String endereco() { return endereco; }
}
