package Cadastro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class Salvar {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Método para salvar qualquer lista de livros em um arquivo específico
    public static void salvarDados(ArrayList<Cadastro> lista, String nomeArquivo) {
        try (FileWriter writer = new FileWriter(nomeArquivo)) {
            gson.toJson(lista, writer);
        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo " + nomeArquivo + ": " + e.getMessage());
        }
    }

    // Método para carregar os dados de volta para a lista
    public static ArrayList<Cadastro> carregarDados(String nomeArquivo) {
        File arquivo = new File(nomeArquivo);
        if (!arquivo.exists()) {
            return new ArrayList<>(); // Retorna lista vazia se o arquivo não existir ainda
        }

        try (FileReader reader = new FileReader(nomeArquivo)) {
            Type tipoLista = new TypeToken<ArrayList<Cadastro>>(){}.getType();
            ArrayList<Cadastro> lista = gson.fromJson(reader, tipoLista);
            return (lista != null) ? lista : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }


}
