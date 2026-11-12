package aep.ui;

import aep.dominio.*;
import aep.persistencia.RepositorioEmMemoria;
import aep.servico.BancoAlimentos;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

public final class Aplicacao {
    private Aplicacao() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ReflectiveOperationException | UnsupportedLookAndFeelException erro) {
                System.err.println("Interface padrão do Java selecionada.");
            }

            Alimento arroz = new Alimento(1, "Arroz", "Grãos", "kg");
            Alimento feijao = new Alimento(2, "Feijão", "Leguminosas", "kg");
            Alimento leite = new Alimento(3, "Leite", "Laticínios", "L");
            List<Instituicao> instituicoes = List.of(
                new Instituicao(1, "Cozinha Solidária", "33333333000133", "cozinha@example.com", "", "Rua Exemplo, 100"),
                new Instituicao(2, "Creche Parceira", "44444444000144", "creche@example.com", "", "Rua Exemplo, 200")
            );
            BancoAlimentos banco = new BancoAlimentos(Clock.systemDefaultZone(), new RepositorioEmMemoria<>());
            Doador doador = banco.cadastrarDoador("Mercado Exemplo", Doador.Tipo.PJ,
                "11111111000111", "mercado@example.com", "43900000001");
            banco.registrarDoacao(doador.id(), banco.hoje().minusDays(3), List.of(
                new Lote("ARZ-001", arroz, banco.hoje().plusDays(20), new BigDecimal("25"), false),
                new Lote("LET-001", leite, banco.hoje().minusDays(1), new BigDecimal("10"), false)
            ));

            JFrame janela = new JFrame("Banco de Alimentos de Londrina");
            janela.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            janela.setContentPane(conteudo(banco, List.of(arroz, feijao, leite), instituicoes));
            janela.setMinimumSize(new Dimension(920, 580));
            janela.setSize(1040, 650);
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);
        });
    }

    static JPanel conteudo(BancoAlimentos banco, List<Alimento> alimentos, List<Instituicao> instituicoes) {
        PainelEstoque estoque = new PainelEstoque(banco, alimentos, instituicoes);
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Doadores", new PainelDoadores(banco, estoque::atualizarDoadores));
        abas.addTab("Estoque e movimentações", estoque);

        JLabel titulo = new JLabel("Banco de Alimentos de Londrina");
        titulo.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        titulo.setOpaque(true);
        titulo.setBackground(new Color(25, 67, 62));
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));

        JPanel painel = new JPanel(new BorderLayout());
        painel.add(titulo, BorderLayout.NORTH);
        painel.add(abas, BorderLayout.CENTER);
        return painel;
    }
}
