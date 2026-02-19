package Cadastro;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class LivroService {

    private static final String API_KEY = ""; // Deixe vazio por enquanto, adicione depois se precisar

    /**
     * Busca informações de um único livro (usado no carregamento automático)
     */
    public static String[] buscarInfoExtra(String titulo) {
        String[] resultados = {"Sem sinopse disponível.", "Geral"};
        try {
            String query = URLEncoder.encode(titulo, StandardCharsets.UTF_8);
            String urlString = "https://www.googleapis.com/books/v1/volumes?q=intitle:" + query;
            if (!API_KEY.isEmpty()) urlString += "&key=" + API_KEY;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) sb.append(line);
            rd.close();

            String json = sb.toString();

            if (json.contains("\"description\":")) {
                int start = json.indexOf("\"description\":") + 14;
                while (json.charAt(start) == ' ' || json.charAt(start) == '\"') start++;
                int end = json.indexOf("\",", start);
                if (end == -1) end = json.indexOf("\"", start);
                if (end > start) {
                    String bio = json.substring(start, end);
                    resultados[0] = bio.replace("\\n", "\n").replace("\\\"", "\"");
                }
            }

            if (json.contains("\"categories\":")) {
                int start = json.indexOf("\"categories\":") + 13;
                int end = json.indexOf("]", start);
                if (end > start) {
                    resultados[1] = json.substring(start, end).replaceAll("[\\[\\]\"]", "").trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultados[0] = "Erro ao buscar biografia online.";
        }
        return resultados;
    }

    /**
     * Busca múltiplos livros para seleção manual (usado no SearchBooks)
     */
    public static ArrayList<Cadastro> buscarLivros(String termo, int maxResultados) {
        ArrayList<Cadastro> lista = new ArrayList<>();
        try {
            // ADICIONADO: Delay de 2 segundos antes da requisição para evitar 429
            Thread.sleep(2000);

            String query = URLEncoder.encode(termo, StandardCharsets.UTF_8);
            String urlString = "https://www.googleapis.com/books/v1/volumes?q=" + query
                    + "&maxResults=" + maxResultados + "&langRestrict=pt";
            if (!API_KEY.isEmpty()) urlString += "&key=" + API_KEY;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Verifica o código de resposta
            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode); // Log para debug

            if (responseCode == 429) {
                System.out.println("ERRO 429: Limite de requisições excedido. Aguarde alguns minutos.");
                return lista;
            }

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) sb.append(line);
            rd.close();

            String json = sb.toString();

            // Processa os resultados
            int pos = 0;
            int contador = 0;

            while ((pos = json.indexOf("\"volumeInfo\":", pos)) != -1 && contador < maxResultados) {
                contador++;
                int fim = Math.min(pos + 3000, json.length());
                String trecho = json.substring(pos, fim);

                Cadastro c = new Cadastro();

                String titulo = extrair(trecho, "\"title\": \"");
                String autor = extrair(trecho, "\"authors\": [ \"", "\"authors\": [\n          \"");
                String biografia = extrair(trecho, "\"description\": \"");
                String capa = extrair(trecho, "\"thumbnail\": \"").replace("http:", "https:");

                c.setNomeDoLivro(titulo);
                c.setAutor(autor.isEmpty() ? "Autor desconhecido" : autor);
                c.setBiografia(biografia);
                c.setPathCapa(capa);

                try {
                    int inicioPage = trecho.indexOf("\"pageCount\":");
                    if (inicioPage != -1) {
                        String numeroStr = trecho.substring(inicioPage + 12).trim().split("[,}]")[0].trim();
                        c.setQtdPag(Integer.parseInt(numeroStr));
                    }
                } catch (Exception e) {
                    c.setQtdPag(0);
                }

                if (!titulo.isEmpty()) {
                    lista.add(c);
                }

                pos += 100;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao buscar livros: " + e.getMessage());
        }
        return lista;
    }

    private static String extrair(String trecho, String... chaves) {
        for (String chave : chaves) {
            if (trecho.contains(chave)) {
                int start = trecho.indexOf(chave) + chave.length();
                int end = trecho.indexOf("\"", start);
                if (end > start) {
                    return trecho.substring(start, end)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"");
                }
            }
        }
        return "";
    }
}