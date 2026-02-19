package Cadastro;

import org.apiguardian.api.API;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Tela extends JFrame {

    // Cores e Estilo
    private final Color COR_FUNDO = new Color(30, 30, 30);
    private final Color COR_PAINEL = new Color(45, 45, 45);
    private final Color COR_MENU = new Color(25, 25, 25);
    private final Color COR_TEXTO = new Color(220, 220, 220);
    private final Color COR_DESTAQUE = new Color(88, 166, 255);

    // Componentes Globais
    private JPanel painelCartoes;
    private CardLayout cardLayout;
    private String categoriaAtiva = "lendo";
    private Map<String, DefaultListModel<Cadastro>> modelosMap = new HashMap<>();
    private Cadastro livroSelecionadoAtual;

    // Componentes de Detalhes
    private JLabel lblTituloInfo, lblAutorDestaque, lblCapa;
    private JProgressBar barraProgresso;
    private JTextArea txtSinopseModern;
    private JButton btnAbrir, btnConcluir, btnDelete;

    private ArrayList<Cadastro> livrosLidos, livrosLendo, livrosParaLer;
    private final String PASTA_DADOS = "dados/";
    private DefaultListModel<Cadastro> livrosQueroLer = new DefaultListModel<>();

    // Getters para permitir que o AddLivro adicione livros nas listas
    public ArrayList<Cadastro> getLivrosLendo() { return livrosLendo; }
    public ArrayList<Cadastro> getLivrosLidos() { return livrosLidos; }
    public ArrayList<Cadastro> getLivrosQueroLer() { return livrosParaLer; }

    public Tela() {
        setTitle("Gerenciador de Leitura Pessoal");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        inicializarDados();
        configurarComponentesDestaque();

        // Menu Lateral
        add(criarMenuLateral(), BorderLayout.WEST);

        // Painel Central: Lista + Detalhes
        JPanel painelCentral = new JPanel(new BorderLayout());

        cardLayout = new CardLayout();
        painelCartoes = new JPanel(cardLayout);
        painelCartoes.setPreferredSize(new Dimension(320, 0));

        painelCartoes.add(criarPainelLista(livrosLendo, "lendo"), "lendo");
        painelCartoes.add(criarPainelLista(livrosParaLer, "querolar"), "querolar");
        painelCartoes.add(criarPainelLista(livrosLidos, "lidos"), "lidos");

        painelCentral.add(painelCartoes, BorderLayout.WEST);
        painelCentral.add(montarPainelDetalhesFixo(), BorderLayout.CENTER);

        add(painelCentral, BorderLayout.CENTER);
        cardLayout.show(painelCartoes, "lendo");
    }

    private void configurarComponentesDestaque() {
        lblTituloInfo = new JLabel("Selecione um livro");
        lblTituloInfo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTituloInfo.setForeground(COR_DESTAQUE);
        lblTituloInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblAutorDestaque = new JLabel(" ");
        lblAutorDestaque.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        lblAutorDestaque.setForeground(Color.GRAY);
        lblAutorDestaque.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblCapa = new JLabel("Sem Capa");
        lblCapa.setPreferredSize(new Dimension(220, 310));
        lblCapa.setMaximumSize(new Dimension(220, 310));
        lblCapa.setBackground(new Color(40, 40, 40));
        lblCapa.setOpaque(true);
        lblCapa.setHorizontalAlignment(SwingConstants.CENTER);
        lblCapa.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        lblCapa.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtSinopseModern = new JTextArea();
        txtSinopseModern.setLineWrap(true);
        txtSinopseModern.setWrapStyleWord(true);
        txtSinopseModern.setEditable(false);
        txtSinopseModern.setBackground(COR_PAINEL);
        txtSinopseModern.setForeground(COR_TEXTO);
        txtSinopseModern.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        barraProgresso = new JProgressBar(0, 100);
        barraProgresso.setMaximumSize(new Dimension(400, 25));
        barraProgresso.setForeground(COR_DESTAQUE);
        barraProgresso.setAlignmentX(Component.CENTER_ALIGNMENT);
        barraProgresso.setVisible(false);

        // Botões
        btnAbrir = criarBotaoEstilizado("Abrir Livro 📖", COR_DESTAQUE);
        btnConcluir = criarBotaoEstilizado("Concluir ✓", new Color(46, 204, 113));
        btnDelete = criarBotaoEstilizado("Excluir 🗑", new Color(231, 76, 60));

        // Ações
        btnAbrir.addActionListener(e -> {
            if (livroSelecionadoAtual != null) {
                if (categoriaAtiva.equals("querolar")) moverParaLendo(livroSelecionadoAtual);
                abrirPDF();
            }
        });
        btnConcluir.addActionListener(e -> concluirLivro());
        btnDelete.addActionListener(e -> excluirLivro());
    }

    // Na classe Tela.java
    public void salvarEAtualizarTudo() {
        // 1. Salva os arquivos JSON usando a sua classe Salvar
        Salvar.salvarDados(livrosLendo, PASTA_DADOS + "lendo.json");
        Salvar.salvarDados(livrosParaLer, PASTA_DADOS + "querolar.json");
        Salvar.salvarDados(livrosLidos, PASTA_DADOS + "lidos.json");

        // 2. Atualiza os modelos visuais (o que aparece nas listas da tela)
        atualizarModelo(modelosMap.get("lendo"), livrosLendo);
        atualizarModelo(modelosMap.get("querolar"), livrosParaLer);
        atualizarModelo(modelosMap.get("lidos"), livrosLidos);

        // 3. Força a interface a se redesenhar
        repaint();
        revalidate();
    }

    private void atualizarModelo(DefaultListModel<Cadastro> modelo, ArrayList<Cadastro> lista) {
        if (modelo != null && lista != null) {
            modelo.clear();
            for (Cadastro c : lista) {
                modelo.addElement(c);
            }
        }
    }

    public ImageIcon redimensionarIcone(String path, int w, int h) {
        try {
            Image img = null;
            if (path != null && path.startsWith("http")) {
                java.net.URL url = new java.net.URL(path);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                img = javax.imageio.ImageIO.read(conn.getInputStream());
            } else if (path != null && !path.isEmpty()) {
                img = new ImageIcon(path).getImage();
            }
            if (img == null) return null;
            return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return null;
        }
    }

    private JScrollPane criarPainelLista(ArrayList<Cadastro> lista, String id) {
        DefaultListModel<Cadastro> modelo = new DefaultListModel<>();
        lista.forEach(modelo::addElement);
        modelosMap.put(id, modelo);

        JList<Cadastro> jlist = new JList<>(modelo);
        jlist.setBackground(COR_PAINEL);
        jlist.setForeground(COR_TEXTO);
        jlist.setFixedCellHeight(55);
        jlist.setSelectionBackground(COR_DESTAQUE);

        jlist.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                livroSelecionadoAtual = jlist.getSelectedValue();
                if (livroSelecionadoAtual != null) renderizarInterface();
            }
        });

        JScrollPane sp = new JScrollPane(jlist);
        sp.setBorder(null);
        return sp;
    }

    private JPanel montarPainelDetalhesFixo() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(COR_FUNDO);
        p.setBorder(new EmptyBorder(40, 50, 40, 50));

        JScrollPane scrollSin = new JScrollPane(txtSinopseModern);
        scrollSin.setOpaque(false);
        scrollSin.getViewport().setOpaque(false);
        scrollSin.setBorder(null);

        JPanel pBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pBotoes.setOpaque(false);
        pBotoes.add(btnAbrir); pBotoes.add(btnConcluir); pBotoes.add(btnDelete);

        p.add(lblCapa);
        p.add(Box.createRigidArea(new Dimension(0, 20)));
        p.add(lblTituloInfo);
        p.add(lblAutorDestaque);
        p.add(Box.createRigidArea(new Dimension(0, 25)));
        p.add(barraProgresso);
        p.add(Box.createRigidArea(new Dimension(0, 25)));
        p.add(scrollSin);
        p.add(Box.createRigidArea(new Dimension(0, 30)));
        p.add(pBotoes);

        return p;
    }

    private void renderizarInterface() {
        lblTituloInfo.setText(livroSelecionadoAtual.getNomeDoLivro());
        lblAutorDestaque.setText("por " + livroSelecionadoAtual.getAutor());
        txtSinopseModern.setText(livroSelecionadoAtual.getBiografia());
        lblCapa.setIcon(redimensionarIcone(livroSelecionadoAtual.getPathCapa(), 220, 310));
        lblCapa.setText(lblCapa.getIcon() == null ? "Sem Capa" : "");

        barraProgresso.setVisible(!categoriaAtiva.equals("querolar"));
        if (barraProgresso.isVisible()) barraProgresso.setValue(livroSelecionadoAtual.calcularPorcentagem());

        btnAbrir.setVisible(true);
        btnConcluir.setVisible(!categoriaAtiva.equals("lidos"));
        btnDelete.setVisible(true);
    }

    private void concluirLivro() {
        if (livroSelecionadoAtual == null) return;
        livrosLendo.remove(livroSelecionadoAtual);
        if (!livrosLidos.contains(livroSelecionadoAtual)) livrosLidos.add(livroSelecionadoAtual);
        salvarEAtualizarTudo();
        limparInterface();
    }

    private void excluirLivro() {
        if (livroSelecionadoAtual == null) return;
        if (JOptionPane.showConfirmDialog(this, "Excluir livro?") == JOptionPane.YES_OPTION) {
            livrosLendo.remove(livroSelecionadoAtual);
            livrosParaLer.remove(livroSelecionadoAtual);
            livrosLidos.remove(livroSelecionadoAtual);
            salvarEAtualizarTudo();
            limparInterface();
        }
    }

    private void moverParaLendo(Cadastro livro) {
        livrosParaLer.remove(livro);
        if (!livrosLendo.contains(livro)) livrosLendo.add(livro);
        salvarEAtualizarTudo();
        cardLayout.show(painelCartoes, "lendo");
        categoriaAtiva = "lendo";
    }

    private void abrirPDF() {
        executaPDF v = new executaPDF(livroSelecionadoAtual, livrosLendo);
        v.setVisible(true);
        v.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { renderizarInterface(); }
        });
    }

    private void limparInterface() {
        livroSelecionadoAtual = null;
        lblTituloInfo.setText("Selecione um livro");
        lblAutorDestaque.setText(" ");
        lblCapa.setIcon(null); lblCapa.setText("Sem Capa");
        txtSinopseModern.setText("");
        barraProgresso.setVisible(false);
        btnAbrir.setVisible(false); btnConcluir.setVisible(false); btnDelete.setVisible(false);
    }

    private void inicializarDados() {
        File f = new File(PASTA_DADOS); if (!f.exists()) f.mkdirs();
        livrosLidos = Salvar.carregarDados(PASTA_DADOS + "lidos.json");
        livrosLendo = Salvar.carregarDados(PASTA_DADOS + "lendo.json");
        livrosParaLer = Salvar.carregarDados(PASTA_DADOS + "querolar.json");
    }

    private JPanel criarMenuLateral() {
        JPanel m = new JPanel();
        m.setLayout(new BoxLayout(m, BoxLayout.Y_AXIS));
        m.setBackground(COR_MENU);
        m.setPreferredSize(new Dimension(230, 0));
        m.setBorder(new EmptyBorder(30, 20, 30, 20));

        JButton btnNovo = criarBotaoMenu(" + Novo Livro", "");
        btnNovo.setBackground(COR_DESTAQUE); btnNovo.setOpaque(true);
        btnNovo.addActionListener(e -> new AddLivro(this, modelosMap).setVisible(true));

        m.add(btnNovo);
        m.add(Box.createRigidArea(new Dimension(0, 40)));
        m.add(criarBotaoMenu("Lendo", "lendo"));
        m.add(Box.createRigidArea(new Dimension(0, 15)));
        m.add(criarBotaoMenu("Quero Ler", "querolar"));
        m.add(Box.createRigidArea(new Dimension(0, 15)));
        m.add(criarBotaoMenu("Lidos", "lidos"));

        return m;
    }

    private JButton criarBotaoMenu(String txt, String id) {
        JButton b = new JButton(txt);
        b.setForeground(COR_TEXTO); b.setBackground(COR_MENU);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false); b.setBorder(new EmptyBorder(0, 15, 0, 15));
        if (!id.isEmpty()) b.addActionListener(e -> {
            cardLayout.show(painelCartoes, id);
            categoriaAtiva = id;
            limparInterface();
        });
        return b;
    }

    private JButton criarBotaoEstilizado(String t, Color c) {
        JButton b = new JButton(t);
        b.setBackground(c); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        return b;
    }
}