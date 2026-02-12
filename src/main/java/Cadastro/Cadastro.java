package Cadastro;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Cadastro {

    private String nomeDoLivro;
    private String autor;
    private String biografia;
    private int qtdPag;
    private int paginasLidas; // Você pode usar isso como o progresso total
    private String pathPDF;
    private int ultimaPagina; // Onde o marcador de página realmente fica

    // --- GETTERS ---
    public String getNomeDoLivro() { return nomeDoLivro; }
    public String getAutor() { return autor; }
    public String getBiografia() { return biografia; }
    public int getQtdPag() { return qtdPag; }
    public int getPaginasLidas() { return paginasLidas; }
    public String getPathPDF() { return pathPDF; }

    // CORREÇÃO: Getter para retornar a última página salva
    public int getUltimaPagina() {
        return ultimaPagina;
    }

    // --- SETTERS ---
    public void setPathPDF(String pathPDF) { this.pathPDF = pathPDF; }

    // CORREÇÃO: Setter para salvar a página e atualizar o progresso lido
    public void setUltimaPagina(int ultimaPagina) {
        this.ultimaPagina = ultimaPagina;
        // Opcional: sincronizar paginasLidas com a última página vista
        if (ultimaPagina > this.paginasLidas) {
            this.paginasLidas = ultimaPagina;
        }
    }

    @Override
    public String toString() {
        return (nomeDoLivro != null && !nomeDoLivro.isEmpty()) ? nomeDoLivro : "Sem Título";
    }

    public void criarCadastro(String nomeDoLivro, String autor, String biografia, int qtdPag, int paginasLidas, String pathPDF){
        this.nomeDoLivro = nomeDoLivro;
        this.autor = autor;
        this.biografia = biografia;
        this.qtdPag = qtdPag;
        this.paginasLidas = paginasLidas;
        this.pathPDF = pathPDF;
        this.ultimaPagina = paginasLidas; // Inicia o marcador na página onde parou
    }

    public void criarCadastro(String nomeDoLivro, String autor, String biografia, int qtdPag, int paginasLidas){
        this.criarCadastro(nomeDoLivro, autor, biografia, qtdPag, paginasLidas, null);
    }

    public int calcularPorcentagem() {
        if (this.qtdPag == 0) return 0;
        return (this.paginasLidas * 100) / this.qtdPag;
    }

    // --- PERSISTÊNCIA ---
    public static void salvarEmArquivo(ArrayList<Cadastro> livros, String nomeArquivo) {
        try (FileWriter writer = new FileWriter(nomeArquivo, false)) {
            for (Cadastro livro : livros) {
                writer.write("Nome: " + livro.nomeDoLivro + "\n");
                writer.write("Autor: " + livro.autor + "\n");
                writer.write("Biografia: " + livro.biografia + "\n");
                writer.write("Páginas: " + livro.qtdPag + "\n");
                writer.write("Lidas: " + livro.paginasLidas + "\n");
                // IMPORTANTE: Salvar a última página no TXT para recuperar depois
                writer.write("UltimaPagina: " + livro.ultimaPagina + "\n");
                writer.write("PDF: " + (livro.pathPDF != null ? livro.pathPDF : "Nenhum") + "\n");
                writer.write("-----------------------------\n");
            }
            System.out.println("Arquivo atualizado com sucesso!");
        } catch (IOException e) {
            System.out.println("Erro ao salvar: " + e.getMessage());
        }
    }
}