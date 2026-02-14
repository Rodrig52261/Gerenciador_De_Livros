package Cadastro;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.Loader;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class AddLivro extends JDialog {

    private JTextField txtNome, txtAutor, txtPaginas;
    private JTextArea txtBio;
    private JComboBox<String> comboCategoria;
    private String pathPDF = "";
    private Map<String, DefaultListModel<Cadastro>> modelos;
    private final Color COR_INPUT = new Color(45, 45, 48);
    private final Color COR_DESTAQUE = new Color(0, 120, 215);

    public AddLivro(JFrame parent, Map<String, DefaultListModel<Cadastro>> modelos) {
        super(parent, "Adicionar à Biblioteca", true);
        this.modelos = modelos;

        setSize(500, 750);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(new Color(30, 30, 30));
        setLayout(new BorderLayout());

        // --- CABEÇALHO ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(37, 37, 38));
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblTitulo = new JLabel("Novo Cadastro");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);

        JButton btnPDF = new JButton("Importar PDF 📄");
        estilizarBotao(btnPDF, COR_DESTAQUE);
        btnPDF.addActionListener(e -> selecionarEProcessarPDF());

        header.add(lblTitulo, BorderLayout.WEST);
        header.add(btnPDF, BorderLayout.EAST);

        // --- CORPO (FORMULÁRIO) ---
        JPanel corpo = new JPanel();
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        corpo.setBackground(new Color(30, 30, 30));
        corpo.setBorder(new EmptyBorder(10, 25, 20, 25));

        txtNome = criarCampoModerno(corpo, "Título do Livro");
        txtAutor = criarCampoModerno(corpo, "Autor");
        txtPaginas = criarCampoModerno(corpo, "Total de Páginas");

        corpo.add(criarLabel("Mover para:"));
        comboCategoria = new JComboBox<>(new String[]{"Lendo", "Quero Ler", "Lidos"});
        comboCategoria.setBackground(COR_INPUT);
        comboCategoria.setForeground(Color.WHITE);
        comboCategoria.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboCategoria.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        comboCategoria.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        corpo.add(comboCategoria);
        corpo.add(Box.createRigidArea(new Dimension(0, 15)));

        corpo.add(criarLabel("Sinopse / Biografia"));
        txtBio = new JTextArea(6, 20);
        txtBio.setLineWrap(true);
        txtBio.setWrapStyleWord(true);
        txtBio.setBackground(COR_INPUT);
        txtBio.setForeground(new Color(200, 200, 200));
        txtBio.setCaretColor(Color.WHITE);
        txtBio.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        corpo.add(new JScrollPane(txtBio));

        // --- RODAPÉ ---
        JButton btnSalvar = new JButton("Confirmar e Salvar");
        estilizarBotao(btnSalvar, new Color(38, 162, 105));
        btnSalvar.setPreferredSize(new Dimension(0, 60));
        btnSalvar.addActionListener(e -> executarSalvamento());

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(corpo), BorderLayout.CENTER);
        add(btnSalvar, BorderLayout.SOUTH);
    }

    private JTextField criarCampoModerno(JPanel p, String label) {
        p.add(criarLabel(label));
        JTextField t = new JTextField();
        t.setBackground(COR_INPUT);
        t.setForeground(Color.WHITE);
        t.setCaretColor(Color.WHITE);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        t.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        p.add(t);
        p.add(Box.createRigidArea(new Dimension(0, 15)));
        return t;
    }

    private JLabel criarLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(new Color(180, 180, 180));
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setBorder(new EmptyBorder(0, 0, 5, 0));
        return l;
    }

    private void estilizarBotao(JButton b, Color cor) {
        b.setBackground(cor);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void selecionarEProcessarPDF() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = chooser.getSelectedFile();
            this.pathPDF = arquivo.getAbsolutePath();
            try (PDDocument doc = Loader.loadPDF(arquivo)) {
                PDDocumentInformation info = doc.getDocumentInformation();
                String titulo = (info.getTitle() != null && !info.getTitle().isEmpty())
                        ? info.getTitle() : arquivo.getName().replace(".pdf", "");

                txtNome.setText(titulo);
                txtAutor.setText(info.getAuthor() != null ? info.getAuthor() : "");
                txtPaginas.setText(String.valueOf(doc.getNumberOfPages()));

                new Thread(() -> {
                    String[] infoExtra = LivroService.buscarInfoExtra(titulo);
                    SwingUtilities.invokeLater(() -> txtBio.setText(infoExtra[0]));
                }).start();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void executarSalvamento() {
        String selecionado = comboCategoria.getSelectedItem().toString();

        // 1. Gera o ID da categoria (ex: "Quero Ler" -> "queroler")
        String catId = selecionado.toLowerCase().replace(" ", "");

        // 2. Normalização: O seu sistema usa "querolar" no modelosMap e no JSON
        if (catId.equals("queroler")) {
            catId = "querolar";
        }

        // 3. Busca o modelo correto
        DefaultListModel<Cadastro> modeloAlvo = modelos.get(catId);

        // DEBUG no console para te ajudar se falhar
        if (modeloAlvo == null) {
            System.out.println("ERRO: Tentou buscar '" + catId + "' mas no mapa só tem: " + modelos.keySet());
        }

        if (modeloAlvo != null && !txtNome.getText().trim().isEmpty()) {
            try {
                Cadastro n = new Cadastro();
                n.setNomeDoLivro(txtNome.getText().trim());
                n.setAutor(txtAutor.getText().trim());

                String paginasTexto = txtPaginas.getText().replaceAll("\\D", "");
                n.setQtdPag(paginasTexto.isEmpty() ? 0 : Integer.parseInt(paginasTexto));

                n.setBiografia(txtBio.getText());
                n.setPathPDF(pathPDF);
                n.setUltimaPagina(0);

                // Adiciona na interface
                modeloAlvo.addElement(n);

                // Salva no JSON
                String caminhoArquivo = "dados/" + catId + ".json";
                ArrayList<Cadastro> listaAtual = Salvar.carregarDados(caminhoArquivo);
                listaAtual.add(n);
                Salvar.salvarDados(listaAtual, caminhoArquivo);

                JOptionPane.showMessageDialog(this, "Livro salvo em: " + selecionado);
                dispose();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Erro: Categoria '" + catId + "' não encontrada ou título vazio.");
        }
    }
}