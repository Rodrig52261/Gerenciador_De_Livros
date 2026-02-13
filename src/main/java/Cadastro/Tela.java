package Cadastro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Tela extends JFrame {

    // Constantes de Cores
    private final Color COR_FUNDO = new Color(30, 30, 30);
    private final Color COR_PAINEL = new Color(45, 45, 45);
    private final Color COR_MENU = new Color(25, 25, 25);
    private final Color COR_TEXTO = new Color(220, 220, 220);
    private final Color COR_DESTAQUE = new Color(88, 166, 255);
    private final Color COR_BOTAO = new Color(60, 60, 60);

    // Layout e Dados
    private JPanel painelConteudo;
    private CardLayout cardLayout;
    private Map<String, DefaultListModel<Cadastro>> modelosMap = new HashMap<>();
    private Cadastro livroSelecionadoAtual;
    private String categoriaAtual;

    // Componentes Globais (Apenas os que são compartilhados)
    private JLabel lblTituloInfo;
    private JProgressBar barraProgresso;

    private ArrayList<Cadastro> livrosLidos;
    private ArrayList<Cadastro> livrosLendo;
    private ArrayList<Cadastro> livrosParaLer;
    private final String PASTA_DADOS = "dados/";

    public Tela() {
        setTitle("Gerenciador de Leitura Pessoal");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Inicializar Componentes Base
        lblTituloInfo = new JLabel("Selecione um livro");
        lblTituloInfo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTituloInfo.setForeground(COR_DESTAQUE);
        lblTituloInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        barraProgresso = new JProgressBar(0, 100);
        barraProgresso.setForeground(COR_DESTAQUE);
        barraProgresso.setBackground(COR_BOTAO);
        barraProgresso.setStringPainted(true);
        barraProgresso.setVisible(false);
        barraProgresso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        barraProgresso.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 2. Carregar Dados
        String caminho = "dados/";
        File pasta = new File(caminho);
        if (!pasta.exists()) pasta.mkdir(); // Cria a pasta se ela não existir

        this.livrosLidos = Salvar.carregarDados(caminho + "lidos.json");
        this.livrosLendo = Salvar.carregarDados(caminho + "lendo.json");
        this.livrosLendo = Salvar.carregarDados(caminho + "QueroLer.json");

        // Cria a pasta física se ela não existir
        File pastaDois = new File(PASTA_DADOS);
        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        // Agora carrega usando o caminho da pasta
        this.livrosLidos = Salvar.carregarDados(PASTA_DADOS + "lidos.json");
        this.livrosLendo = Salvar.carregarDados(PASTA_DADOS + "lendo.json");
        this.livrosParaLer = Salvar.carregarDados(PASTA_DADOS + "querolar.json");

        // 3. Montar Interface
        add(criarMenuLateral(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        painelConteudo = new JPanel(cardLayout);

        painelConteudo.add(criarPainelCategoria("Lidos", livrosLidos), "lidos");
        painelConteudo.add(criarPainelCategoria("Lendo", livrosLendo), "lendo");
        painelConteudo.add(criarPainelCategoria("Quero Ler", livrosParaLer), "querolar");

        add(painelConteudo, BorderLayout.CENTER);
        cardLayout.show(painelConteudo, "lendo");
    }

    private JButton criarBotaoAcao(String t, Color c) {
        JButton b = new JButton(t);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(c.brighter()); }
            public void mouseExited(MouseEvent e) { b.setBackground(c); }
        });
        return b;
    }

    private JTextField criarCampo() {
        JTextField f = new JTextField();
        f.setBackground(COR_BOTAO);
        f.setForeground(COR_TEXTO);
        f.setCaretColor(COR_TEXTO);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_DESTAQUE), new EmptyBorder(5, 5, 5, 5)));
        return f;
    }

    private JLabel criarLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(COR_TEXTO);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    private void abrirJanelaCadastro() {
        JDialog janela = new JDialog(this, "Cadastrar Novo Livro", true);
        janela.setSize(450, 550);
        janela.setLocationRelativeTo(this);
        janela.getContentPane().setBackground(COR_PAINEL);
        janela.setLayout(new BorderLayout());

        final String[] biografiaCapturada = {""};
        final String[] caminhoPDF = {null};

        // 1. Painel Superior (Destaque para o PDF)
        JPanel painelTopo = new JPanel(new BorderLayout(10, 10));
        painelTopo.setBackground(COR_PAINEL);
        painelTopo.setBorder(new EmptyBorder(20, 30, 10, 30));

        JButton btnPDF = criarBotaoAcao("📁 SELECIONAR ARQUIVO PDF", COR_DESTAQUE);
        JLabel lblArq = new JLabel("Nenhum arquivo selecionado", SwingConstants.CENTER);
        lblArq.setForeground(Color.GRAY);
        lblArq.setFont(new Font("Segoe UI", Font.ITALIC, 11));

        painelTopo.add(btnPDF, BorderLayout.NORTH);
        painelTopo.add(lblArq, BorderLayout.SOUTH);

        // 2. Painel de Formulário (Campos Desabilitados)
        JPanel painelForm = new JPanel(new GridLayout(0, 1, 2, 2));
        painelForm.setBackground(COR_PAINEL);
        painelForm.setBorder(new EmptyBorder(10, 30, 20, 30));

        JTextField campoNome = criarCampo();
        JTextField campoAutor = criarCampo();
        JTextField campoPaginas = criarCampo();

        // Desabilitando a edição manual para forçar a automação
        campoNome.setEditable(false);
        campoAutor.setEditable(false);
        campoPaginas.setEditable(false);

        // Estética de campo desabilitado mas legível
        Color corDesabilitado = new Color(70, 70, 70);
        campoNome.setBackground(corDesabilitado);
        campoAutor.setBackground(corDesabilitado);
        campoPaginas.setBackground(corDesabilitado);

        String[] cats = {"Lidos", "Lendo", "Quero Ler"};
        JComboBox<String> combo = new JComboBox<>(cats);

        painelForm.add(criarLabel("Título extraído:")); painelForm.add(campoNome);
        painelForm.add(criarLabel("Autor extraído:")); painelForm.add(campoAutor);
        painelForm.add(criarLabel("Total de páginas:")); painelForm.add(campoPaginas);
        painelForm.add(criarLabel("Mover para categoria:")); painelForm.add(combo);

        // --- Lógica de Automação ---
        btnPDF.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Arquivos PDF", "pdf"));

            if (fc.showOpenDialog(janela) == JFileChooser.APPROVE_OPTION) {
                File arquivo = fc.getSelectedFile();
                caminhoPDF[0] = arquivo.getAbsolutePath();
                lblArq.setText("✔ " + arquivo.getName());
                lblArq.setForeground(COR_DESTAQUE);

                try (org.apache.pdfbox.pdmodel.PDDocument doc = org.apache.pdfbox.Loader.loadPDF(arquivo)) {
                    var info = doc.getDocumentInformation();

                    String titulo = (info.getTitle() != null && !info.getTitle().isEmpty())
                            ? info.getTitle() : arquivo.getName().replace(".pdf", "");

                    campoNome.setText(titulo);
                    campoAutor.setText(info.getAuthor() != null ? info.getAuthor() : "Não identificado");
                    campoPaginas.setText(String.valueOf(doc.getNumberOfPages()));

                    // Busca Biografia em Background
                    new Thread(() -> {
                        String[] infoExtra = LivroService.buscarInfoExtra(titulo);
                        biografiaCapturada[0] = infoExtra[0];
                    }).start();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(janela, "Falha ao processar PDF.");
                }
            }
        });

        // 3. Botão Salvar (Rodapé)
        JButton btnSalvar = criarBotaoAcao("Confirmar e Salvar", new Color(40, 167, 69));
        btnSalvar.addActionListener(e -> {
            if (caminhoPDF[0] == null) {
                JOptionPane.showMessageDialog(janela, "Por favor, selecione um PDF primeiro.");
                return;
            }

            Cadastro n = new Cadastro();
            n.criarCadastro(campoNome.getText(), campoAutor.getText(), biografiaCapturada[0],
                    Integer.parseInt(campoPaginas.getText()), 0, caminhoPDF[0]);

            String sel = ((String) combo.getSelectedItem()).toLowerCase().replace(" ", "");

            // Lógica de lista dinâmica
            if (sel.equals("lidos")) livrosLidos.add(n);
            else if (sel.equals("lendo")) livrosLendo.add(n);
            else livrosParaLer.add(n);

            Salvar.salvarDados(sel.equals("lidos") ? livrosLidos :
                    sel.equals("lendo") ? livrosLendo : livrosParaLer, sel + ".json");

            modelosMap.get(sel).addElement(n);
            janela.dispose();
        });

        janela.add(painelTopo, BorderLayout.NORTH);
        janela.add(painelForm, BorderLayout.CENTER);
        janela.add(btnSalvar, BorderLayout.SOUTH);
        janela.setVisible(true);
    }

    private JPanel criarMenuLateral() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(COR_MENU);
        menu.setPreferredSize(new Dimension(200, 0));
        menu.setBorder(new EmptyBorder(30, 15, 30, 15));

        JLabel titulo = new JLabel("BIBLIOTECA");
        titulo.setForeground(COR_DESTAQUE);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        menu.add(titulo);
        menu.add(Box.createRigidArea(new Dimension(0, 40)));

        menu.add(criarBotaoMenu("Lidos", "lidos"));
        menu.add(Box.createRigidArea(new Dimension(0, 10)));
        menu.add(criarBotaoMenu("Lendo", "lendo"));
        menu.add(Box.createRigidArea(new Dimension(0, 10)));
        menu.add(criarBotaoMenu("Quero Ler", "querolar"));

        menu.add(Box.createVerticalGlue());

        JButton btnNovoLivro = criarBotaoAcao("+ Novo Livro", COR_DESTAQUE);
        btnNovoLivro.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnNovoLivro.setMaximumSize(new Dimension(180, 40));
        btnNovoLivro.addActionListener(e -> abrirJanelaCadastro());
        menu.add(btnNovoLivro);

        return menu;
    }

    private JButton criarBotaoMenu(String texto, String painelID) {
        JButton botao = new JButton(texto);
        botao.setBackground(COR_BOTAO);
        botao.setForeground(COR_TEXTO);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        botao.setMaximumSize(new Dimension(180, 40));
        botao.setAlignmentX(Component.CENTER_ALIGNMENT);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.addActionListener(e -> {
            limparDetalhesGerais();
            cardLayout.show(painelConteudo, painelID);
        });
        return botao;
    }

    private void limparDetalhesGerais() {
        livroSelecionadoAtual = null;
        lblTituloInfo.setText("Selecione um livro");
        barraProgresso.setVisible(false);
    }

    private JPanel criarPainelCategoria(String nomeExibicao, ArrayList<Cadastro> lista) {
        String idAba = nomeExibicao.toLowerCase().replace(" ", "");
        JPanel painelPrincipal = new JPanel(new BorderLayout());
        painelPrincipal.setBackground(COR_FUNDO);
        painelPrincipal.setBorder(new EmptyBorder(20, 20, 20, 20));

        DefaultListModel<Cadastro> modeloLista = new DefaultListModel<>();
        lista.forEach(modeloLista::addElement);
        modelosMap.put(idAba, modeloLista);

        JList<Cadastro> listaLivros = new JList<>(modeloLista);
        listaLivros.setBackground(COR_PAINEL);
        listaLivros.setForeground(COR_TEXTO);
        JScrollPane scrollLista = new JScrollPane(listaLivros);
        scrollLista.setPreferredSize(new Dimension(250, 0));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COR_PAINEL);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Componentes Locais desta Aba
        JTextArea txtAreaLocal = new JTextArea();
        txtAreaLocal.setEditable(false);
        txtAreaLocal.setLineWrap(true);
        txtAreaLocal.setWrapStyleWord(true);
        txtAreaLocal.setBackground(COR_PAINEL);
        txtAreaLocal.setForeground(COR_TEXTO);
        JScrollPane scrollTxt = new JScrollPane(txtAreaLocal);
        scrollTxt.setBorder(null);

        JButton btnAbrirLocal = criarBotaoAcao("Abrir Livro 📖", COR_DESTAQUE);
        JButton btnConcluirLocal = criarBotaoAcao("Marcar como Lido ✓", new Color(40, 167, 69));
        JButton btnDeleteLocal = criarBotaoDelete(listaLivros, modeloLista, lista, idAba);

        btnAbrirLocal.setVisible(false);
        btnConcluirLocal.setVisible(false);
        btnDeleteLocal.setVisible(false);

        // Montagem do Card
        card.add(lblTituloInfo);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(barraProgresso);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(scrollTxt);
        card.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        painelAcoes.setOpaque(false);
        painelAcoes.add(btnAbrirLocal);
        painelAcoes.add(btnConcluirLocal);
        painelAcoes.add(btnDeleteLocal);
        painelAcoes.add(Box.createHorizontalGlue());
        card.add(painelAcoes);

        listaLivros.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Cadastro sel = listaLivros.getSelectedValue();
                if (sel != null) {
                    this.livroSelecionadoAtual = sel;
                    this.categoriaAtual = idAba;

                    lblTituloInfo.setText(sel.getNomeDoLivro());
                    txtAreaLocal.setText("Autor: " + sel.getAutor() +
                            "\nProgresso: " + sel.getPaginasLidas() + " / " + sel.getQtdPag() +
                            "\n\nBiografia:\n" + (sel.getBiografia().isEmpty() ? "Sem biografia." : sel.getBiografia()));

                    barraProgresso.setValue(sel.calcularPorcentagem());
                    barraProgresso.setVisible(true);

                    btnAbrirLocal.setVisible(sel.getPathPDF() != null && !sel.getPathPDF().isEmpty());
                    btnConcluirLocal.setVisible(!idAba.equals("lidos"));
                    btnDeleteLocal.setVisible(true);
                }
            }
        });

        btnAbrirLocal.addActionListener(ev -> {
            if (livroSelecionadoAtual != null) {
                new executaPDF(livroSelecionadoAtual, lista).setVisible(true);
            }
        });

        btnConcluirLocal.addActionListener(ev -> executarConclusao(idAba, txtAreaLocal, btnAbrirLocal, btnConcluirLocal, btnDeleteLocal));

        painelPrincipal.add(scrollLista, BorderLayout.WEST);
        painelPrincipal.add(card, BorderLayout.CENTER);

        return painelPrincipal;
    }

    private void executarConclusao(String deOnde, JTextArea txt, JButton b1, JButton b2, JButton b3) {
        if (livroSelecionadoAtual == null) return;

        // 1. Armazena a referência do livro para não perdê-la durante a transição
        Cadastro alvo = livroSelecionadoAtual;

        // 2. Remove da lista de origem (Lendo ou Quero Ler)
        if (deOnde.equals("lendo")) {
            livrosLendo.remove(alvo);
        } else if (deOnde.equals("querolar")) {
            livrosParaLer.remove(alvo);
        }

        // 3. Adiciona na lista de destino (Lidos)
        livrosLidos.add(alvo);

        // 4. Salva as mudanças nos arquivos JSON correspondentes
        Salvar.salvarDados(livrosLendo, "lendo.json");
        Salvar.salvarDados(livrosParaLer, "querolar.json");
        Salvar.salvarDados(livrosLidos, "lidos.json");

        // 5. Atualiza os componentes visuais das listas (JList)
        modelosMap.get(deOnde).removeElement(alvo);
        modelosMap.get("lidos").addElement(alvo);

        // 6. Limpa os detalhes da aba atual para indicar que o livro saiu dali
        limparDetalhesGerais();
        txt.setText("");
        b1.setVisible(false); // Botão Abrir
        b2.setVisible(false); // Botão Concluir
        b3.setVisible(false); // Botão Excluir

        JOptionPane.showMessageDialog(this, "Parabéns! Livro movido para a galeria de Lidos. 🎉");
    }

    // --- Métodos de auxílio para botões e campos que você já tem ---

    private JButton criarBotaoDelete(JList<Cadastro> lista, DefaultListModel<Cadastro> modelo, ArrayList<Cadastro> dados, String cat) {
        JButton btn = criarBotaoAcao("Excluir 🗑", new Color(180, 50, 50));
        btn.addActionListener(e -> {
            Cadastro sel = lista.getSelectedValue();
            if (sel != null && JOptionPane.showConfirmDialog(this, "Deseja excluir este livro?") == JOptionPane.YES_OPTION) {
                dados.remove(sel);
                modelo.removeElement(sel);
                Salvar.salvarDados(dados, cat + ".json");
                limparDetalhesGerais();
            }
        });
        return btn;
    }
} // <--- Não esqueça de fechar a classe Tela aqui!