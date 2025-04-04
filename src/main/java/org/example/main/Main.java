package org.example.main;

import org.example.controller.DocumentGenerator;
import org.example.controller.FileManager;
import org.example.model.TagDatabase;
import org.example.model.TagExtractor;
import org.example.view.ViewModelStartScreen;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static JFrame frame;
    public static ViewModelStartScreen viewModelStartScreen;
    public static DocumentGenerator documentGenerator;
    public static FileManager fileManager;
    public static TagDatabase tagDatabase;
    public static TagExtractor tagExtractor;
    private static JPanel mainPanel;
    private static CardLayout cardLayout;
    private static Map<String, JPanel> panelMap;
    public static final String PANEL_START_SCREEN = "startScreen";
    public static final String PANEL_TEXT_FIELDS = "textFields";
    public static final String PANEL_TABLE = "table";

    private static void Init() {
        fileManager = new FileManager();
        documentGenerator = new DocumentGenerator(fileManager);
        viewModelStartScreen = new ViewModelStartScreen();
        panelMap = new HashMap<>();
        cardLayout = new CardLayout();
        tagDatabase = new TagDatabase();
        tagExtractor = new TagExtractor();

        // Регистрация начальной панели
        panelMap.put(PANEL_START_SCREEN, viewModelStartScreen);
        panelMap.put(PANEL_TEXT_FIELDS, viewModelStartScreen.viewModelTextFields);
        panelMap.put(PANEL_TABLE, viewModelStartScreen.viewModelTable);

        generateFrame();
    }

    private static void generateFrame() {
        frame = new JFrame("Генерация документов");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1200, 700));
        frame.setSize(1200, 750);

        mainPanel = new JPanel(cardLayout);
        // Добавление всех зарегистрированных панелей
        for (Map.Entry<String, JPanel> entry : panelMap.entrySet()) {
            mainPanel.add(entry.getValue(), entry.getKey());
        }

        frame.getContentPane().add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        cardLayout.show(mainPanel, PANEL_START_SCREEN);
    }

    // Переключение панелей по строковому ключу
    public static void switchToPanel(String panelName) {
        if (panelMap.containsKey(panelName)) {
            cardLayout.show(mainPanel, panelName);
        } else {
            System.err.println("Панель '" + panelName + "' не найдена.");
        }
    }

    public static void main(String[] args) {
        Main.Init();
    }
}