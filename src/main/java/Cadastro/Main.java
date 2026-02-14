package Cadastro;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Seu código para iniciar a tela
        java.awt.EventQueue.invokeLater(() -> {
            new Tela().setVisible(true);
        });

        try {
            // 1. Configurações de arredondamento globais
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("ProgressBar.arc", 15);

            // 2. Deixa a barra de rolagem bem discreta e moderna
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 6);

            // 3. Aplica o Tema Dark
            com.formdev.flatlaf.FlatDarkLaf.setup();

            // Inicia a aplicação
            SwingUtilities.invokeLater(() -> {
                new Tela().setVisible(true);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}