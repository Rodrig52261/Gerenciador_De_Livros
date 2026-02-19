package Cadastro;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Tela extends JFrame {

    private final Color COR_FUNDO = new Color(30, 30, 30);
    private final Color COR_PAINEL = new Color(45, 45, 45);
    private final Color COR_MENU = new Color(25, 25, 25);
    private final Color COR_TEXTO = new Color(220, 220, 220);
    private final Color COR_DESTAQUE = new Color(88, 166, 255);

    private JPanel painelCartoes;
    private CardLayout cardLayout;
    private Map<String, DefaultListModel<Cadastro>> modelosMap = new HashMap<>();
    private Cadastro livroSelecionadoAtual;

    private JLabel lblTituloInfo, lblAutorDestaque, lblCapa;
    private JProgressBar barraProgresso;
    private JTextArea txtSinopseModern;
    private JButton btnAbrir, btnConcluir, btnDelete;

    private ArrayList<Cadastro> livrosLidos, livrosLendo, livrosParaLer;
    private final String PASTA_DADOS = "resources/dados/";

    public ArrayList<Cadastro> getLivrosLendo() { return livrosLendo; }
    public ArrayList<Cadastro> getLivrosLidos() { return livrosLidos; }
    public ArrayList<Cadastro> getLivrosParaLer() { return livrosParaLer; }

    public Tela() {
        setTitle("Gerenciador de Leitura - 2026");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        inicializarDados();
        configurarComponentesDestaque();

        add(criarMenuLateral(), BorderLayout.WEST);

        JPanel painelCentral = new JPanel(new BorderLayout());
        cardLayout = new CardLayout();
        painelCartoes = new JPanel(cardLayout);

        painelCartoes.add(criarPainelLista(livrosLendo, "lendo"), "lendo");
        painelCartoes.add(criarPainelLista(livrosParaLer, "querolar"), "querolar");
        painelCartoes.add(criarPainelLista(livrosLidos, "lidos"), "lidos");

        painelCentral.add(painelCartoes, BorderLayout.WEST);
        painelCentral.add(montarPainelDetalhesFixo(), BorderLayout.CENTER);

        add(painelCentral, BorderLayout.CENTER);
        cardLayout.show(painelCartoes, "lendo");
    }

    private void configurarComponentesDestaque() {
        lblTituloInfo = new JLabel("Selecione um livro", SwingConstants.CENTER);
        lblTituloInfo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTituloInfo.setForeground(COR_DESTAQUE);
        lblTituloInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblAutorDestaque = new JLabel(" ", SwingConstants.CENTER);
        lblAutorDestaque.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        lblAutorDestaque.setForeground(Color.GRAY);
        lblAutorDestaque.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblCapa = new JLabel("Sem Capa");
        lblCapa.setPreferredSize(new Dimension(200, 280));
        lblCapa.setMaximumSize(new Dimension(200, 280));
        lblCapa.setBackground(new Color(40, 40, 40));
        lblCapa.setOpaque(true);
        lblCapa.setHorizontalAlignment(SwingConstants.CENTER);
        lblCapa.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnAbrir = criarBotaoEstilizado(" Abrir Livro", COR_DESTAQUE);
        btnAbrir.setIcon(new FlatSVGIcon("icons/play_circle.svg", 18, 18)); // Ícone de Play ou Abrir

        btnConcluir = criarBotaoEstilizado(" Concluir", new Color(46, 204, 113));
        btnConcluir.setIcon(new FlatSVGIcon("icons/check_circle.svg", 18, 18));

        btnDelete = criarBotaoEstilizado(" Excluir", new Color(231, 76, 60));
        btnDelete.setIcon(new FlatSVGIcon("icons/delete_forever.svg", 18, 18));

        txtSinopseModern = new JTextArea();
        txtSinopseModern.setLineWrap(true);
        txtSinopseModern.setWrapStyleWord(true);
        txtSinopseModern.setEditable(false);
        txtSinopseModern.setBackground(COR_PAINEL);
        txtSinopseModern.setForeground(COR_TEXTO);
        txtSinopseModern.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSinopseModern.setMargin(new Insets(10, 10, 10, 10));

        barraProgresso = new JProgressBar(0, 100);
        barraProgresso.setMaximumSize(new Dimension(400, 25));
        barraProgresso.setForeground(COR_DESTAQUE);
        barraProgresso.setVisible(false);

        btnAbrir = criarBotaoEstilizado(" Abrir Livro", COR_DESTAQUE);
        btnAbrir.setIcon(criarIconeBranco("icons/book.svg", 18, 18));

        btnConcluir = criarBotaoEstilizado(" Concluir", new Color(46, 204, 113));
        btnConcluir.setIcon(criarIconeBranco("icons/library_add_check.svg", 18, 18));

        btnDelete = criarBotaoEstilizado(" Excluir", new Color(231, 76, 60));
        btnDelete.setIcon(criarIconeBranco("icons/delete.svg", 18, 18));

        // Vinculando as ações
        btnAbrir.addActionListener(e -> {
            if (livroSelecionadoAtual != null) {
                // Usa a sua classe interna que renderiza as páginas
                executaPDF leitor = new executaPDF(livroSelecionadoAtual, livrosLendo);
                leitor.setVisible(true);
            }
        });
        btnConcluir.addActionListener(e -> {
            if (livroSelecionadoAtual != null) {
                livrosLendo.remove(livroSelecionadoAtual);
                livrosParaLer.remove(livroSelecionadoAtual);
                if(!livrosLidos.contains(livroSelecionadoAtual)) livrosLidos.add(livroSelecionadoAtual);
                salvarEAtualizarTudo();
                JOptionPane.showMessageDialog(this, "Livro movido para Lidos!");
            }
        });
        btnDelete.addActionListener(e -> {
            if (livroSelecionadoAtual != null) {
                int opt = JOptionPane.showConfirmDialog(this, "Excluir este livro?");
                if (opt == JOptionPane.YES_OPTION) {
                    livrosLendo.remove(livroSelecionadoAtual);
                    livrosParaLer.remove(livroSelecionadoAtual);
                    livrosLidos.remove(livroSelecionadoAtual);
                    livroSelecionadoAtual = null;
                    salvarEAtualizarTudo();
                    limparDetalhes();
                }
            }
        });
    }

    private void renderizarInterface() {
        if (livroSelecionadoAtual == null) return;

        System.out.println("DEBUG: Sinopse do livro: " + livroSelecionadoAtual.getBiografia());

        lblTituloInfo.setText(livroSelecionadoAtual.getNomeDoLivro());
        lblAutorDestaque.setText("por " + livroSelecionadoAtual.getAutor());

        // Atualiza o texto
        txtSinopseModern.setText(livroSelecionadoAtual.getBiografia());

        // Título e Autor
        lblTituloInfo.setText(livroSelecionadoAtual.getNomeDoLivro());
        lblAutorDestaque.setText("por " + livroSelecionadoAtual.getAutor());

        // SINOPSE: O quadro cinza agora vai mostrar o texto correto
        txtSinopseModern.setText(livroSelecionadoAtual.getBiografia());
        txtSinopseModern.setCaretPosition(0);

        // CAPA: Corrigindo a proporção (imagem não ficará "fina")
        if (livroSelecionadoAtual.getPathCapa() != null && !livroSelecionadoAtual.getPathCapa().isEmpty()) {
            ImageIcon icon = new ImageIcon(livroSelecionadoAtual.getPathCapa());
            // Ajustamos para 200x280 (proporção padrão de livro)
            Image img = icon.getImage().getScaledInstance(200, 280, Image.SCALE_SMOOTH);
            lblCapa.setIcon(new ImageIcon(img));
            lblCapa.setText("");
        }

        revalidate();
        repaint();
    }

    private void abrirPDF() {
        if (livroSelecionadoAtual != null && livroSelecionadoAtual.getPathPDF() != null) {
            try {
                File pdf = new File(livroSelecionadoAtual.getPathPDF());
                if (pdf.exists()) {
                    Desktop.getDesktop().open(pdf);
                } else {
                    JOptionPane.showMessageDialog(this, "Arquivo PDF não encontrado!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao abrir: " + ex.getMessage());
            }
        }
    }

    private void concluirLivro() {
        if (livroSelecionadoAtual != null) {
            livrosLendo.remove(livroSelecionadoAtual);
            livrosParaLer.remove(livroSelecionadoAtual);
            if (!livrosLidos.contains(livroSelecionadoAtual)) {
                livrosLidos.add(livroSelecionadoAtual);
            }
            salvarEAtualizarTudo();
            JOptionPane.showMessageDialog(this, "Livro movido para Lidos!");
        }
    }

    private void excluirLivro() {
        if (livroSelecionadoAtual != null) {
            int opt = JOptionPane.showConfirmDialog(this, "Excluir este livro?");
            if (opt == JOptionPane.YES_OPTION) {
                livrosLendo.remove(livroSelecionadoAtual);
                livrosParaLer.remove(livroSelecionadoAtual);
                livrosLidos.remove(livroSelecionadoAtual);
                livroSelecionadoAtual = null;
                salvarEAtualizarTudo();
                limparDetalhes();
            }
        }
    }

    private void limparDetalhes() {
        lblTituloInfo.setText("Selecione um livro");
        lblAutorDestaque.setText(" ");
        txtSinopseModern.setText("");
        lblCapa.setIcon(null);
        lblCapa.setText("Sem Capa");
    }

    // --- MÉTODOS DE SUPORTE (MANTIDOS) ---

    public void salvarEAtualizarTudo() {
        Salvar.salvarDados(livrosLendo, PASTA_DADOS + "lendo.json");
        Salvar.salvarDados(livrosParaLer, PASTA_DADOS + "querolar.json");
        Salvar.salvarDados(livrosLidos, PASTA_DADOS + "lidos.json");

        atualizarModelo(modelosMap.get("lendo"), livrosLendo);
        atualizarModelo(modelosMap.get("querolar"), livrosParaLer);
        atualizarModelo(modelosMap.get("lidos"), livrosLidos);
        repaint(); revalidate();
    }

    private void atualizarModelo(DefaultListModel<Cadastro> modelo, ArrayList<Cadastro> lista) {
        if (modelo != null) {
            modelo.clear();
            lista.forEach(modelo::addElement);
        }
    }

    private JScrollPane criarPainelLista(ArrayList<Cadastro> lista, String id) {
        DefaultListModel<Cadastro> modelo = new DefaultListModel<>();
        lista.forEach(modelo::addElement);
        modelosMap.put(id, modelo);
        JList<Cadastro> jlist = new JList<>(modelo);
        jlist.setBackground(COR_PAINEL); jlist.setForeground(COR_TEXTO);
        jlist.setFixedCellHeight(55); jlist.setSelectionBackground(COR_DESTAQUE);
        jlist.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                livroSelecionadoAtual = jlist.getSelectedValue();
                renderizarInterface();
            }
        });
        return new JScrollPane(jlist);
    }

    private FlatSVGIcon criarIconeBranco(String path, int w, int h) {
        FlatSVGIcon icon = new FlatSVGIcon(path, w, h);
        // Filtro que transforma qualquer cor do SVG em Branco
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
        return icon;
    }

    private JPanel montarPainelDetalhesFixo() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(COR_FUNDO);
        p.setBorder(new EmptyBorder(40, 50, 40, 50));
        JScrollPane scrollSin = new JScrollPane(txtSinopseModern);
        scrollSin.setBorder(null);
        scrollSin.setOpaque(false);
        scrollSin.getViewport().setOpaque(false);
        JPanel pBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pBotoes.setOpaque(false);
        pBotoes.add(btnAbrir); pBotoes.add(btnConcluir); pBotoes.add(btnDelete);
        p.add(lblCapa); p.add(Box.createRigidArea(new Dimension(0, 20)));
        p.add(lblTituloInfo); p.add(lblAutorDestaque); p.add(Box.createRigidArea(new Dimension(0, 25)));
        p.add(barraProgresso); p.add(Box.createRigidArea(new Dimension(0, 25)));
        p.add(scrollSin); p.add(Box.createRigidArea(new Dimension(0, 30)));
        p.add(pBotoes);
        return p;
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

        // Botão Novo Livro
        JButton btnNovo = criarBotaoMenu(" Novo Livro", "");
        btnNovo.setIcon(new FlatSVGIcon("icons/add_circle.svg", 20, 20)); // Ícone de Adicionar
        btnNovo.setBackground(COR_DESTAQUE);
        btnNovo.setOpaque(true);
        btnNovo.addActionListener(e -> new AddLivro(this).setVisible(true));

        // Botões de Categorias
        JButton btnLendo = criarBotaoMenu(" Lendo", "lendo");
        btnLendo.setIcon(new FlatSVGIcon("icons/book.svg", 18, 18));

        JButton btnQueroLer = criarBotaoMenu(" Quero Ler", "querolar");
        btnQueroLer.setIcon(new FlatSVGIcon("icons/bookmark.svg", 18, 18));

        JButton btnLidos = criarBotaoMenu(" Lidos", "lidos");
        btnLidos.setIcon(new FlatSVGIcon("icons/library_add_check.svg", 18, 18));

        m.add(btnNovo);
        m.add(Box.createRigidArea(new Dimension(0, 40)));
        m.add(btnLendo);
        m.add(Box.createRigidArea(new Dimension(0, 15)));
        m.add(btnQueroLer);
        m.add(Box.createRigidArea(new Dimension(0, 15)));
        m.add(btnLidos);

        return m;
    }

    private JButton criarBotaoMenu(String txt, String id) {
        JButton b = new JButton(txt);
        b.setForeground(COR_TEXTO); b.setBackground(COR_MENU);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(0, 15, 0, 15));
        if (!id.isEmpty()) b.addActionListener(e -> cardLayout.show(painelCartoes, id));
        return b;
    }

    private JButton criarBotaoEstilizado(String t, Color c) {
        JButton b = new JButton(t);
        b.setBackground(c); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        return b;
    }
}