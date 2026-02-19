package Cadastro;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Configurações de arredondamento globais (Devem vir ANTES do setup)
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("ProgressBar.arc", 15);

            // 2. Deixa a barra de rolagem bem discreta e moderna
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 6);

            // 3. Aplica o Tema Dark
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
            FlatDarkLaf.setup();

            // 4. Inicia a aplicação
            SwingUtilities.invokeLater(() -> {
                new Tela().setVisible(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}