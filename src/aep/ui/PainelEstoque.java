package aep.ui;

import aep.dominio.*;
import aep.servico.BancoAlimentos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

final class PainelEstoque extends JPanel {
    private final BancoAlimentos banco;
    private final JComboBox<Doador> doadores = new JComboBox<>();
    private final JComboBox<Lote> lotes = new JComboBox<>();
    private final DefaultTableModel modelo = new DefaultTableModel(
        new String[]{"Código", "Alimento", "Validade", "Recebido", "Saldo", "Unidade", "Situação"}, 0
    ) {
        @Override
        public boolean isCellEditable(int linha, int coluna) { return false; }
    };
    private final JLabel status = new JLabel(" ");

    PainelEstoque(BancoAlimentos banco, List<Alimento> alimentos, List<Instituicao> instituicoes) {
        super(new BorderLayout(0, 14));
        this.banco = banco;
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JTable tabela = new JTable(modelo);
        tabela.setName("tabelaEstoque");
        tabela.setRowHeight(26);

        JPanel formularios = new JPanel(new GridLayout(1, 2, 14, 0));
        formularios.add(criarRecebimento(alimentos));
        formularios.add(criarExpedicao(instituicoes));
        add(formularios, BorderLayout.NORTH);
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setColumnHeaderView(tabela.getTableHeader());
        add(rolagem, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        atualizarDoadores();
        atualizarEstoque();
    }

    private JPanel criarRecebimento(List<Alimento> alimentos) {
        JComboBox<Alimento> alimento = new JComboBox<>(alimentos.toArray(Alimento[]::new));
        JTextField codigo = new JTextField();
        codigo.setName("codigoLote");
        JTextField validade = new JTextField(banco.hoje().plusDays(7).toString());
        JTextField quantidade = new JTextField();
        quantidade.setName("quantidadeRecebida");
        JPanel painel = new JPanel(new GridLayout(0, 2, 8, 6));
        painel.setBorder(BorderFactory.createTitledBorder("Receber doação"));
        campo(painel, "Doador", doadores);
        campo(painel, "Alimento", alimento);
        campo(painel, "Código do lote", codigo);
        campo(painel, "Validade (AAAA-MM-DD)", validade);
        campo(painel, "Quantidade", quantidade);
        JButton receber = new JButton("Registrar recebimento");
        receber.addActionListener(evento -> executar(() -> {
            Doador doador = (Doador) doadores.getSelectedItem();
            if (doador == null) throw new RegraNegocioException("Cadastre e selecione um doador.");
            Lote lote = new Lote(codigo.getText(), (Alimento) alimento.getSelectedItem(),
                LocalDate.parse(validade.getText().strip()), decimal(quantidade), false);
            Doacao doacao = banco.registrarDoacao(doador.id(), banco.hoje(), lote);
            atualizarEstoque();
            codigo.setText("");
            quantidade.setText("");
            status.setText("Doação #" + doacao.id() + " registrada.");
        }));
        campo(painel, "", receber);
        return painel;
    }

    private JPanel criarExpedicao(List<Instituicao> instituicoes) {
        JComboBox<Instituicao> instituicao = new JComboBox<>(instituicoes.toArray(Instituicao[]::new));
        lotes.setName("loteEntrega");
        JTextField quantidade = new JTextField();
        quantidade.setName("quantidadeEntregue");
        JPanel campos = new JPanel(new GridLayout(0, 2, 8, 6));
        campo(campos, "Instituição", instituicao);
        campo(campos, "Lote", lotes);
        campo(campos, "Quantidade", quantidade);
        JButton entregar = new JButton("Registrar entrega");
        entregar.addActionListener(evento -> executar(() -> {
            Lote lote = (Lote) lotes.getSelectedItem();
            if (lote == null) throw new RegraNegocioException("Selecione um lote.");
            LoteEntrega item = new LoteEntrega(lote.codigo(), decimal(quantidade));
            Entrega entrega = banco.registrarEntrega((Instituicao) instituicao.getSelectedItem(), item);
            atualizarEstoque();
            quantidade.setText("");
            status.setText("Entrega #" + entrega.id() + " registrada.");
        }));
        campo(campos, "", entregar);
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Expedir lote"));
        painel.add(campos, BorderLayout.NORTH);
        return painel;
    }

    void atualizarDoadores() {
        doadores.setModel(new DefaultComboBoxModel<>(banco.listarDoadores().toArray(Doador[]::new)));
    }

    private void atualizarEstoque() {
        List<Lote> estoque = banco.listarLotes();
        lotes.setModel(new DefaultComboBoxModel<>(estoque.toArray(Lote[]::new)));
        modelo.setRowCount(0);
        for (Lote lote : estoque) {
            modelo.addRow(new Object[]{lote.codigo(), lote.alimento().nome(), lote.validade(),
                lote.quantidade().toPlainString(), lote.saldo().toPlainString(),
                lote.alimento().unidadeMedida(), lote.situacao(banco.hoje())});
        }
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RegraNegocioException erro) {
            status.setText(erro.getMessage());
        } catch (DateTimeParseException erro) {
            status.setText("Use uma data válida no formato AAAA-MM-DD.");
        } catch (NumberFormatException erro) {
            status.setText("Informe uma quantidade numérica, como 2 ou 2,5.");
        }
    }

    private static void campo(JPanel painel, String texto, JComponent componente) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setLabelFor(componente);
        painel.add(rotulo);
        painel.add(componente);
    }

    private static BigDecimal decimal(JTextField campo) {
        return new BigDecimal(campo.getText().strip().replace(',', '.'));
    }
}
