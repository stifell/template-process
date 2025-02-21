package org.example.main;

import org.example.controller.DocumentGenerator;
import org.example.controller.FileManager;
import org.example.view.ViewModelStartScreen;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public JFrame frame;
    public ViewModelStartScreen viewModelStartScreen;
    private DocumentGenerator documentGenerator;
    private FileManager fileManager;
    private JPanel mainPanel; // Панель, которая будет использовать CardLayout
    private CardLayout cardLayout; // Менеджер компоновки для переключения панелей
    private Map<String, JPanel> panelMap; // Карта для хранения панелей по их именам

    private Main() {
        fileManager = new FileManager();
        documentGenerator = new DocumentGenerator(fileManager);
        viewModelStartScreen = new ViewModelStartScreen(this, documentGenerator, fileManager);
        panelMap = new HashMap<>(); // Инициализация карты для хранения панелей
        cardLayout = new CardLayout(); // Инициализация CardLayout
        generateFrame();
    }

    public void generateFrame() {
        frame = new JFrame("Генерация документов"); // Создаем главное окно
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Устанавливаем операцию закрытия
        frame.setMinimumSize(new Dimension(1200, 700));
        frame.setSize(1200, 750); // Устанавливаем начальный размер окна

        // Создаем панель с CardLayout и добавляем в неё начальный экран
        mainPanel = new JPanel(cardLayout);
        mainPanel.add(viewModelStartScreen, "startScreen");
        frame.getContentPane().add(mainPanel); // Добавляем mainPanel в контейнер главного окна
        frame.setLocationRelativeTo(null);
        frame.setVisible(true); // Делаем окно видимым
    }

    // Функция для переключения панелей
    public void switchToPanel(JPanel newPanel) {
        String panelName = newPanel.getClass().getSimpleName(); // Используем имя класса в качестве ключа

        if (!panelMap.containsKey(panelName)) {
            panelMap.put(panelName, newPanel); // Если панель ещё не добавлена, добавляем её в карту и mainPanel
            mainPanel.add(newPanel, panelName);
        }
        cardLayout.show(mainPanel, panelName); // Переключаемся на нужную панель
    }

    public static void main(String[] args) {
        new Main();
    }
}
