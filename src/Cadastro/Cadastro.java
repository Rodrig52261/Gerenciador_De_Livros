package Cadastro;

import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;

public class Cadastro {

    private String nomeDoLivro;
    private String autor;
    private String biografia;
    private int qtdPag;
    private int paginasLidas;;

    public String getNomeDoLivro() { return nomeDoLivro; }
    public String getAutor() { return autor; }
    public String getBiografia() { return biografia; }

    @Override
    public String toString() {
        // Se o nome estiver vazio, ele mostra "Livro sem título" para não ficar um espaço em branco
        return (nomeDoLivro != null && !nomeDoLivro.isEmpty()) ? nomeDoLivro : "Sem Título";
    }

    public int getQtdPag() { return qtdPag; }

    public void exibirCadastro(){
        System.out.println("Nome: " + this.nomeDoLivro);
        System.out.println("Autor: " +this.autor);
        System.out.println("Biografia: " +this.biografia);
        System.out.println("Quantidade de páginas: " +this.qtdPag);
    }

    public String getDados() {
        return String.format(
                        "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓\n" +
                        "  📖 LIVRO: %-30s\n" +
                        "  ✍️  AUTOR: %-30s\n" +
                        "  📊 PROGRESSO: [%d%%]\n" +
                        "  📝 BIO: %-30s\n" + "" +
                        "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛\n",
                this.nomeDoLivro.toUpperCase(),
                this.autor,
                calcularPorcentagem(),
                this.biografia
        );
    }

    public void criarCadastro(String nomeDoLivro, String autor, String biografia, int qtdPag, int paginasLidas){
        this.nomeDoLivro = nomeDoLivro;
        this.autor = autor;
        this.biografia = biografia;
        this.qtdPag = qtdPag;
        this.paginasLidas = paginasLidas;
    }

    // Método para calcular a porcentagem
    public int calcularPorcentagem() {
        if (this.qtdPag == 0) return 0;
        return (this.paginasLidas * 100) / this.qtdPag;
    }

    public static void salvarEmArquivo(ArrayList<Cadastro> livros, String nomeArquivo) {
        try {
            FileWriter writer = new FileWriter(nomeArquivo, false);
            for (Cadastro livro : livros) {
                writer.write("Nome: " + livro.nomeDoLivro + "\n");
                writer.write("Autor: " + livro.autor + "\n");
                writer.write("Biografia: " + livro.biografia + "\n");
                writer.write("Páginas: " + livro.qtdPag + "\n");
                writer.write("-----------------------------\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Erro ao salvar: " + e.getMessage());
        }
    }

    public static void carregarDoArquivo() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("livros.txt"));
            String linha;
            int contador = 1;
            System.out.println("\n=============================");
            while ((linha = reader.readLine()) != null) {
                if (linha.equals("-----------------------------")) {
                    System.out.println("=============================");
                    contador++;
                } else {
                    if (linha.startsWith("Nome:")) {
                        System.out.println("\n        LIVRO ");
                        System.out.println("=============================");
                    }
                    System.out.println(linha);
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Nenhum arquivo encontrado.");
        }
    }
}
