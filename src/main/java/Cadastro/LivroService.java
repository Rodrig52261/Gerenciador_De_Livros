package Cadastro;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LivroService {

        public static String[] buscarInfoExtra(String titulo) {
            String[] resultados = {"", ""}; // [0] Biografia, [1] Gênero
            try {
                String query = URLEncoder.encode(titulo, StandardCharsets.UTF_8);
                URL url = new URL("https://www.googleapis.com/books/v1/volumes?q=intitle:" + query);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) sb.append(line);

                String json = sb.toString();
                // Extração simples (sem bibliotecas extras de JSON para manter o projeto leve)
                if (json.contains("\"description\": \"")) {
                    int start = json.indexOf("\"description\": \"") + 16;
                    int end = json.indexOf("\"", start);
                    resultados[0] = json.substring(start, end).replace("\\n", "\n");
                }
                if (json.contains("\"categories\": [")) {
                    int start = json.indexOf("\"categories\": [") + 16;
                    int end = json.indexOf("]", start);
                    resultados[1] = json.substring(start, end).replace("\"", "");
                }
            } catch (Exception e) {
                resultados[0] = "Erro ao buscar biografia online.";
            }
            return resultados;
        }
}
