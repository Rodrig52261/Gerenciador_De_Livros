package Cadastro;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class executaPDF extends JFrame {

    private PDDocument documento;
    private PDFRenderer renderer;
    int paginaAtual = 0;
    private int totalPaginas = 0;
    private Cadastro livro;
    private ArrayList<Cadastro> listaGeral;
    private ArrayList<Cadastro> listaParaSalvar;

    // Estados
    private boolean modoDuplo = true;
    private boolean modoNoturno = false;
    boolean modoMarcador = false;
    double fatorZoom = 1.0;
    private Point pontoInicial;
    private Point pontoArrasto;

    // Componentes Interface
    private JLabel lblPagina, lblStatusZoom;
    private JLabel labelPagEsquerda, labelPagDireita;
    private JScrollPane scrollPane;
    DefaultListModel<String> modelFavoritos;
    java.util.List<Integer> favoritos = new ArrayList<>();
    private JList<String> listFavoritos;
    private JPanel painelLateralFavoritos;

    public executaPDF(Cadastro livro, ArrayList<Cadastro> listaParaSalvar) {
        this.livro = livro;
        this.listaParaSalvar = listaParaSalvar;
        this.paginaAtual = livro.getUltimaPagina();

        // Garante início em página par no modo duplo
        if (modoDuplo && paginaAtual % 2 != 0 && paginaAtual > 0) paginaAtual--;

        configurarJanela();

        // Painel de Navegação
        JPanel painelNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        painelNav.setBackground(new Color(25, 25, 25));

        // Botões com ícones sugeridos
        JButton btnAnterior = criarBotaoComIcone(" Anterior", "arrow_back.svg");
        JButton btnProximo = criarBotaoComIcone(" Próximo", "arrow_forward.svg");
        JButton btnModo = criarBotaoComIcone(" Alternar Visão", "view_column.svg");
        JButton btnNoite = criarBotaoComIcone("", "visibility.svg");

        lblPagina = new JLabel("Página 0 de 0");
        lblPagina.setForeground(Color.WHITE);
        lblPagina.setFont(new Font("Segoe UI", Font.BOLD, 14));

        painelNav.add(btnAnterior);
        painelNav.add(lblPagina);
        painelNav.add(btnProximo);
        painelNav.add(btnModo);

        // Área do Livro
        JPanel painelFolhas = new JPanel(new GridLayout(1, 2, 0, 0));
        painelFolhas.setBackground(new Color(35, 35, 35));
        painelFolhas.setBorder(new EmptyBorder(20, 40, 20, 40));
        JPanel painelStatus = new JPanel(new BorderLayout());
        JButton btnFavoritos = criarBotaoComIcone(" Marcadores", "star.svg");

        // Criamos as labels já preparadas para desenhar o retângulo temporário
        labelPagEsquerda = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                desenharRetanguloTemporario(g);
            }
        };
        labelPagEsquerda.setHorizontalAlignment(JLabel.RIGHT);

        labelPagDireita = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                desenharRetanguloTemporario(g);
            }
        };

        lblStatusZoom = new JLabel("Zoom: 100%");
        lblStatusZoom.setForeground(Color.GRAY);
        lblStatusZoom.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Sombra Central
        Color corSombra = new Color(0, 0, 0, 80);
        labelPagEsquerda.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, corSombra));
        labelPagDireita.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, corSombra));


        painelStatus.setBackground(new Color(20, 20, 20));
        painelStatus.setBorder(new EmptyBorder(5, 15, 5, 15));

        painelFolhas.add(labelPagEsquerda);
        painelFolhas.add(labelPagDireita);
        configurarEventosMouse(labelPagEsquerda);
        configurarEventosMouse(labelPagDireita);

        modelFavoritos = new DefaultListModel<>();

        if (livro.getFavoritos() != null) {
            for (String fav : livro.getFavoritos()) {
                modelFavoritos.addElement(fav);

                // Extrai o número da página (para evitar que o usuário favorite a mesma página duas vezes)
                try {
                    String numeroStr = fav.replaceAll("[^0-9]", ""); // Pega apenas os números do texto
                    if (!numeroStr.isEmpty()) {
                        // Salva na lista de controle (subtrai 1 porque nosso sistema conta a partir do 0)
                        favoritos.add(Integer.parseInt(numeroStr) - 1);
                    }
                } catch (Exception ex) {
                    System.out.println("Erro ao ler número da página do favorito.");
                }
            }
        }

        listFavoritos = new JList<>(modelFavoritos);


        listFavoritos = new JList<>(modelFavoritos);
        listFavoritos.setBackground(new Color(30, 30, 30));
        listFavoritos.setForeground(Color.WHITE);
        listFavoritos.setSelectionBackground(new Color(60, 60, 60));

        // Painel que conterá a lista de favoritos
        painelLateralFavoritos = new JPanel(new BorderLayout());
        painelLateralFavoritos.setPreferredSize(new Dimension(180, 0));
        painelLateralFavoritos.setBackground(new Color(25, 25, 25));
        painelLateralFavoritos.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY), "FAVORITOS",
                0, 0, null, Color.LIGHT_GRAY));

        painelLateralFavoritos.add(new JScrollPane(listFavoritos), BorderLayout.CENTER);
        painelLateralFavoritos.setVisible(false); // Começa escondido

        add(painelLateralFavoritos, BorderLayout.EAST);

        // Ação ao clicar em um favorito na lista
        listFavoritos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listFavoritos.getSelectedValue() != null) {
                String item = listFavoritos.getSelectedValue();
                int pag = Integer.parseInt(item.replace("Página ", "")) - 1;
                paginaAtual = pag;
                renderizarPaginas();
            }
        });

        scrollPane = new JScrollPane(painelFolhas);
        scrollPane.setBackground(new Color(30, 30, 30));
        scrollPane.setBorder(null);
        scrollPane.setWheelScrollingEnabled(false);


        add(painelNav, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        painelNav.add(btnFavoritos);

        painelStatus.add(lblStatusZoom, BorderLayout.WEST);
        add(painelStatus, BorderLayout.SOUTH);

        // Ações
        btnAnterior.addActionListener(e -> voltarPagina());
        btnProximo.addActionListener(e -> avancarPagina());
        btnFavoritos.addActionListener(e -> painelLateralFavoritos.setVisible(!painelLateralFavoritos.isVisible()));

        btnModo.addActionListener(e -> {
            modoDuplo = !modoDuplo;
            labelPagDireita.setVisible(modoDuplo);
            renderizarPaginas();
        });

        btnNoite.addActionListener(e -> {
            modoNoturno = !modoNoturno;
            renderizarPaginas();
        });

        scrollPane.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                // Se segurar CTRL, roda pra cima (negativo) dá Zoom In, pra baixo dá Zoom Out
                if (e.getWheelRotation() < 0) {
                    ajustarZoom(0.1);
                } else {
                    ajustarZoom(-0.1);
                }
            } else {
                // Se não estiver segurando CTRL, faz a rolagem vertical normal
                JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
                scrollBar.setValue(scrollBar.getValue() + (e.getWheelRotation() * 40));
            }
        });

        painelNav.add(btnNoite);
        configurarAtalhosTeclado();
        carregarPDF();

        //configuração lista de favoritos
        listFavoritos.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value.toString().contains("Grifado")) {
                    label.setIcon(new FlatSVGIcon("icons/edit_note.svg", 16, 16));
                } else {
                    label.setIcon(new FlatSVGIcon("icons/bookmark.svg", 16, 16));
                }
                label.setBorder(new EmptyBorder(5, 5, 5, 5));
                return label;
            }
        });

        // Redimensionamento dinâmico
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) { renderizarPaginas(); }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                livro.setUltimaPagina(paginaAtual);

                // Passa os favoritos do model da interface para a lista do objeto
                ArrayList<String> favsParaSalvar = new ArrayList<>();
                for (int i = 0; i < modelFavoritos.getSize(); i++) {
                    favsParaSalvar.add(modelFavoritos.getElementAt(i));
                }
                livro.setFavoritos(favsParaSalvar);

                fecharPDF();
                Salvar.salvarDados(listaParaSalvar, "resources/dados/lendo.json");
            }
        });

        // Salvar ao fechar
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                livro.setUltimaPagina(paginaAtual);
                fecharPDF();
                Salvar.salvarDados(listaParaSalvar, "resources/dados/lendo.json");
            }
        });
    }

    void configurarEventosMouse(JLabel label) {
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (modoMarcador || e.isShiftDown()) {
                    pontoInicial = e.getPoint();
                    pontoArrasto = e.getPoint(); // Inicia o arrasto no mesmo ponto
                } else {
                    pontoInicial = null;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (pontoInicial != null) {
                    // Aplica o grifo definitivo na imagem
                    aplicarDestaque(label, pontoInicial, e.getPoint());

                    int numPag = (label == labelPagDireita) ? (paginaAtual + 2) : (paginaAtual + 1);
                    String item = "Página " + numPag + " (Grifo)";

                    if (modelFavoritos != null && !modelFavoritos.contains(item)) {
                        modelFavoritos.addElement(item);
                    }

                    // Limpa as variáveis e atualiza a tela
                    pontoInicial = null;
                    pontoArrasto = null;
                    label.repaint();
                }
            }
        });

        // --- NOVO: Listener para o movimento do mouse ---
        label.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (pontoInicial != null) {
                    pontoArrasto = e.getPoint();
                    label.repaint(); // Pede para a interface se desenhar novamente mostrando o retângulo
                }
            }
        });
    }

    private void desenharRetanguloTemporario(Graphics g) {
        if (pontoInicial != null && pontoArrasto != null) {
            Graphics2D g2d = (Graphics2D) g.create();

            // Cor amarela transparente
            g2d.setColor(new Color(255, 255, 0, 100));

            int x = Math.min(pontoInicial.x, pontoArrasto.x);
            int y = Math.min(pontoInicial.y, pontoArrasto.y);
            int width = Math.max(Math.abs(pontoInicial.x - pontoArrasto.x), 1);
            int height = Math.max(Math.abs(pontoInicial.y - pontoArrasto.y), 1);

            g2d.fillRect(x, y, width, height);

            // Opcional: desenha uma bordinha laranja para enxergar melhor a área
            g2d.setColor(new Color(255, 140, 0, 150));
            g2d.drawRect(x, y, width, height);

            g2d.dispose();
        }
    }

    private void configurarJanela() {
        setTitle("Lendo: " + livro.getNomeDoLivro());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void configurarAtalhosTeclado() {
        JComponent content = (JComponent) getContentPane();
        InputMap im = content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = content.getActionMap();

        // CTRL + M: Alternar Marcador
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK), "toggleMarcador");
        am.put("toggleMarcador", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modoMarcador = !modoMarcador;
                setCursor(modoMarcador ? new Cursor(Cursor.CROSSHAIR_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
                lblStatusZoom.setText(modoMarcador ? " MODO MARCADOR ATIVO" : " Zoom: " + (int)(fatorZoom * 100) + "%");
            }
        });

        // CTRL + D: Adicionar Favorito
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "addFav");
        am.put("addFav", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adicionarFavoritoManual();
            }
        });

        // Setas: Navegação de Página
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "next");
        am.put("next", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { avancarPagina(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "prev");
        am.put("prev", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { voltarPagina(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "scrollDown");
        am.put("scrollDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                verticalBar.setValue(verticalBar.getValue() + 60);
            }
        });

        // Seta para Cima (Scroll Up)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "scrollUp");
        am.put("scrollUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                verticalBar.setValue(verticalBar.getValue() - 60);
            }
        });

        // CTRL + "+" (Zoom In) - Suporta tanto o teclado numérico quanto o normal
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK), "zoomIn");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK), "zoomIn2");
        am.put("zoomIn", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { ajustarZoom(0.1); } });
        am.put("zoomIn2", am.get("zoomIn"));

        // CTRL + "-" (Zoom Out)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK), "zoomOut");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK), "zoomOut2");
        am.put("zoomOut", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { ajustarZoom(-0.1); } });
        am.put("zoomOut2", am.get("zoomOut"));

        // CTRL + "0" (Resetar Zoom para 100%)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK), "zoomReset");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, InputEvent.CTRL_DOWN_MASK), "zoomReset2");
        am.put("zoomReset", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                fatorZoom = 1.0;
                lblStatusZoom.setText("Zoom: 100%");
                renderizarPaginas();
            }
        });
        am.put("zoomReset2", am.get("zoomReset"));
    }

    // Método auxiliar para o favorito manual
    void adicionarFavoritoManual() {
        if (!favoritos.contains(paginaAtual)) {
            favoritos.add(paginaAtual);
            modelFavoritos.addElement("Página " + (paginaAtual + 1));
            JOptionPane.showMessageDialog(this, "Página " + (paginaAtual + 1) + " favoritada!");
        }
    }

    // Método auxiliar para facilitar o registro das setas
    private void registrarSeta(InputMap im, ActionMap am, int key, String nome, Runnable acao) {
        im.put(KeyStroke.getKeyStroke(key, 0), nome);
        am.put(nome, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acao.run();
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        });
    }

    private void ajustarZoom(double incremento) {
        fatorZoom += incremento;
        if (fatorZoom < 0.5) fatorZoom = 0.5;
        if (fatorZoom > 3.0) fatorZoom = 3.0;

        lblStatusZoom.setText("Zoom: " + (int)(fatorZoom * 100) + "%");
        renderizarPaginas();
    }

    void aplicarDestaque(JLabel label, Point p1, Point p2) {
        ImageIcon icon = (ImageIcon) label.getIcon();
        if (icon == null) return;

        // 1. Descobrir onde a imagem começa dentro do JLabel (Offset)
        int offsetX = 0;
        int offsetY = (label.getHeight() - icon.getIconHeight()) / 2; // O Swing centraliza verticalmente por padrão

        if (label.getHorizontalAlignment() == SwingConstants.RIGHT) {
            offsetX = label.getWidth() - icon.getIconWidth();
        } else if (label.getHorizontalAlignment() == SwingConstants.CENTER) {
            offsetX = (label.getWidth() - icon.getIconWidth()) / 2;
        } // Se for LEFT, o offsetX continua 0

        // 2. Traduzir a coordenada do mouse para a coordenada real da IMAGEM
        int startX = p1.x - offsetX;
        int startY = p1.y - offsetY;
        int endX = p2.x - offsetX;
        int endY = p2.y - offsetY;

        // 3. Preparar a pintura
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.drawImage(icon.getImage(), 0, 0, null);

        // Cor amarela translúcida
        g2d.setColor(new Color(255, 255, 0, 100));

        // 4. Calcular o tamanho do quadrado com as coordenadas ajustadas
        int x = Math.min(startX, endX);
        int y = Math.min(startY, endY);
        int width = Math.max(Math.abs(startX - endX), 5);
        int height = Math.max(Math.abs(startY - endY), 5);

        // 5. Desenha apenas se o grifo estiver dentro da área da imagem
        if (x < w && y < h && (x + width) > 0 && (y + height) > 0) {
            g2d.fillRect(x, y, width, height);

            // Descobre a página atual
            int numPag = (label == labelPagDireita) ? (paginaAtual + 1) : paginaAtual;

            // Calcula a proporção
            double proporcaoX = (double) x / w;
            double proporcaoY = (double) y / h;
            double proporcaoW = (double) width / w;
            double proporcaoH = (double) height / h;

            // Salva no objeto Cadastro (livro)
            if (livro.getGrifos() != null) {
                livro.getGrifos().add(new Grifo(numPag, proporcaoX, proporcaoY, proporcaoW, proporcaoH));
            }
        }
        g2d.dispose();

        // 6. Atualiza a tela
        label.setIcon(new ImageIcon(bi));
        label.repaint();
    }

    void avancarPagina() {
        int pulo = modoDuplo ? 2 : 1;
        if (paginaAtual + pulo < totalPaginas) {
            paginaAtual += pulo;
            renderizarPaginas();
        }
    }

    void voltarPagina() {
        int pulo = modoDuplo ? 2 : 1;
        if (paginaAtual - pulo >= 0) {
            paginaAtual -= pulo;
            renderizarPaginas();
        }
    }

    private void renderizarPaginas() {
        if (documento == null || scrollPane.getWidth() <= 0) return;
        try {
            int gap = modoDuplo ? 80 : 120;
            // Multiplicamos a largura disponível pelo fator de zoom
            int larguraBase = (scrollPane.getWidth() / (modoDuplo ? 2 : 1)) - gap;
            int larguraComZoom = (int) (larguraBase * fatorZoom);

            labelPagEsquerda.setIcon(new ImageIcon(gerarImagem(paginaAtual, larguraComZoom)));
            if (modoDuplo && paginaAtual + 1 < totalPaginas) {
                labelPagDireita.setIcon(new ImageIcon(gerarImagem(paginaAtual + 1, larguraComZoom)));
                lblPagina.setText("Páginas " + (paginaAtual + 1) + "-" + (paginaAtual + 2) + " de " + totalPaginas);
            } else {
                lblPagina.setText("Página " + (paginaAtual + 1) + " de " + totalPaginas);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Image gerarImagem(int index, int larguraAlvo) throws IOException {
        BufferedImage img = renderer.renderImageWithDPI(index, 110);
        int imgW = img.getWidth();
        int imgH = img.getHeight();

        // 1. Desenhar Grifos
        if (livro.getGrifos() != null) {
            Graphics2D g2dGrifo = img.createGraphics();
            g2dGrifo.setColor(new Color(255, 255, 0, 100)); // Amarelo translúcido

            for (Grifo g : livro.getGrifos()) {
                if (g.getPagina() == index) {
                    // Converte a porcentagem de volta para pixels
                    int rx = (int) (g.getxRatio() * imgW);
                    int ry = (int) (g.getyRatio() * imgH);
                    int rw = (int) (g.getwRatio() * imgW);
                    int rh = (int) (g.gethRatio() * imgH);
                    g2dGrifo.fillRect(rx, ry, rw, rh);
                }
            }
            g2dGrifo.dispose();
        }

        // 2. Modo Noturno Otimizado (LookupOp é MUITO mais rápido que laço de repetição)
        if (modoNoturno) {
            short[] invert = new short[256];
            for (int i = 0; i < 256; i++) {
                invert[i] = (short) (255 - i);
            }
            java.awt.image.ShortLookupTable table = new java.awt.image.ShortLookupTable(0, invert);
            java.awt.image.LookupOp op = new java.awt.image.LookupOp(table, null);
            img = op.filter(img, null);
        }

        // 3. Redimensionamento Otimizado (Substitui o lento getScaledInstance)
        double ratio = (double) larguraAlvo / imgW;
        int novaAltura = (int) (imgH * ratio);

        BufferedImage imagemRedimensionada = new BufferedImage(larguraAlvo, novaAltura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagemRedimensionada.createGraphics();

        // Define qualidade de renderização suave (Bilinear)
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(img, 0, 0, larguraAlvo, novaAltura, null);
        g2d.dispose();

        // Atualiza o texto do status do zoom
        lblStatusZoom.setText("Zoom: " + (int)(fatorZoom * 100) + "% | Página: " + (index + 1));

        return imagemRedimensionada;
    }

    private JButton criarBotaoComIcone(String texto, String iconePath) {
        FlatSVGIcon icon = new FlatSVGIcon("icons/" + iconePath, 20, 20);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));

        JButton btn = new JButton(texto, icon);
        btn.setBackground(new Color(50, 50, 50));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        return btn;
    }

    private void carregarPDF() {
        try {
            documento = Loader.loadPDF(new File(livro.getPathPDF()));
            renderer = new PDFRenderer(documento);
            totalPaginas = documento.getNumberOfPages();
            renderizarPaginas();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar PDF.");
        }
    }

    private void fecharPDF() {
        try { if (documento != null) documento.close(); } catch (IOException e) {}
    }
}