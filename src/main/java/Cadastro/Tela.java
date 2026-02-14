package Cadastro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.formdev.flatlaf.extras.FlatSVGIcon;

public class Tela extends JFrame {

    private final Color COR_FUNDO = new Color(30, 30, 30);
    private final Color COR_PAINEL = new Color(45, 45, 45);
    private final Color COR_MENU = new Color(25, 25, 25);
    private final Color COR_TEXTO = new Color(220, 220, 220);
    private final Color COR_DESTAQUE = new Color(88, 166, 255);
    private final Color COR_BOTAO = new Color(60, 60, 60);

    private JPanel painelConteudo;
    private CardLayout cardLayout;
    private Map<String, DefaultListModel<Cadastro>> modelosMap = new HashMap<>();
    private Cadastro livroSelecionadoAtual;

    private JLabel lblTituloInfo;
    private JProgressBar barraProgresso;

    private ArrayList<Cadastro> livrosLidos, livrosLendo, livrosParaLer;
    private final String PASTA_DADOS = "dados/";

    public Tela() {
        setTitle("Gerenciador de Leitura Pessoal");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        configurarComponentesGlobais();
        inicializarDados();

        add(criarMenuLateral(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        painelConteudo = new JPanel(cardLayout);

        painelConteudo.add(criarPainelCategoria("Lidos", livrosLidos), "lidos");
        painelConteudo.add(criarPainelCategoria("Lendo", livrosLendo), "lendo");
        painelConteudo.add(criarPainelCategoria("Quero Ler", livrosParaLer), "querolar");

        add(painelConteudo, BorderLayout.CENTER);
        cardLayout.show(painelConteudo, "lendo");
    }

    private void configurarComponentesGlobais() {
        lblTituloInfo = new JLabel("Selecione um livro");
        lblTituloInfo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTituloInfo.setForeground(COR_DESTAQUE);
        lblTituloInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        barraProgresso = new JProgressBar(0, 100);
        barraProgresso.setForeground(COR_DESTAQUE);
        barraProgresso.setBackground(COR_BOTAO);
        barraProgresso.setStringPainted(true);
        barraProgresso.setVisible(false);
        barraProgresso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
    }

    private void inicializarDados() {
        File pasta = new File(PASTA_DADOS);
        if (!pasta.exists()) pasta.mkdirs();
        this.livrosLidos = Salvar.carregarDados(PASTA_DADOS + "lidos.json");
        this.livrosLendo = Salvar.carregarDados(PASTA_DADOS + "lendo.json");
        this.livrosParaLer = Salvar.carregarDados(PASTA_DADOS + "querolar.json");
    }

    private JPanel criarPainelCategoria(String nomeExibicao, ArrayList<Cadastro> lista) {
        String idAba = nomeExibicao.toLowerCase().replace(" ", "");
        String idFixoDestaAba = idAba;
        if (idAba.equals("queroler")) idAba = "querolar";

        JPanel painelPrincipal = new JPanel(new BorderLayout());
        painelPrincipal.setBackground(COR_FUNDO);

        DefaultListModel<Cadastro> modeloLista = new DefaultListModel<>();
        lista.forEach(modeloLista::addElement);
        modelosMap.put(idAba, modeloLista);

        JList<Cadastro> listaLivros = new JList<>(modeloLista);
        listaLivros.setBackground(COR_PAINEL);
        listaLivros.setForeground(COR_TEXTO);
        listaLivros.setFixedCellHeight(45);
        listaLivros.setBorder(new EmptyBorder(5, 5, 5, 5));
        listaLivros.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listaLivros.setSelectionBackground(COR_DESTAQUE);

        JScrollPane scrollLista = new JScrollPane(listaLivros);
        scrollLista.setPreferredSize(new Dimension(300, 0));
        scrollLista.setBorder(BorderFactory.createEmptyBorder());
        scrollLista.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollLista.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));

        JPanel painelDetalhes = new JPanel();
        painelDetalhes.setLayout(new BoxLayout(painelDetalhes, BoxLayout.Y_AXIS));
        painelDetalhes.setBackground(COR_FUNDO);
        painelDetalhes.setBorder(new EmptyBorder(30, 30, 30, 30));

        JTextArea txtInfo = new JTextArea();
        txtInfo.setEditable(false);
        txtInfo.setLineWrap(true);
        txtInfo.setWrapStyleWord(true);
        txtInfo.setBackground(COR_FUNDO);
        txtInfo.setForeground(COR_TEXTO);
        txtInfo.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JScrollPane scrollTxt = new JScrollPane(txtInfo);
        scrollTxt.setBorder(null);
        scrollTxt.setOpaque(false);
        scrollTxt.getViewport().setOpaque(false);

        JButton btnAbrir = criarBotaoAcao("Abrir Livro 📖", COR_DESTAQUE);
        JButton btnConcluir = criarBotaoAcao("Marcar como Lido ✓", new Color(40, 167, 69));
        JButton btnDelete = criarBotaoAcao("Excluir 🗑", new Color(180, 50, 50));

        btnAbrir.setVisible(false); btnConcluir.setVisible(false); btnDelete.setVisible(false);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        painelBotoes.setOpaque(false);
        painelBotoes.add(btnAbrir); painelBotoes.add(btnConcluir); painelBotoes.add(btnDelete);

        painelDetalhes.add(lblTituloInfo);
        painelDetalhes.add(Box.createRigidArea(new Dimension(0, 20)));
        painelDetalhes.add(barraProgresso);
        painelDetalhes.add(Box.createRigidArea(new Dimension(0, 20)));
        painelDetalhes.add(scrollTxt);
        painelDetalhes.add(Box.createRigidArea(new Dimension(0, 20)));
        painelDetalhes.add(painelBotoes);

        String finalIdAba = idAba;
        listaLivros.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Cadastro sel = listaLivros.getSelectedValue();
                if (sel != null) {
                    this.livroSelecionadoAtual = sel;

                    // Verificação direta: Se o título do painel for "Quero Ler", esconde a barra
                    if (nomeExibicao.equalsIgnoreCase("Quero Ler")) {
                        barraProgresso.setVisible(false);
                    } else {
                        barraProgresso.setValue(sel.calcularPorcentagem());
                        barraProgresso.setVisible(true);
                    }

                    renderizarDetalhes(txtInfo, nomeExibicao); // Passamos o nome fixo

                    btnAbrir.setVisible(true);
                    btnConcluir.setVisible(!nomeExibicao.equals("Lidos"));
                    btnDelete.setVisible(true);
                }
            }
        });

        btnAbrir.addActionListener(ev -> {
            if (livroSelecionadoAtual == null) return;
            if (nomeExibicao.contains("Quero")) moverParaLendo(livroSelecionadoAtual);
            abrirVisualizadorPDF(livroSelecionadoAtual, txtInfo, finalIdAba);
        });

        btnConcluir.addActionListener(ev -> executarConclusao(finalIdAba, txtInfo, btnAbrir, btnConcluir, btnDelete));

        btnDelete.addActionListener(ev -> {
            if (JOptionPane.showConfirmDialog(this, "Excluir livro?") == JOptionPane.YES_OPTION) {
                lista.remove(livroSelecionadoAtual);
                modeloLista.removeElement(livroSelecionadoAtual);
                Salvar.salvarDados(lista, PASTA_DADOS + finalIdAba + ".json");
                limparDetalhesGerais(txtInfo, btnAbrir, btnConcluir, btnDelete);
            }
        });

        painelPrincipal.add(scrollLista, BorderLayout.WEST);
        painelPrincipal.add(painelDetalhes, BorderLayout.CENTER);

        return painelPrincipal;
    }

    private JPanel criarMenuLateral() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(COR_MENU);
        menu.setPreferredSize(new Dimension(230, 0));
        menu.setBorder(new EmptyBorder(30, 15, 30, 15));

        JLabel lblBib = new JLabel("BIBLIOTECA");
        lblBib.setForeground(new Color(120, 120, 120));
        lblBib.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBib.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnNovo = criarBotaoMenu("Novo Livro", "", "icons/add_circle.svg");
        btnNovo.setBackground(COR_DESTAQUE);
        btnNovo.setForeground(Color.WHITE);
        btnNovo.setContentAreaFilled(true);
        btnNovo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNovo.setFocusPainted(false);
        btnNovo.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        btnNovo.addActionListener(e -> new AddLivro(this, modelosMap).setVisible(true));

        menu.add(lblBib);
        menu.add(Box.createRigidArea(new Dimension(0, 30)));
        menu.add(btnNovo);
        menu.add(Box.createRigidArea(new Dimension(0, 50)));

        menu.add(criarBotaoMenu("Lendo", "lendo", "icons/book.svg"));
        menu.add(Box.createRigidArea(new Dimension(0, 20)));
        menu.add(criarBotaoMenu("Quero Ler", "querolar", "icons/bookmark.svg"));
        menu.add(Box.createRigidArea(new Dimension(0, 20)));
        menu.add(criarBotaoMenu("Lidos", "lidos", "icons/library_add_check.svg"));

        return menu;
    }

    private JButton criarBotaoMenu(String texto, String id, String pathIcone) {
        JButton b = new JButton(texto);
        if (pathIcone != null) {
            try { b.setIcon(new FlatSVGIcon(pathIcone, 18, 18)); } catch (Exception e) {}
        }
        b.setBackground(COR_MENU);
        b.setForeground(COR_TEXTO);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        b.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 10));
        b.setIconTextGap(15);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setContentAreaFilled(false);
        b.setOpaque(true);

        if (!id.isEmpty()) b.addActionListener(e -> cardLayout.show(painelConteudo, id));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(50, 50, 50));
                b.setContentAreaFilled(true);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(COR_MENU);
                b.setContentAreaFilled(false);
            }
        });

        if (!id.isEmpty()) {
            b.addActionListener(e -> {
                cardLayout.show(painelConteudo, id);
                // RESET: Esconde a barra e limpa a seleção ao trocar de aba
                barraProgresso.setVisible(false);
                lblTituloInfo.setText("Selecione um livro");
                livroSelecionadoAtual = null;
            });
        }
        return b;
    }

    private void moverParaLendo(Cadastro livro) {
        livrosParaLer.remove(livro);
        if (!livrosLendo.contains(livro)) livrosLendo.add(livro);
        modelosMap.get("querolar").removeElement(livro);
        modelosMap.get("lendo").addElement(livro);
        Salvar.salvarDados(livrosParaLer, PASTA_DADOS + "querolar.json");
        Salvar.salvarDados(livrosLendo, PASTA_DADOS + "lendo.json");
        cardLayout.show(painelConteudo, "lendo");
    }

    private void abrirVisualizadorPDF(Cadastro livro, JTextArea t, String cat) {
        try {
            executaPDF v = new executaPDF(livro, livrosLendo);
            v.setVisible(true);
            v.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosed(java.awt.event.WindowEvent e) { renderizarDetalhes(t, cat); }
            });
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void executarConclusao(String de, JTextArea t, JButton b1, JButton b2, JButton b3) {
        if (livroSelecionadoAtual == null) return;
        Cadastro alvo = livroSelecionadoAtual;
        if (de.equals("lendo")) livrosLendo.remove(alvo);
        else if (de.equals("querolar")) livrosParaLer.remove(alvo);
        livrosLidos.add(alvo);
        Salvar.salvarDados(livrosLendo, PASTA_DADOS + "lendo.json");
        Salvar.salvarDados(livrosParaLer, PASTA_DADOS + "querolar.json");
        Salvar.salvarDados(livrosLidos, PASTA_DADOS + "lidos.json");
        modelosMap.get(de).removeElement(alvo);
        modelosMap.get("lidos").addElement(alvo);
        limparDetalhesGerais(t, b1, b2, b3);
        JOptionPane.showMessageDialog(this, "Livro Concluído! 🎉");
    }

    private void renderizarDetalhes(JTextArea txt, String categoria) {
        if (livroSelecionadoAtual == null) return;

        lblTituloInfo.setText(livroSelecionadoAtual.getNomeDoLivro());

        // Nova lógica de verificação mais robusta
        boolean ehQueroLer = categoria.toLowerCase().contains("quero") || categoria.toLowerCase().contains("querolar");

        if (ehQueroLer) {
            barraProgresso.setVisible(false);
        } else {
            int p = livroSelecionadoAtual.calcularPorcentagem();
            barraProgresso.setValue(p);
            barraProgresso.setVisible(true);
        }

        txt.setText(String.format("Autor: %s\nProgresso: %d / %d páginas (%d%%)\nLocal: %s\n\n--- Sinopse ---\n%s",
                livroSelecionadoAtual.getAutor(),
                livroSelecionadoAtual.getUltimaPagina(),
                livroSelecionadoAtual.getQtdPag(),
                livroSelecionadoAtual.calcularPorcentagem(),
                livroSelecionadoAtual.getPathPDF(),
                livroSelecionadoAtual.getBiografia()));
    }

    private void limparDetalhesGerais(JTextArea t, JButton b1, JButton b2, JButton b3) {
        livroSelecionadoAtual = null; lblTituloInfo.setText("Selecione um livro");
        barraProgresso.setVisible(false); t.setText("");
        b1.setVisible(false); b2.setVisible(false); b3.setVisible(false);
    }

    private JButton criarBotaoAcao(String t, Color c) {
        JButton b = new JButton(t);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return b;
    }
}