package aep.ui;

import aep.dominio.Doador;
import aep.dominio.RegraNegocioException;
import aep.servico.BancoAlimentos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

final class PainelDoadores extends JPanel {
    private final BancoAlimentos banco;
    private final Runnable aoAtualizar;
    private final JTextField nome = new JTextField();
    private final JComboBox<Doador.Tipo> tipo = new JComboBox<>(Doador.Tipo.values());
    private final JTextField documento = new JTextField();
    private final JTextField email = new JTextField();
    private final JTextField telefone = new JTextField();
    private final JLabel status = new JLabel(" ");
    private final DefaultTableModel modelo = new DefaultTableModel(
        new String[]{"ID", "Nome", "Tipo", "CPF / CNPJ", "E-mail", "Telefone"}, 0
    ) {
        @Override
        public boolean isCellEditable(int linha, int coluna) { return false; }
    };
    private final JTable tabela = new JTable(modelo);
    private Integer idSelecionado;

    PainelDoadores(BancoAlimentos banco, Runnable aoAtualizar) {
        super(new BorderLayout(0, 14));
        this.banco = banco;
        this.aoAtualizar = aoAtualizar;
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel formulario = new JPanel(new GridLayout(2, 5, 10, 5));
        for (String rotulo : new String[]{"Nome", "Tipo", "CPF / CNPJ", "E-mail", "Telefone"}) {
            formulario.add(new JLabel(rotulo));
        }
        nome.setName("nome");
        documento.setName("documento");
        email.setName("email");
        telefone.setName("telefone");
        formulario.add(nome);
        formulario.add(tipo);
        formulario.add(documento);
        formulario.add(email);
        formulario.add(telefone);

        JButton novo = new JButton("Novo");
        JButton salvar = new JButton("Salvar");
        JButton excluir = new JButton("Excluir selecionado");
        novo.addActionListener(evento -> limpar());
        salvar.addActionListener(evento -> executar(this::salvar));
        excluir.addActionListener(evento -> executar(() -> {
            if (idSelecionado == null) throw new RegraNegocioException("Selecione um doador.");
            banco.excluirDoador(idSelecionado);
            limpar();
            atualizarTabela();
            status.setText("Doador excluído.");
        }));
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        acoes.add(novo);
        acoes.add(Box.createHorizontalStrut(8));
        acoes.add(salvar);
        acoes.add(Box.createHorizontalStrut(8));
        acoes.add(excluir);
        JPanel topo = new JPanel(new BorderLayout(0, 8));
        topo.add(formulario, BorderLayout.CENTER);
        topo.add(acoes, BorderLayout.SOUTH);

        tabela.setName("tabelaDoadores");
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(26);
        tabela.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting() && tabela.getSelectedRow() >= 0) {
                int linha = tabela.getSelectedRow();
                preencher(banco.consultarDoador((Integer) modelo.getValueAt(linha, 0)));
            }
        });
        add(topo, BorderLayout.NORTH);
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setColumnHeaderView(tabela.getTableHeader());
        add(rolagem, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        atualizarTabela();
    }

    private void preencher(Doador doador) {
        idSelecionado = doador.id();
        nome.setText(doador.nome());
        tipo.setSelectedItem(doador.tipo());
        documento.setText(doador.documento());
        email.setText(doador.email());
        telefone.setText(doador.telefone());
        status.setText("Editando doador #" + doador.id() + ".");
    }

    private void limpar() {
        tabela.clearSelection();
        idSelecionado = null;
        nome.setText("");
        tipo.setSelectedItem(Doador.Tipo.PF);
        documento.setText("");
        email.setText("");
        telefone.setText("");
        status.setText("Novo cadastro.");
        nome.requestFocusInWindow();
    }

    private void salvar() {
        Doador.Tipo tipoSelecionado = (Doador.Tipo) tipo.getSelectedItem();
        if (idSelecionado == null) {
            banco.cadastrarDoador(nome.getText(), tipoSelecionado, documento.getText(), email.getText(), telefone.getText());
        } else {
            banco.atualizarDoador(idSelecionado, nome.getText(), tipoSelecionado, documento.getText(), email.getText(), telefone.getText());
        }
        limpar();
        atualizarTabela();
        status.setText("Doador salvo.");
    }

    private void atualizarTabela() {
        modelo.setRowCount(0);
        for (Doador d : banco.listarDoadores()) {
            modelo.addRow(new Object[]{d.id(), d.nome(), d.tipo(), d.documento(), d.email(), d.telefone()});
        }
        aoAtualizar.run();
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RegraNegocioException erro) {
            status.setText(erro.getMessage());
        }
    }
}
