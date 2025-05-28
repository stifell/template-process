package org.example.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.controller.DocumentGenerator;
import org.example.controller.FileManager;
import org.example.model.AppState;
import org.example.model.TagDatabase;
import org.example.model.TagExtractor;
import org.example.model.TagMap;
import org.example.view.ViewModelStartScreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static JFrame frame;
    public static ViewModelStartScreen viewModelStartScreen;
    public static DocumentGenerator documentGenerator;
    public static FileManager fileManager;
    public static TagDatabase tagDatabase;
    public static TagExtractor tagExtractor;
    public static AppState appState = new AppState();
    private static JPanel mainPanel;
    private static CardLayout cardLayout;
    private static Map<String, JPanel> panelMap;
    public static TagMap tagMap = new TagMap();
    public static boolean shouldSaveState = false;
    public static final String PANEL_START_SCREEN = "startScreen";
    public static final String PANEL_TEXT_FIELDS = "textFields";
    public static final String PANEL_TABLE = "table";

    private static void saveAppState() {
        if(shouldSaveState) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Path path = Paths.get(System.getProperty("user.home"), "DocCraft", "appstate.json");
            try {
                Files.write(path, gson.toJson(appState).getBytes());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void loadAppState() {
        Path path = Paths.get(System.getProperty("user.home"), "DocCraft", "appstate.json");
        if (Files.exists(path)) {
            try {
                String json = new String(Files.readAllBytes(path));
                appState = new Gson().fromJson(json, AppState.class);

                // Заменяем TagMap на обычный HashMap
                Main.tagMap.clear();
                if (appState.getTagValues() != null) {
                    Main.tagMap.putAll(appState.getTagValues());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static void Init() {
        loadAppState();
        fileManager = new FileManager();
        fileManager.setTargetFolderPath(appState.getSaveDirectory());
        documentGenerator = new DocumentGenerator(fileManager);
        tagExtractor = new TagExtractor();
        tagDatabase = new TagDatabase();
        viewModelStartScreen = new ViewModelStartScreen();
        panelMap = new HashMap<>();
        cardLayout = new CardLayout();


        // Регистрация начальной панели
        panelMap.put(PANEL_START_SCREEN, viewModelStartScreen);
        panelMap.put(PANEL_TEXT_FIELDS, viewModelStartScreen.viewModelTextFields);
        panelMap.put(PANEL_TABLE, viewModelStartScreen.viewModelTable);

        generateFrame();
    }

    private static void generateFrame() {
        frame = new JFrame("Генерация документов");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1200, 800));
        frame.setSize(1200, 850);

        mainPanel = new JPanel(cardLayout);
        // Добавление всех зарегистрированных панелей
        for (Map.Entry<String, JPanel> entry : panelMap.entrySet()) {
            mainPanel.add(entry.getValue(), entry.getKey());
        }

        frame.getContentPane().add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveAppState(); // Сохраняем все состояние
            }
        });
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