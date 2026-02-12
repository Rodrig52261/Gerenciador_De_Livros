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
        if (!arquivo.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(nomeArquivo)) {
            Type tipoLista = new TypeToken<ArrayList<Cadastro>>() {}.getType();
            ArrayList<Cadastro> lista = gson.fromJson(reader, tipoLista);
            return (lista != null) ? lista : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Erro ao carregar arquivo: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}