package Cadastro;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class SearchBooks extends JDialog {

    private JTextField txtBusca;
    private DefaultListModel<String> modeloLista;
    private JList<String> listaResultados;
    private ArrayList<Cadastro> livrosEncontrados;
    private AddLivro telaCadastro;
    private JButton btnPesquisar;

    public SearchBooks(AddLivro parent) {
        super(parent, "Buscar Livro no Google Books", true);
        this.telaCadastro = parent;
        this.livrosEncontrados = new ArrayList<>();

        configurarJanela();
        criarInterface();
    }

    private void configurarJanela() {
        setSize(650, 500);
        setLocationRelativeTo(telaCadastro);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(30, 30, 30));
    }

    private void criarInterface() {
        // --- PAINEL DE BUSCA (TOPO) ---
        JPanel painelBusca = new JPanel(new BorderLayout(10, 0));
        painelBusca.setBackground(new Color(30, 30, 30));
        painelBusca.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        txtBusca = new JTextField();
        txtBusca.setBackground(new Color(50, 50, 50));
        txtBusca.setForeground(Color.WHITE);
        txtBusca.setCaretColor(Color.WHITE);
        txtBusca.setFont(new Font("Arial", Font.PLAIN, 14));
        txtBusca.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(88, 166, 255)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        btnPesquisar = new JButton("BUSCAR");
        btnPesquisar.setBackground(new Color(88, 166, 255));
        btnPesquisar.setForeground(Color.WHITE);
        btnPesquisar.setFocusPainted(false);
        btnPesquisar.setFont(new Font("Arial", Font.BOLD, 13));
        btnPesquisar.setPreferredSize(new Dimension(120, 40));
        btnPesquisar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        painelBusca.add(txtBusca, BorderLayout.CENTER);
        painelBusca.add(btnPesquisar, BorderLayout.EAST);

        // --- LISTA DE RESULTADOS (CENTRO) ---
        modeloLista = new DefaultListModel<>();
        listaResultados = new JList<>(modeloLista);
        listaResultados.setBackground(new Color(45, 45, 45));
        listaResultados.setForeground(Color.WHITE);
        listaResultados.setSelectionBackground(new Color(88, 166, 255));
        listaResultados.setSelectionForeground(Color.WHITE);
        listaResultados.setFont(new Font("Arial", Font.PLAIN, 13));
        listaResultados.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(listaResultados);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                " Resultados ",
                0, 0,
                new Font("Arial", Font.BOLD, 12),
                Color.LIGHT_GRAY
        ));
        scroll.getViewport().setBackground(new Color(45, 45, 45));

        // --- BOTÃO CONFIRMAR (RODAPÉ) ---
        JButton btnConfirmar = new JButton("USAR ESTE LIVRO");
        btnConfirmar.setBackground(new Color(46, 204, 113));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.setFont(new Font("Arial", Font.BOLD, 14));
        btnConfirmar.setPreferredSize(new Dimension(0, 50));
        btnConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // --- MONTAGEM ---
        add(painelBusca, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(btnConfirmar, BorderLayout.SOUTH);

        // --- EVENTOS ---
        btnPesquisar.addActionListener(e -> realizarBusca());
        txtBusca.addActionListener(e -> realizarBusca());
        btnConfirmar.addActionListener(e -> selecionarLivro());

        // Duplo clique na lista também seleciona
        listaResultados.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    selecionarLivro();
                }
            }
        });
    }

    private void realizarBusca() {
        String termo = txtBusca.getText().trim();

        if (termo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite algo para buscar!");
            return;
        }

        btnPesquisar.setEnabled(false);
        btnPesquisar.setText("Buscando...");
        modeloLista.clear();
        livrosEncontrados.clear();

        new Thread(() -> {
            ArrayList<Cadastro> resultados = LivroService.buscarLivros(termo, 3);

            SwingUtilities.invokeLater(() -> {
                if (resultados.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Nenhum livro encontrado.\n\nDica: Tente buscar apenas o título principal ou o autor.",
                            "Sem resultados",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    for (Cadastro livro : resultados) {
                        livrosEncontrados.add(livro);

                        // Formata a exibição na lista
                        String display = String.format(
                                "<html><b>%s</b><br><i>%s</i> • %d páginas</html>",
                                livro.getNomeDoLivro(),
                                livro.getAutor(),
                                livro.getQtdPag()
                        );
                        modeloLista.addElement(display);
                    }
                }

                btnPesquisar.setEnabled(true);
                btnPesquisar.setText("BUSCAR");
            });
        }).start();
    }

    private void selecionarLivro() {
        int index = listaResultados.getSelectedIndex();

        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um livro da lista!");
            return;
        }

        Cadastro selecionado = livrosEncontrados.get(index);
        telaCadastro.preencherCamposManualmente(selecionado);
        dispose();
    }
}