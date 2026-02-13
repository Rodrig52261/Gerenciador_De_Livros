package Cadastro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class Salvar {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // CORREÇÃO: Removido o prefixo Cadastro.Main.java do tipo da lista
    public static void salvarDados(ArrayList<Cadastro> lista, String nomeArquivo) {
        try (Writer writer = new FileWriter(nomeArquivo)) {
            gson.toJson(lista, writer);
        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo: " + e.getMessage());
        }
    }

    public static ArrayList<Cadastro> carregarDados(String nomeArquivo) {
        File arquivo = new File(nomeArquivo);

        // Se o arquivo não existir, retorna uma lista vazia IMEDIATAMENTE
        if (!arquivo.exists()) {
            System.out.println("Arquivo " + nomeArquivo + " não encontrado. Criando lista nova.");
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(arquivo)) {
            Type tipoLista = new TypeToken<ArrayList<Cadastro>>() {}.getType();
            ArrayList<Cadastro> lista = gson.fromJson(reader, tipoLista);

            // Se o arquivo existir mas estiver totalmente vazio, o Gson retorna null.
            // Por isso, garantimos o retorno de uma lista vazia nesse caso também.
            return (lista != null) ? lista : new ArrayList<>();

        } catch (IOException e) {
            System.err.println("Erro ao carregar dados: " + e.getMessage());
            return new ArrayList<>(); // Retorno de segurança
        }
    }
}