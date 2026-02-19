package Cadastro;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class AddLivro extends JFrame {

    // Componentes da Interface
    private JTextField txtTitulo, txtAutor, txtPaginas;
    private JTextArea txtSinopse;
    private JComboBox<String> comboStatus;
    private JLabel lblCapa;

    // Variáveis de Controle
    private String pathPDFOrigem = "";
    private String caminhoCapaSalva = "";
    private Tela telaPrincipal;

    public AddLivro(Tela telaPrincipal) {
        this.telaPrincipal = telaPrincipal;
        configurarLayout();

        // Inicia a seleção do arquivo logo ao abrir
        SwingUtilities.invokeLater(this::selecionarArquivoPDF);
    }

    private void configurarLayout() {
        setTitle("Novo Cadastro de Livro");
        setSize(750, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Painel Esquerdo: Capa
        JPanel painelCapa = new JPanel(new BorderLayout());
        painelCapa.setPreferredSize(new Dimension(280, 0));
        painelCapa.setBackground(new Color(40, 40, 40));
        lblCapa = new JLabel("Buscando Capa...", SwingConstants.CENTER);
        lblCapa.setForeground(Color.LIGHT_GRAY);
        painelCapa.add(lblCapa, BorderLayout.CENTER);

        // Painel Direito: Campos
        JPanel painelDireito = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);

        txtTitulo = new JTextField();
        txtAutor = new JTextField();
        txtPaginas = new JTextField();
        comboStatus = new JComboBox<>(new String[]{"Lendo", "Quero Ler", "Lido"});

        adicionarCampo(painelDireito, gbc, "Título:", txtTitulo, 0);
        adicionarCampo(painelDireito, gbc, "Autor:", txtAutor, 1);
        adicionarCampo(painelDireito, gbc, "Páginas:", txtPaginas, 2);
        adicionarCampo(painelDireito, gbc, "Status:", comboStatus, 3);

        // Botão de Busca Manual (Correção para abrir o SearchBooks)
        JButton btnBuscarManual = new JButton(" Não é este? Buscar Manual");
        btnBuscarManual.setIcon(new FlatSVGIcon("icons/search.svg", 16, 16));
        btnBuscarManual.addActionListener(e -> {
            SearchBooks buscador = new SearchBooks(this);
            buscador.setVisible(true);
        });
        gbc.gridy = 4; gbc.gridx = 1;
        painelDireito.add(btnBuscarManual, gbc);

        // Sinopse (Centro)
        txtSinopse = new JTextArea();
        txtSinopse.setLineWrap(true);
        txtSinopse.setWrapStyleWord(true);
        JScrollPane scrollSinopse = new JScrollPane(txtSinopse);
        scrollSinopse.setBorder(BorderFactory.createTitledBorder("Sinopse / Descrição"));

        // Botão Salvar (Rodapé)
        JButton btnSalvar = new JButton(" CONFIRMAR E SALVAR");
        btnSalvar.setBackground(new Color(46, 204, 113));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSalvar.setPreferredSize(new Dimension(0, 60));
        btnSalvar.addActionListener(e -> salvarLivro());

        add(painelCapa, BorderLayout.WEST);
        add(painelDireito, BorderLayout.NORTH);
        add(scrollSinopse, BorderLayout.CENTER);
        add(btnSalvar, BorderLayout.SOUTH);
    }

    private void adicionarCampo(JPanel p, GridBagConstraints gbc, String label, Component c, int y) {
        gbc.gridy = y; gbc.gridx = 0; gbc.weightx = 0;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        p.add(c, gbc);
    }

    private void selecionarArquivoPDF() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fc.getSelectedFile();
            this.pathPDFOrigem = arquivo.getAbsolutePath();
            carregarDadosDoPDF(arquivo);
        } else {
            dispose();
        }
    }

    private void carregarDadosDoPDF(File arquivo) {
        try (PDDocument document = Loader.loadPDF(arquivo)) {
            var info = document.getDocumentInformation();

            // 1. Puxa dados básicos do PDF
            String tituloPDF = info.getTitle() != null ? info.getTitle() : arquivo.getName().replace(".pdf", "");
            String autorPDF = info.getAuthor() != null ? info.getAuthor() : "Desconhecido";

            txtTitulo.setText(tituloPDF);
            txtAutor.setText(autorPDF);
            txtPaginas.setText(String.valueOf(document.getNumberOfPages()));

            // 2. Tenta pegar a sinopse do PDF (Subject)
            String sinopsePDF = info.getSubject();

            if (sinopsePDF != null && !sinopsePDF.trim().isEmpty()) {
                txtSinopse.setText(sinopsePDF);
            } else {
                // 3. Se o PDF não tem sinopse, busca no Google Books AUTOMATICAMENTE
                txtSinopse.setText("Buscando sinopse no Google Books...");

                // Rodamos em uma thread separada para não travar a interface enquanto baixa
                new Thread(() -> {
                    String[] infoExtra = LivroService.buscarInfoExtra(tituloPDF);
                    String biografiaGoogle = infoExtra[0];

                    SwingUtilities.invokeLater(() -> {
                        if (biografiaGoogle != null && !biografiaGoogle.isEmpty()) {
                            txtSinopse.setText(biografiaGoogle);
                        } else {
                            txtSinopse.setText("Sinopse não encontrada. Digite manualmente aqui.");
                        }
                    });
                }).start();
            }

            // --- Renderizar Capa (Mantendo seu código original) ---
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage bim = renderer.renderImageWithDPI(0, 100);
            File pasta = new File("resources/capas/");
            if (!pasta.exists()) pasta.mkdirs();
            caminhoCapaSalva = "resources/capas/" + System.currentTimeMillis() + ".png";
            ImageIO.write(bim, "png", new File(caminhoCapaSalva));
            Image dimg = bim.getScaledInstance(220, 300, Image.SCALE_SMOOTH);
            lblCapa.setIcon(new ImageIcon(dimg));
            lblCapa.setText("");

        } catch (Exception e) {
            e.printStackTrace();
            lblCapa.setText("Erro ao extrair dados");
        }
    }

    // ESTE MÉTODO RESOLVE O ERRO DA IMAGEM 40b079.png
    public void preencherCamposManualmente(Cadastro selecionado) {
        txtTitulo.setText(selecionado.getNomeDoLivro());
        txtAutor.setText(selecionado.getAutor());
        txtSinopse.setText(selecionado.getBiografia()); // Verifique se o nome é txtSinopseModern ou txtSinopse
        txtPaginas.setText(String.valueOf(selecionado.getQtdPag()));
    }

    private void salvarLivro() {
        Cadastro novoLivro = new Cadastro();
        novoLivro.setNomeDoLivro(txtTitulo.getText());
        novoLivro.setAutor(txtAutor.getText());

        // ESTA LINHA É A QUE PUXA A SINOPSE PARA O OBJETO
        novoLivro.setBiografia(txtSinopse.getText());

        novoLivro.setPathPDF(pathPDFOrigem);
        novoLivro.setPathCapa(caminhoCapaSalva);

        try {
            novoLivro.setQtdPag(Integer.parseInt(txtPaginas.getText()));
        } catch (Exception e) {
            novoLivro.setQtdPag(0);
        }

        // Adiciona na lista correta e salva
        String status = (String) comboStatus.getSelectedItem();
        if (status.equals("Lendo")) {
            telaPrincipal.getLivrosLendo().add(novoLivro);
        } else if (status.equals("Quero Ler")) {
            telaPrincipal.getLivrosParaLer().add(novoLivro);
        } else {
            telaPrincipal.getLivrosLidos().add(novoLivro);
        }

        telaPrincipal.salvarEAtualizarTudo();
        dispose();
    }
}