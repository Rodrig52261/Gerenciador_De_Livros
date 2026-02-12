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
    private int paginaAtual = 0;
    private int totalPaginas = 0;
    private Cadastro livro;

    private JLabel lblPagina;
    private JLabel lblImagem;
    private JScrollPane scrollPane;
    private ArrayList<Cadastro> listaParaSalvar;

    public executaPDF(Cadastro livro, ArrayList<Cadastro> listaParaSalvar) {
        this.livro = livro;
        this.listaParaSalvar = listaParaSalvar;
        this.paginaAtual = livro.getUltimaPagina();

        setTitle("Lendo: " + livro.getNomeDoLivro());
        setSize(900, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(30, 30, 30));

        // Painel de navegação no topo
        JPanel painelNav = new JPanel(new FlowLayout());
        painelNav.setBackground(new Color(25, 25, 25));

        JButton btnAnterior = new JButton("◀ Anterior");
        JButton btnProximo = new JButton("Próximo ▶");
        lblPagina = new JLabel("Página 0 de 0");

        estilizarBotao(btnAnterior);
        estilizarBotao(btnProximo);
        lblPagina.setForeground(Color.WHITE);
        lblPagina.setFont(new Font("Arial", Font.BOLD, 14));

        painelNav.add(btnAnterior);
        painelNav.add(lblPagina);
        painelNav.add(btnProximo);

        // Área de exibição da página
        lblImagem = new JLabel();
        lblImagem.setHorizontalAlignment(JLabel.CENTER);
        lblImagem.setBackground(new Color(30, 30, 30));
        lblImagem.setOpaque(true);

        scrollPane = new JScrollPane(lblImagem);
        scrollPane.setBackground(new Color(30, 30, 30));
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        add(painelNav, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Carrega o PDF
        carregarPDF();

        // Ações dos botões
        btnAnterior.addActionListener(e -> {
            if (paginaAtual > 0) {
                paginaAtual--;
                renderizarPagina();
            }
        });

        btnProximo.addActionListener(e -> {
            if (paginaAtual < totalPaginas - 1) {
                paginaAtual++;
                renderizarPagina();
            }
        });

        // Salva o progresso ao fechar
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                livro.setUltimaPagina(paginaAtual);

                // Agora o erro some, pois listaParaSalvar existe aqui!
                Salvar.salvarDados(listaParaSalvar, "lendo.json");

                fecharPDF();
            }
        });
    }

    private void carregarPDF() {
        try {
            documento = Loader.loadPDF(new File(livro.getPathPDF()));
            renderer = new PDFRenderer(documento);
            totalPaginas = documento.getNumberOfPages();

            // Garante que a página inicial é válida
            if (paginaAtual >= totalPaginas) paginaAtual = 0;

            renderizarPagina();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir o PDF: " + e.getMessage());
        }
    }

    private void renderizarPagina() {
        try {
            // Renderiza a página em alta qualidade (DPI 150)
            BufferedImage imagem = renderer.renderImageWithDPI(paginaAtual, 150);
            lblImagem.setIcon(new ImageIcon(imagem));
            lblPagina.setText("Página " + (paginaAtual + 1) + " de " + totalPaginas);
            scrollPane.getVerticalScrollBar().setValue(0);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao renderizar página: " + e.getMessage());
        }
    }

    private void fecharPDF() {
        try {
            if (documento != null) documento.close();
        } catch (IOException e) {
            System.out.println("Erro ao fechar PDF: " + e.getMessage());
        }
    }

    private void estilizarBotao(JButton botao) {
        botao.setBackground(new Color(60, 60, 60));
        botao.setForeground(Color.WHITE);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}