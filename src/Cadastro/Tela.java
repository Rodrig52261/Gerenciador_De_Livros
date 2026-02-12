package Cadastro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Tela extends JFrame {

    private java.util.Map<String, DefaultListModel<Cadastro>> modelosMap = new java.util.HashMap<>();
    private void sincronizarInterface(String categoriaDestino, Cadastro livro) {
        // 1. Pega o modelo da aba que deve receber o livro
        DefaultListModel<Cadastro> modeloAlvo = modelosMap.get(categoriaDestino);

        if (modeloAlvo != null) {
            // 2. Adiciona o livro lá (a JList atualiza sozinha na hora)
            modeloAlvo.addElement(livro);
        }
    }

    // Cores (Constantes)
    private final Color COR_FUNDO = new Color(30, 30, 30);
    private final Color COR_PAINEL = new Color(45, 45, 45);
    private final Color COR_MENU = new Color(25, 25, 25);
    private final Color COR_TEXTO = new Color(220, 220, 220);
    private final Color COR_DESTAQUE = new Color(88, 166, 255);
    private final Color COR_BOTAO = new Color(60, 60, 60);

    private JPanel painelConteudo;
    private CardLayout cardLayout;

    // Listas de dados
    private ArrayList<Cadastro> livrosLidos = new ArrayList<>();
    private ArrayList<Cadastro> livrosLendo = new ArrayList<>();
    private ArrayList<Cadastro> livrosParaLer = new ArrayList<>();

    public Tela() {
        setTitle("Gerenciador de Leitura Pessoal");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COR_FUNDO);

        JPanel menuLateral = criarMenuLateral();
        add(menuLateral, BorderLayout.WEST);

        cardLayout = new CardLayout();
        painelConteudo = new JPanel(cardLayout);
        painelConteudo.setBackground(COR_FUNDO);

        // Carregando os dados usando a classe Salvar
        this.livrosLidos = Salvar.carregarDados("lidos.json");
        this.livrosLendo = Salvar.carregarDados("lendo.json");
        this.livrosParaLer = Salvar.carregarDados("queroler.json");

        painelConteudo.add(criarPainelCategoria("Lidos", livrosLidos), "lidos");
        painelConteudo.add(criarPainelCategoria("Lendo", livrosLendo), "lendo");
        painelConteudo.add(criarPainelCategoria("Quero Ler", livrosParaLer), "querolar");

        add(painelConteudo, BorderLayout.CENTER);
        cardLayout.show(painelConteudo, "lidos");
    }

    private JPanel criarMenuLateral() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(COR_MENU);
        menu.setPreferredSize(new Dimension(180, 600));
        menu.setBorder(new EmptyBorder(20, 10, 20, 10));

        JLabel titulo = new JLabel("Minha Biblioteca"); // Removi emoji para evitar quadrados
        titulo.setForeground(COR_DESTAQUE);
        titulo.setFont(new Font("Arial", Font.BOLD, 14));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setBorder(new EmptyBorder(0, 0, 20, 0));
        menu.add(titulo);

        menu.add(criarBotaoMenu("Lidos", "lidos"));
        menu.add(Box.createRigidArea(new Dimension(0, 10)));
        menu.add(criarBotaoMenu("Lendo", "lendo"));
        menu.add(Box.createRigidArea(new Dimension(0, 10)));
        menu.add(criarBotaoMenu("Quero Ler", "querolar"));

        return menu;
    }

    private JButton criarBotaoMenu(String texto, String painelID) {
        JButton botao = new JButton(texto);
        botao.setBackground(COR_BOTAO);
        botao.setForeground(COR_TEXTO);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setFont(new Font("Arial", Font.PLAIN, 13));
        botao.setMaximumSize(new Dimension(160, 40));
        botao.setAlignmentX(Component.CENTER_ALIGNMENT);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.addActionListener(e -> cardLayout.show(painelConteudo, painelID));
        return botao;
    }

    private JPanel criarPainelCategoria(String categoria, ArrayList<Cadastro> lista) {
        JPanel painelPrincipal = new JPanel(new BorderLayout());
        painelPrincipal.setBackground(COR_FUNDO);
        painelPrincipal.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- 1. MODELO E LISTA (LADO ESQUERDO) ---
        DefaultListModel<Cadastro> modeloLista = new DefaultListModel<>();
        for (Cadastro c : lista) {
            modeloLista.addElement(c);
        }

        JList<Cadastro> listaLivros = new JList<>(modeloLista);
        listaLivros.setBackground(COR_PAINEL);
        listaLivros.setForeground(COR_TEXTO);
        listaLivros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaLivros.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane scrollLista = new JScrollPane(listaLivros);
        scrollLista.setPreferredSize(new Dimension(200, 0));

        for (Cadastro c : lista) modeloLista.addElement(c);

        // Registra para acesso global
        modelosMap.put(categoria, modeloLista);

        // --- 2. PAINEL DE DETALHES (LADO DIREITO) ---
        JPanel painelDetalhes = new JPanel();
        painelDetalhes.setLayout(new BoxLayout(painelDetalhes, BoxLayout.Y_AXIS));
        painelDetalhes.setBackground(COR_PAINEL);
        painelDetalhes.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTituloInfo = new JLabel("Selecione um livro");
        lblTituloInfo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTituloInfo.setForeground(COR_DESTAQUE);
        lblTituloInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea txtInfo = new JTextArea("Clique em um item da lista para ver os detalhes.");
        txtInfo.setEditable(false);
        txtInfo.setBackground(COR_PAINEL);
        txtInfo.setForeground(COR_TEXTO);
        txtInfo.setLineWrap(true);
        txtInfo.setWrapStyleWord(true);
        txtInfo.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane scrollDetalhes = new JScrollPane(txtInfo);
        scrollDetalhes.setBorder(null);
        scrollDetalhes.setOpaque(false);
        scrollDetalhes.getViewport().setOpaque(false);
        scrollDetalhes.setAlignmentX(Component.LEFT_ALIGNMENT);

        painelDetalhes.add(lblTituloInfo);
        painelDetalhes.add(Box.createRigidArea(new Dimension(0, 10)));
        painelDetalhes.add(scrollDetalhes);

        // BOTÃO CONCLUIR (Apenas se não estiver na aba Lidos)
        if (!categoria.equals("Lidos")) {
            JButton btnConcluir = criarBotaoAcao("Marcar como Lido ✓", new Color(40, 167, 69));
            btnConcluir.setAlignmentX(Component.LEFT_ALIGNMENT);
            painelDetalhes.add(Box.createRigidArea(new Dimension(0, 10)));
            painelDetalhes.add(btnConcluir);

            btnConcluir.addActionListener(e -> {
                Cadastro selecionado = listaLivros.getSelectedValue();
                if (selecionado != null) {
                    // Move nas listas de dados (ArrayLists)
                    livrosLidos.add(selecionado);
                    lista.remove(selecionado);

                    // Atualiza a visualização da aba ATUAL
                    modeloLista.removeElement(selecionado);

                    // Atualiza a visualização da aba LIDOS (mesmo que ela esteja escondida)
                    sincronizarInterface("Lidos", selecionado);

                    // Salva tudo
                    Salvar.salvarDados(livrosLidos, "lidos.json");
                    Salvar.salvarDados(lista, categoria.toLowerCase().replace(" ", "") + ".json");

                    lblTituloInfo.setText("Movido com sucesso!");
                }
            });
        }

        // EVENTO DE CLIQUE NA LISTA
        listaLivros.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Cadastro selecionado = listaLivros.getSelectedValue();
                if (selecionado != null) {
                    lblTituloInfo.setText(selecionado.getNomeDoLivro());
                    String detalhes = "Autor: " + selecionado.getAutor() + "\n\n" +
                            "Progresso: " + selecionado.calcularPorcentagem() + "%\n\n" +
                            "Biografia:\n" + selecionado.getBiografia();
                    txtInfo.setText(detalhes);
                    txtInfo.setCaretPosition(0); // Volta o scroll da bio para o topo
                }
            }
        });

        // --- 3. FORMULÁRIO DE CADASTRO (TOPO) ---
        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.setBackground(COR_PAINEL);
        formulario.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTextField campoNome = criarCampo();
        JTextField campoAutor = criarCampo();
        JTextField campoPaginas = criarCampo();
        JTextArea areaBio = new JTextArea(3, 20);
        areaBio.setBackground(COR_BOTAO);
        areaBio.setForeground(COR_TEXTO);
        areaBio.setLineWrap(true);
        JScrollPane scrollBio = new JScrollPane(areaBio);

        formulario.add(criarLabel("Nome do Livro:"));
        formulario.add(campoNome);
        formulario.add(criarLabel("Autor:"));
        formulario.add(campoAutor);
        formulario.add(criarLabel("Biografia:"));
        formulario.add(scrollBio);
        formulario.add(criarLabel("Total de Páginas:"));
        formulario.add(campoPaginas);

        JTextField campoLidas = null;
        if (categoria.equals("Lendo")) {
            campoLidas = criarCampo();
            formulario.add(criarLabel("Páginas Lidas:"));
            formulario.add(campoLidas);
        }
        final JTextField finalLidas = campoLidas;

        // --- 4. BOTÕES ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelBotoes.setBackground(COR_FUNDO);
        JButton botaoCadastrar = criarBotaoAcao("Cadastrar", COR_DESTAQUE);
        JButton botaoSalvar = criarBotaoAcao("Salvar", COR_BOTAO);
        JButton botaoExibir = criarBotaoAcao("Exibir Tudo", COR_BOTAO);

        painelBotoes.add(botaoCadastrar);
        painelBotoes.add(botaoExibir);
        painelBotoes.add(botaoSalvar);

        // Ações dos botões
        botaoExibir.addActionListener(e -> {
            if (lista.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Sua biblioteca nesta categoria está vazia.");
                return;
            }
            StringBuilder relatorio = new StringBuilder("=== RESUMO: " + categoria.toUpperCase() + " ===\n\n");
            for (Cadastro c : lista) {
                relatorio.append("📖 ").append(c.getNomeDoLivro()).append(" (").append(c.calcularPorcentagem()).append("%)\n");
            }
            JTextArea msg = new JTextArea(relatorio.toString());
            msg.setEditable(false);
            msg.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JOptionPane.showMessageDialog(this, new JScrollPane(msg), "Relatório", JOptionPane.PLAIN_MESSAGE);
        });

        botaoCadastrar.addActionListener(e -> {
            try {
                int total = Integer.parseInt(campoPaginas.getText());
                int lidas = (finalLidas != null && !finalLidas.getText().isEmpty()) ? Integer.parseInt(finalLidas.getText()) : 0;
                if (categoria.equals("Lidos")) lidas = total;

                Cadastro novo = new Cadastro();
                novo.criarCadastro(campoNome.getText(), campoAutor.getText(), areaBio.getText(), total, lidas);

                lista.add(novo);
                modeloLista.addElement(novo);
                JOptionPane.showMessageDialog(this, "Cadastrado!");

                campoNome.setText(""); campoAutor.setText(""); areaBio.setText(""); campoPaginas.setText("");
                if(finalLidas != null) finalLidas.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro nos dados!");
            }
        });

        botaoSalvar.addActionListener(e -> {
            Salvar.salvarDados(lista, categoria.toLowerCase().replace(" ", "") + ".json");
            JOptionPane.showMessageDialog(this, "Salvo com sucesso!");
        });

        // --- 5. MONTAGEM FINAL ---
        JPanel painelSuperior = new JPanel(new BorderLayout());
        painelSuperior.setBackground(COR_FUNDO);
        JLabel lblCategoria = new JLabel(categoria);
        lblCategoria.setFont(new Font("Arial", Font.BOLD, 22));
        lblCategoria.setForeground(COR_DESTAQUE);
        lblCategoria.setBorder(new EmptyBorder(0,0,10,0));

        painelSuperior.add(lblCategoria, BorderLayout.NORTH);
        painelSuperior.add(formulario, BorderLayout.CENTER);
        painelSuperior.add(painelBotoes, BorderLayout.SOUTH);

        JPanel painelInferior = new JPanel(new GridLayout(1, 2, 10, 0));
        painelInferior.setBackground(COR_FUNDO);
        painelInferior.add(scrollLista);
        painelInferior.add(painelDetalhes);

        painelPrincipal.add(painelSuperior, BorderLayout.NORTH);
        painelPrincipal.add(painelInferior, BorderLayout.CENTER);

        return painelPrincipal;
    }

    private JLabel criarLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(COR_TEXTO);
        return l;
    }

    private JTextField criarCampo() {
        JTextField f = new JTextField();
        f.setBackground(COR_BOTAO);
        f.setForeground(COR_TEXTO);
        f.setCaretColor(COR_TEXTO);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COR_DESTAQUE), new EmptyBorder(5,5,5,5)));
        return f;
    }

    private JButton criarBotaoAcao(String t, Color c) {
        JButton b = new JButton(t);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efeito visual ao passar o mouse
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(c.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(c);
            }
        });
        return b;
    }
}