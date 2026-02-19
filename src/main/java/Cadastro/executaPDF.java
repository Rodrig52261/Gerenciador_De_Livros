package Cadastro;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class executaPDF extends JFrame {

    private PDDocument documento;
    private PDFRenderer renderer;
    private int paginaEsquerda = 0;
    private int totalPaginas = 0;
    private Cadastro livro;

    private JLabel lblPagina;
    private JLabel labelPagEsquerda;
    private JLabel labelPagDireita;
    private JScrollPane scrollPane;
    private ArrayList<Cadastro> listaParaSalvar;

    public executaPDF(Cadastro livro, ArrayList<Cadastro> listaParaSalvar) {
        this.livro = livro;
        this.listaParaSalvar = listaParaSalvar;

        this.paginaEsquerda = livro.getUltimaPagina();
        if (paginaEsquerda % 2 != 0 && paginaEsquerda > 0) paginaEsquerda--;

        setTitle("Modo Livro: " + livro.getNomeDoLivro());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // --- PAINEL DE NAVEGAÇÃO ---
        JPanel painelNav = new JPanel(new FlowLayout());
        painelNav.setBackground(new Color(25, 25, 25));

        JButton btnAnterior = new JButton("◀ Anterior");
        JButton btnProximo = new JButton("Próximo ▶");
        lblPagina = new JLabel("Página 0 de 0");

        estilizarBotao(btnAnterior);
        estilizarBotao(btnProximo);
        lblPagina.setForeground(Color.WHITE);
        lblPagina.setFont(new Font("Segoe UI", Font.BOLD, 14));

        painelNav.add(btnAnterior);
        painelNav.add(lblPagina);
        painelNav.add(btnProximo);

        // --- ÁREA DO LIVRO (PÁGINAS E SOMBRA) ---
        JPanel painelFolhas = new JPanel(new GridLayout(1, 2, 0, 0)); // 0 de espaço para a sombra se unir
        painelFolhas.setBackground(new Color(35, 35, 35));
        painelFolhas.setBorder(new EmptyBorder(20, 40, 20, 40));

        // 1. Instanciar primeiro
        labelPagEsquerda = new JLabel();
        labelPagEsquerda.setHorizontalAlignment(JLabel.RIGHT);

        labelPagDireita = new JLabel();
        labelPagDireita.setHorizontalAlignment(JLabel.LEFT);

        // 2. Aplicar bordas (Sombra Central)
        Color corSombra = new Color(0, 0, 0, 80);
        labelPagEsquerda.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, corSombra));
        labelPagDireita.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, corSombra));

        painelFolhas.add(labelPagEsquerda);
        painelFolhas.add(labelPagDireita);

        scrollPane = new JScrollPane(painelFolhas);
        scrollPane.setBackground(new Color(30, 30, 30));
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        add(painelNav, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        carregarPDF();

        // Atalhos de Teclado
        // Atalhos de teclado para passar páginas e scroll
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(java.awt.event.KeyEvent e) {
                if (!isShowing()) return false;

                if (e.getID() == java.awt.event.KeyEvent.KEY_PRESSED) {
                    int keyCode = e.getKeyCode();
                    JScrollBar verticalBar = scrollPane.getVerticalScrollBar();

                    if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
                        avancarPagina();
                        verticalBar.setValue(0); // Volta o scroll para o topo na nova página
                    } else if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
                        voltarPagina();
                        verticalBar.setValue(0); // Volta o scroll para o topo na nova página
                    } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
                        // Scroll para baixo: valor atual + incremento
                        verticalBar.setValue(verticalBar.getValue() + 30);
                    } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
                        // Scroll para cima: valor atual - incremento
                        verticalBar.setValue(verticalBar.getValue() - 30);
                    }
                }
                return false;
            }
        });

        btnAnterior.addActionListener(e -> voltarPagina());
        btnProximo.addActionListener(e -> avancarPagina());

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) { renderizarPaginas(); }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                livro.setUltimaPagina(paginaEsquerda);
                fecharPDF();
                Salvar.salvarDados(listaParaSalvar, "dados/lendo.json");
            }
        });
    }

    private void avancarPagina() {
        if (paginaEsquerda + 2 < totalPaginas) {
            paginaEsquerda += 2;
            renderizarPaginas();
        }
    }

    private void voltarPagina() {
        if (paginaEsquerda >= 2) {
            paginaEsquerda -= 2;
            renderizarPaginas();
        }
    }

    private void carregarPDF() {
        try {
            documento = Loader.loadPDF(new File(livro.getPathPDF()));
            renderer = new PDFRenderer(documento);
            totalPaginas = documento.getNumberOfPages();
            renderizarPaginas();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void renderizarPaginas() {
        if (documento == null) return;
        int larguraContainer = (scrollPane.getWidth() / 2) - 60;
        if (larguraContainer < 100) larguraContainer = 500;

        try {
            labelPagEsquerda.setIcon(new ImageIcon(gerarImagemPagina(paginaEsquerda, larguraContainer)));
            if (paginaEsquerda + 1 < totalPaginas) {
                labelPagDireita.setIcon(new ImageIcon(gerarImagemPagina(paginaEsquerda + 1, larguraContainer)));
                labelPagDireita.setVisible(true);
                lblPagina.setText("Páginas " + (paginaEsquerda + 1) + "-" + (paginaEsquerda + 2) + " de " + totalPaginas);
            } else {
                labelPagDireita.setVisible(false);
                lblPagina.setText("Página " + (paginaEsquerda + 1) + " de " + totalPaginas);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Image gerarImagemPagina(int index, int larguraAlvo) throws IOException {
        BufferedImage img = renderer.renderImageWithDPI(index, 100);
        double ratio = (double) larguraAlvo / img.getWidth();
        int novaAltura = (int) (img.getHeight() * ratio);
        return img.getScaledInstance(larguraAlvo, novaAltura, Image.SCALE_SMOOTH);
    }

    private void fecharPDF() {
        try { if (documento != null) documento.close(); } catch (IOException e) {}
    }

    private void estilizarBotao(JButton b) {
        b.setBackground(new Color(60, 60, 60));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}