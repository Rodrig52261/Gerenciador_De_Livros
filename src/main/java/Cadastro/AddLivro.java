package Cadastro;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

public class AddLivro extends JFrame {

    private JTextField txtTitulo, txtAutor, txtPaginas;
    private JTextArea txtSinopse;
    private JComboBox<String> comboStatus; // Nova caixa de seleção
    private JLabel lblCapa;
    private String pathCapaTemporaria = "";
    private String pathPDFOrigem = "";
    private Tela telaPrincipal;

    public AddLivro(Tela telaPrincipal, Map<String, DefaultListModel<Cadastro>> modelosMap) {
        this.telaPrincipal = telaPrincipal;
        configurarJanela();

        // Painel da capa
        JPanel painelCapa = new JPanel(new BorderLayout());
        painelCapa.setBackground(new Color(30, 30, 30));
        painelCapa.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        painelCapa.setPreferredSize(new Dimension(200, 0));

        lblCapa = new JLabel("Buscando capa...", SwingConstants.CENTER);
        lblCapa.setForeground(Color.GRAY);
        lblCapa.setBackground(new Color(45, 45, 45));
        lblCapa.setOpaque(true);
        lblCapa.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        painelCapa.add(lblCapa, BorderLayout.CENTER);

        // Painel Formulário
        JPanel painelDireita = new JPanel(new BorderLayout());
        painelDireita.setBackground(new Color(30, 30, 30));

        JPanel painelCampos = new JPanel(new GridBagLayout());
        painelCampos.setBackground(new Color(30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);

        txtTitulo = criarCampo();
        txtAutor = criarCampo();
        txtPaginas = criarCampo();

        // Configurando a seleção de Status
        String[] statusOpcoes = {"Lendo", "Quero Ler", "Lido"};
        comboStatus = new JComboBox<>(statusOpcoes);
        comboStatus.setBackground(new Color(50, 50, 50));
        comboStatus.setForeground(Color.WHITE);
        ((JLabel)comboStatus.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        adicionarComponente(painelCampos, gbc, "Título:", txtTitulo, 0);
        adicionarComponente(painelCampos, gbc, "Autor:", txtAutor, 1);
        adicionarComponente(painelCampos, gbc, "Páginas:", txtPaginas, 2);
        adicionarComponente(painelCampos, gbc, "Status:", comboStatus, 3);

        JButton btnRefinar = new JButton("Não é este? Buscar outras opções 🔍");
        btnRefinar.setBackground(new Color(40, 40, 40));
        btnRefinar.setForeground(Color.CYAN);
        gbc.gridy = 4; gbc.gridx = 1;
        painelCampos.add(btnRefinar, gbc);

        txtSinopse = new JTextArea(15, 20);
        txtSinopse.setLineWrap(true);
        txtSinopse.setWrapStyleWord(true);
        txtSinopse.setBackground(new Color(45, 45, 45));
        txtSinopse.setForeground(Color.WHITE);
        txtSinopse.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scroll = new JScrollPane(txtSinopse);
        scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Sinopse", 0, 0, null, Color.WHITE));

        painelDireita.add(painelCampos, BorderLayout.NORTH);
        painelDireita.add(scroll, BorderLayout.CENTER);

        JButton btnSalvar = new JButton("CONFIRMAR E SALVAR ✅");
        btnSalvar.setPreferredSize(new Dimension(0, 60));
        btnSalvar.setBackground(new Color(46, 204, 113));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 14));

        add(painelCapa, BorderLayout.WEST);
        add(painelDireita, BorderLayout.CENTER);
        add(btnSalvar, BorderLayout.SOUTH);

        btnRefinar.addActionListener(e -> {
            SearchBooks buscador = new SearchBooks(this);
            buscador.setVisible(true);
        });

        btnSalvar.addActionListener(e -> salvarLivro());

        selecionarPDF();
    }

    public void preencherCamposManualmente(Cadastro selecionado) {
        txtTitulo.setText(selecionado.getNomeDoLivro());
        txtAutor.setText(selecionado.getAutor());
        txtSinopse.setText(selecionado.getBiografia());
        txtPaginas.setText(String.valueOf(selecionado.getQtdPag()));
        this.pathCapaTemporaria = selecionado.getPathCapa();
        carregarCapa(pathCapaTemporaria); // Chama sua função de carregar imagem
    }

    private void salvarLivro() {
        Cadastro c = new Cadastro();
        c.setNomeDoLivro(txtTitulo.getText());
        c.setAutor(txtAutor.getText());
        c.setBiografia(txtSinopse.getText());
        c.setPathCapa(pathCapaTemporaria);
        c.setPathPDF(pathPDFOrigem);

        try {
            c.setQtdPag(Integer.parseInt(txtPaginas.getText()));
        } catch(Exception ex) {
            c.setQtdPag(0);
        }

        // Pega o status selecionado no ComboBox
        String status = (String) comboStatus.getSelectedItem();

        if ("Lendo".equals(status)) {
            telaPrincipal.getLivrosLendo().add(c);
        } else if ("Quero Ler".equals(status)) {
            telaPrincipal.getLivrosQueroLer().add(c); // Agora ele vai cair no livrosParaLer corretamente
        } else {
            telaPrincipal.getLivrosLidos().add(c);
        }

        telaPrincipal.salvarEAtualizarTudo();
        dispose();
    }

    // --- MÉTODOS DE BUSCA E EXTRAÇÃO (IGUAIS AOS ANTERIORES PORÉM REVISADOS) ---

    private void realizarBusca(String termo) {
        new Thread(() -> {
            try {
                String termoLimpo = termo.replaceAll("\\(.*?\\)", "").replace("_", " ").trim();
                String q = URLEncoder.encode(termoLimpo, "UTF-8");
                URL url = new URL("https://www.googleapis.com/books/v1/volumes?q=" + q + "&maxResults=5&langRestrict=pt");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder(); String linha;
                while ((linha = rd.readLine()) != null) sb.append(linha);

                ArrayList<Cadastro> resultados = processarVarios(sb.toString());

                SwingUtilities.invokeLater(() -> {
                    if (!resultados.isEmpty()) {
                        Cadastro melhor = resultados.get(0);
                        txtTitulo.setText(melhor.getNomeDoLivro());
                        txtAutor.setText(melhor.getAutor());
                        txtSinopse.setText(melhor.getBiografia());
                        pathCapaTemporaria = melhor.getPathCapa();
                        carregarCapa(pathCapaTemporaria);
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private ArrayList<Cadastro> processarVarios(String json) {
        ArrayList<Cadastro> lista = new ArrayList<>();
        try {
            int pos = 0;
            while ((pos = json.indexOf("\"volumeInfo\":", pos)) != -1 && lista.size() < 5) {
                int fimItem = json.indexOf("\"saleInfo\"", pos);
                if (fimItem == -1) fimItem = Math.min(pos + 3500, json.length());
                String trecho = json.substring(pos, fimItem);

                Cadastro c = new Cadastro();
                c.setNomeDoLivro(extrairValor(trecho, "\"title\": \""));
                c.setAutor(extrairValor(trecho, "\"authors\": [ \"", "\"authors\": [\n          \""));
                c.setBiografia(extrairValor(trecho, "\"description\": \""));
                c.setPathCapa(extrairValor(trecho, "\"thumbnail\": \"").replace("http:", "https:"));

                try {
                    int pIdx = trecho.indexOf("\"pageCount\":");
                    if (pIdx != -1) {
                        String num = trecho.substring(pIdx + 12).split("[,}]")[0].trim();
                        c.setQtdPag(Integer.parseInt(num));
                    }
                } catch (Exception e) {}

                if (!c.getNomeDoLivro().isEmpty()) lista.add(c);
                pos += 20;
            }
        } catch (Exception e) {}
        return lista;
    }

    private String extrairValor(String trecho, String... chaves) {
        for (String chave : chaves) {
            if (trecho.contains(chave)) {
                int start = trecho.indexOf(chave) + chave.length();
                int end = trecho.indexOf("\"", start);
                return trecho.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"");
            }
        }
        return "";
    }

    private void carregarCapa(String urlOuPath) {
        if (urlOuPath.isEmpty()) return;
        new Thread(() -> {
            try {
                Image img = ImageIO.read(new URL(urlOuPath));
                Image scaled = img.getScaledInstance(180, 260, Image.SCALE_SMOOTH);
                SwingUtilities.invokeLater(() -> {
                    lblCapa.setIcon(new ImageIcon(scaled));
                    lblCapa.setText("");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> lblCapa.setText("Sem Capa"));
            }
        }).start();
    }

    private void selecionarPDF() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            pathPDFOrigem = fc.getSelectedFile().getAbsolutePath();
            try (org.apache.pdfbox.pdmodel.PDDocument doc = org.apache.pdfbox.Loader.loadPDF(fc.getSelectedFile())) {
                org.apache.pdfbox.pdmodel.PDDocumentInformation info = doc.getDocumentInformation();
                String t = (info.getTitle() != null && !info.getTitle().isEmpty()) ? info.getTitle() : fc.getSelectedFile().getName().replace(".pdf", "");
                String a = (info.getAuthor() != null) ? info.getAuthor() : "";

                txtTitulo.setText(t);
                txtAutor.setText(a);
                txtPaginas.setText(String.valueOf(doc.getNumberOfPages()));
                realizarBusca(t);
            } catch (Exception e) { realizarBusca(fc.getSelectedFile().getName().replace(".pdf", "")); }
        } else { dispose(); }
    }

    private void configurarJanela() {
        setSize(750, 750); setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(30, 30, 30)); setLayout(new BorderLayout());
    }

    private JTextField criarCampo() {
        JTextField f = new JTextField(); f.setBackground(new Color(50, 50, 50));
        f.setForeground(Color.WHITE); f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)); return f;
    }

    private void adicionarComponente(JPanel p, GridBagConstraints gbc, String l, Component c, int y) {
        gbc.gridy = y; gbc.gridx = 0; p.add(new JLabel("<html><font color='white'>"+l+"</font></html>"), gbc);
        gbc.gridx = 1; p.add(c, gbc);
    }
}