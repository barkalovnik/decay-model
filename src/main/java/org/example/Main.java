package org.example;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DecayController controller = new DecayController();
            controller.start();                 // запуск в отдельном потоке
            new MainFrame(controller);          // создание GUI
        });
    }
}
