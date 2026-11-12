package aep.dominio;

public final class Doador extends CadastroContato {
    public enum Tipo { PF, PJ }

    private final Tipo tipo;
    private final String documento;

    public Doador(int id, String nome, Tipo tipo, String documento, String email, String telefone) {
        super(id, nome, email, telefone);
        if (tipo == null) {
            throw new RegraNegocioException("Selecione PF ou PJ.");
        }
        this.tipo = tipo;
        this.documento = Validacao.texto(documento, "o CPF ou CNPJ").replaceAll("[./\\s-]", "");
        if (!this.documento.matches(tipo == Tipo.PF ? "[0-9]{11}" : "[0-9]{14}")) {
            throw new RegraNegocioException("Informe 11 dígitos para CPF ou 14 para CNPJ.");
        }
    }

    public Tipo tipo() { return tipo; }

    @Override
    public String documento() { return documento; }
}
