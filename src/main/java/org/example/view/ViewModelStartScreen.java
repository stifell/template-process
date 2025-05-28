package org.example.view;

import org.example.main.Main;
import org.example.model.AppState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.example.main.Main.appState;
import static org.example.main.Main.fileManager;


public class ViewModelStartScreen extends JPanel {

    public ViewModelTextFields viewModelTextFields;
    public ViewModelTable viewModelTable;
    private JLabel labelChoosingGenerateMethod;
    private JLabel labelChooseCountOfAuthor;
    private JLabel universityLabel;
    private JLabel chosenDirectoryLabel;
    private JButton buttonGenerateWithTextFields;
    private JButton buttonGenerateWithTable;
    private JButton buttonEditPlaceholders;
    private JButton resetDataButton;
    private JButton chooseDirectoryButton;
    public JComboBox<Integer> authorComboBox;
    public boolean verification;
    public static int selectedNumber = 1;
    public static JCheckBox convertToPdfCheckBox;
    String[] select;

    // Константы для одинакового размера компонентов
    private static final Dimension COMPONENT_SIZE = new Dimension((int) (250 * 1.4), (int) (40 * 1.4));
    private static final Dimension LABEL_SIZE = new Dimension((int) (250 * 1.4), (int) (50 * 1.4)); // Размер надписи

    public ViewModelStartScreen() {
        viewModelTextFields = new ViewModelTextFields();
        viewModelTable = new ViewModelTable();

        initializeStartScreen();
        authorComboBox.setSelectedItem(appState.getNumberOfAuthors());
        if (appState.getSaveDirectory() != null) {
            chosenDirectoryLabel.setText("Путь: " + appState.getSaveDirectory());
            buttonGenerateWithTextFields.setEnabled(true);
            buttonGenerateWithTable.setEnabled(true);
        }

        authorComboBox.addActionListener(e ->
                appState.setNumberOfAuthors((int) authorComboBox.getSelectedItem()));
    }

    private void initializeStartScreen() {
        // Применяем стиль к текущей панели
        ViewStyles.stylePanel(this);
        // Устанавливаем GridBagLayout как менеджер компоновки
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0); // Отступы по умолчанию: сверху, слева, снизу, справа
        gbc.anchor = GridBagConstraints.CENTER; // Центрируем элементы
        gbc.fill = GridBagConstraints.NONE; // Компоненты не растягиваются
        // Создаем и стилизуем метку для выбора количества авторов
        labelChooseCountOfAuthor = new JLabel("Выберите количество авторов");
        ViewStyles.styleLabel(labelChooseCountOfAuthor);
        labelChooseCountOfAuthor.setPreferredSize(COMPONENT_SIZE); // Устанавливаем размер
        // Устанавливаем отступ слева для метки
        gbc.insets = new Insets(10, 10, 0, 0);
        gbc.gridy = 2;
        add(labelChooseCountOfAuthor, gbc);

        // Создаем и стилизуем JComboBox для выбора количества авторов
        Integer[] numbers = new Integer[9];
        for (int i = 0; i < 9; i++) {
            numbers[i] = i + 1;
        }
        authorComboBox = new JComboBox<>(numbers);
        ViewStyles.styleComboBox(authorComboBox);
        authorComboBox.setMaximumRowCount(9);
        authorComboBox.setPreferredSize(COMPONENT_SIZE); // Устанавливаем размер
        gbc.insets = new Insets(0, 0, 5, 0); // Сброс отступов для остальных элементов
        gbc.gridy = 3;
        add(authorComboBox, gbc);

        // Обработка выбора в JComboBox
        authorComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedNumber = (int) authorComboBox.getSelectedItem();
            }
        });
        universityLabel = new JLabel("RUDN University");
        ViewStyles.styleStartLabel(universityLabel);
        universityLabel.setPreferredSize(LABEL_SIZE);
        universityLabel.setHorizontalAlignment(SwingConstants.CENTER); // Центрируем текст
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0); // Отступ снизу для надписи
        add(universityLabel, gbc);

        chooseDirectoryButton = new JButton("Выбрать папку для сохранения");
        ViewStyles.styleButton(chooseDirectoryButton);
        chooseDirectoryButton.setPreferredSize(COMPONENT_SIZE);
        chooseDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setDialogTitle("Выберите папку для сохранения");
                fileChooser.setAcceptAllFileFilterUsed(false);  // Отключаем фильтр для всех типов файлов
                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedDirectory = fileChooser.getSelectedFile();
                    String chosenDirectoryPath = selectedDirectory.getAbsolutePath();
                    chosenDirectoryLabel.setText("Путь: " + chosenDirectoryPath);
                    fileManager.setTargetFolderPath(chosenDirectoryPath);
                    buttonGenerateWithTextFields.setEnabled(true);
                    buttonGenerateWithTable.setEnabled(true);
                    appState.setSaveDirectory(chosenDirectoryPath);
                }
            }
        });

        // Метка для отображения выбранного пути
        chosenDirectoryLabel = new JLabel("Путь не выбран");
        chosenDirectoryLabel.setPreferredSize(COMPONENT_SIZE);

        // Добавление кнопки и метки на экран
        gbc.insets = new Insets(10, 0, 0, 0);
        gbc.gridy = 4;
        add(chooseDirectoryButton, gbc);
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.gridy = 5;
        add(chosenDirectoryLabel, gbc);

        convertToPdfCheckBox = new JCheckBox("Конвертировать в .pdf?");
        ViewStyles.styleCheckBox(convertToPdfCheckBox);
        convertToPdfCheckBox.setPreferredSize(COMPONENT_SIZE);
        gbc.insets = new Insets(10, 0, 20, 0);
        gbc.gridy = 6;
        add(convertToPdfCheckBox, gbc);
        // Обрабатываем изменение состояния чекбокса
        convertToPdfCheckBox.setSelected(appState.isConvertToPdf());
        convertToPdfCheckBox.addActionListener(e ->
                appState.setConvertToPdf(convertToPdfCheckBox.isSelected())
        );
        // Создаем и стилизуем метку для выбора метода генерации
        labelChoosingGenerateMethod = new JLabel("Как сгенерировать документ?");
        ViewStyles.styleLabel(labelChoosingGenerateMethod);
        labelChoosingGenerateMethod.setPreferredSize(COMPONENT_SIZE); // Устанавливаем размер
        // Устанавливаем отступ слева для метки
        gbc.insets = new Insets(10, 10, 0, 0);
        gbc.gridy = 7;
        add(labelChoosingGenerateMethod, gbc);

        // Создаем и стилизуем кнопку для генерации с текстовыми полями
        buttonGenerateWithTextFields = new JButton("Использовать поля ввода");
        buttonGenerateWithTextFields.setEnabled(false);
        ViewStyles.styleButton(buttonGenerateWithTextFields);
        buttonGenerateWithTextFields.setPreferredSize(COMPONENT_SIZE); // Устанавливаем размер
        gbc.insets = new Insets(0, 0, 5, 0); // Сброс отступов для остальных элементов
        gbc.gridy = 8;
        add(buttonGenerateWithTextFields, gbc);
        buttonGenerateWithTextFields.addActionListener(e -> {
            viewModelTextFields.setEditMode(false); // Явный сброс режима
            Main.shouldSaveState = true; // Разрешаем сохранение
            Main.switchToPanel(Main.PANEL_TEXT_FIELDS);
            verification = true;
        });

        // Создаем и стилизуем кнопку для генерации с помощью таблицы
        buttonGenerateWithTable = new JButton("Использовать таблицу");
        ViewStyles.styleButton(buttonGenerateWithTable);
        buttonGenerateWithTable.setEnabled(false);
        buttonGenerateWithTable.setPreferredSize(COMPONENT_SIZE); // Устанавливаем размер
        gbc.gridy = 9;
        add(buttonGenerateWithTable, gbc);
        buttonGenerateWithTable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.switchToPanel(Main.PANEL_TABLE);
                Main.shouldSaveState = false; // Запрещаем сохранение
                verification = false;
            }
        });
        buttonEditPlaceholders = new JButton("Редактировать подсказки");
        ViewStyles.styleButton(buttonEditPlaceholders);
        buttonEditPlaceholders.setPreferredSize(COMPONENT_SIZE);
        gbc.gridy = 10;
        add(buttonEditPlaceholders, gbc);
        buttonEditPlaceholders.addActionListener(e -> {
            viewModelTextFields.setEditMode(true);
            Main.switchToPanel(Main.PANEL_TEXT_FIELDS);
        });
        resetDataButton = new JButton("Сбросить состояние системы");
        ViewStyles.styleButton(resetDataButton);
        resetDataButton.setPreferredSize(COMPONENT_SIZE);
        resetDataButton.addActionListener(e -> resetAppState());
        gbc.gridy = 11; // Позиция после последней кнопки
        add(resetDataButton, gbc);
    }
    private void resetAppState() {
        // Создаем кастомные кнопки
        JButton confirmButton = new JButton("Подтвердить");
        ViewStyles.styleButton(confirmButton);
        confirmButton.setPreferredSize(new Dimension(140, 40));

        JButton cancelButton = new JButton("Отмена");
        ViewStyles.styleButton(cancelButton);
        cancelButton.setPreferredSize(new Dimension(140, 40));

        // Стилизуем текст сообщения
        JLabel messageLabel = new JLabel("Вы уверены, что хотите сбросить состояние системы?");
        messageLabel.setFont(ViewStyles.DEFAULT_FONT);
        messageLabel.setForeground(ViewStyles.TEXT_COLOR_LABEL);

        // Создаем кастомный диалог
        JOptionPane pane = new JOptionPane(
                messageLabel,
                JOptionPane.WARNING_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new Object[]{confirmButton, cancelButton},
                null // Убираем дефолтный выбор
        );

        JDialog dialog = pane.createDialog(this, "Подтверждение сброса");
        dialog.getContentPane().setBackground(ViewStyles.BACKGROUND_COLOR);

        // Добавляем обработчики кликов
        confirmButton.addActionListener(e -> {
            pane.setValue(confirmButton);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> {
            pane.setValue(cancelButton);
            dialog.dispose();
        });

        dialog.setVisible(true);

        // Получаем результат выбора
        Object selectedValue = pane.getValue();
        int confirm = (selectedValue == confirmButton)
                ? JOptionPane.YES_OPTION
                : JOptionPane.NO_OPTION;

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Main.appState = new AppState();
                Main.tagMap.clear();
                Path path = Paths.get(System.getProperty("user.home"), "DocCraft", "appstate.json");
                Files.deleteIfExists(path);

                // Сброс UI элементов
                chosenDirectoryLabel.setText("Путь не выбран");
                authorComboBox.setSelectedIndex(0);
                convertToPdfCheckBox.setSelected(false);
                buttonGenerateWithTextFields.setEnabled(false);
                buttonGenerateWithTable.setEnabled(false);

                ViewStyles.showStyledMessage(
                        this,
                        "Все данные сброшены!",
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (IOException ex) {
                ViewStyles.showStyledMessage(
                        this,
                        "Ошибка при сбросе данных: " + ex.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}