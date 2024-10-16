package org.example.view;

import org.example.controller.DocumentGenerator;
import org.example.main.Main;
import org.example.model.TagDatabase;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

public class ViewModelTextFields extends JPanel {
    private Main main;
    private DocumentGenerator documentGenerator;
    private JButton generateButton;
    public JButton buttonBackSpace;
    public JButton chooseFileButton;
    private JButton showAllTagsButton;
    private JLabel chooseFileLabel;
    private ViewModelStartScreen viewModelStartScreen;
    private JPanel textFieldPanel;
    private JScrollPane scrollPane;
    private JScrollPane scrollPaneButton;
    private ViewModelTable viewModelTable;
    private JPanel buttonPanel;
    private HashMap<String, List<String>> fileTagMap;
    private Map<String, String> tagValuesMap; // Map to store tag values
    private TagDatabase tagDatabase; // Database instance

    ViewModelTextFields(Main main, ViewModelStartScreen viewModelStartScreen, DocumentGenerator documentGenerator, ViewModelTable viewModelTable) {
        this.main = main;
        this.viewModelStartScreen = viewModelStartScreen;
        this.documentGenerator = documentGenerator;
        this.viewModelTable = viewModelTable;
        this.tagDatabase = new TagDatabase("jdbc:sqlite:tags.db");
        ViewStyles.stylePanel(this);
        setLayout(null);
        setFocusable(true);
        tagValuesMap = new HashMap<>();
        initializeUI();
        // Добавляем слушатель для изменения размеров панели
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustScrollPaneSizes();
            }
        });
    }
    private void adjustScrollPaneSizes() {
        // Получаем родительский контейнер (окно)
        Window window = SwingUtilities.getWindowAncestor(this);

        if (window != null) {
            int windowWidth = window.getWidth();
            int windowHeight = window.getHeight();

            int marginTop =windowHeight/6;   // Верхний отступ от окна
            int marginBottom = windowHeight/6;  // Нижний отступ от окна
            int marginLeft = windowWidth/18;  // Левый отступ от окна

            // Установим размеры для scrollPane с учетом отступов
            int scrollPaneWidth = (windowWidth / 2) - marginLeft;
            int scrollPaneHeight = windowHeight - marginTop - marginBottom;
            scrollPane.setBounds(marginLeft, marginTop, scrollPaneWidth, scrollPaneHeight);

            // Установим размеры для scrollPaneButton с учетом отступов
            int scrollPaneButtonX = windowWidth / 2 + marginLeft / 2;  // Размещаем с отступом справа
            scrollPaneButton.setBounds(scrollPaneButtonX, marginTop, 300, scrollPaneHeight);
            generateButton.setBounds(scrollPaneButtonX-scrollPaneButtonX/4,scrollPaneHeight+scrollPaneHeight/3-20 , 220, 50);
            chooseFileButton.setBounds(scrollPaneButtonX-scrollPaneButtonX/4, scrollPaneHeight/15, 220, 50);
            chooseFileLabel.setBounds(scrollPaneButtonX-scrollPaneButtonX/4,scrollPaneHeight/6,400,30);

            // Обновляем компоненты
            revalidate();
            repaint();
        }
    }

    private boolean areAllTextFieldsFilled() {
        for (JTextField textField : findTextFields()) {
            String placeholder = (String) textField.getClientProperty("placeholder"); // Получаем плейсхолдер
            String text = textField.getText().trim();

            // Проверяем, заполнено ли текстовое поле
            if (text.isEmpty() || text.equals(placeholder)) {
                return false; // Если текстовое поле пустое или равно плейсхолдеру, возвращаем false
            }
        }
        return true; // Все текстовые поля заполнены корректно
    }

    private void updateGenerateButtonState() {
        generateButton.setEnabled(areAllTextFieldsFilled()); // Активируем или деактивируем кнопку
        generateButton.setFocusable(areAllTextFieldsFilled());
    }

    private void initializeUI() {
        buttonBackSpace = new JButton();
        buttonBackSpace.setText("⬅");
        Font font = buttonBackSpace.getFont();
        ViewStyles.styleButton(buttonBackSpace);
        buttonBackSpace.setFont(font.deriveFont(Font.PLAIN, 32));
        buttonBackSpace.setBounds(0, 0, 70, 50);
        buttonBackSpace.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                main.switchToPanel(viewModelStartScreen);

            }
        });
        add(buttonBackSpace);
        chooseFileLabel = new JLabel("Файлы не выбраны");
        add(chooseFileLabel);


        chooseFileButton = new JButton("Выбор файлов (doc/docx)");
        chooseFileButton.setBounds(300, 40, 200, 50);
        ViewStyles.styleButton(chooseFileButton);
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeTextFields();
                tagValuesMap.clear();
                FileDialog fileDialog = new FileDialog((Frame) null, "Выберите файл", FileDialog.LOAD);
                fileDialog.setFile("*.doc;*.docx");
                fileDialog.setMultipleMode(true);
                fileDialog.setVisible(true);
                documentGenerator.selectedFiles = fileDialog.getFiles();

                if (documentGenerator.selectedFiles != null && documentGenerator.selectedFiles.length > 0) {
                    chooseFileLabel.setText("Выбранные файлы: ");
                    viewModelStartScreen.select = new String[documentGenerator.selectedFiles.length];
                    for (int i = 0; i < documentGenerator.selectedFiles.length; i++) {
                        viewModelStartScreen.select[i] = documentGenerator.selectedFiles[i].getName();
                    }

                    // Обновить поле со списком в ViewModelTable
                    if (viewModelTable != null) {
                        viewModelTable.clearComboBox();
                        viewModelTable.updateComboBox(viewModelStartScreen.select);
                        viewModelTable.getFileLabel().setText("Выбранные файлы: ");
                    }
                }
                documentGenerator.createFolder();
                if (viewModelStartScreen.verification) {
                    fileTagMap = documentGenerator.tagExtractor.writeTagsToMap(documentGenerator.selectedFiles);
                    generateFileButtons(fileTagMap);
                    generateTextFields(getAllTags(fileTagMap));
                } else {
                    documentGenerator.tagExtractor.writeTagsToCSV(documentGenerator.selectedFiles, documentGenerator.outputFolderPath);
                }
                viewModelTable.generateButtonUsingTable.setEnabled(true);
            }
        });

        add(chooseFileButton);

        generateButton = new JButton("Генерация документов");
        ViewStyles.styleButton(generateButton);
        generateButton.setEnabled(false);
        generateButton.setFocusable(false); // Убираем фокус с кнопки
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showAllTagsButton != null) {
                    showAllTagsButton.doClick();  // Имитируем нажатие на кнопку "Показать все теги"
                }

                // Генерируем документ
                documentGenerator.generateDocument();

                // Очищаем текстовые поля
                clearTextFields();

                tagValuesMap.clear();
            }
        });
        add(generateButton);

        textFieldPanel = new JPanel();
        textFieldPanel.setLayout(null);
        ViewStyles.stylePanel(textFieldPanel);

        scrollPane = new JScrollPane(textFieldPanel);
        scrollPane.setBounds(70, 125, 300, 360);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        ViewStyles.styleScrollBar(scrollPane.getVerticalScrollBar());
        add(scrollPane);

        buttonPanel = new JPanel();

        ViewStyles.stylePanel(buttonPanel);
        add(buttonPanel);

        scrollPaneButton = new JScrollPane(buttonPanel);
        scrollPaneButton.setBounds(450,125,300,360);
        scrollPaneButton.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        ViewStyles.styleScrollBar(scrollPaneButton.getVerticalScrollBar());
        add(scrollPaneButton);


    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    // метод для добавления подсказок в текстовые поля
    private void addPlaceholder(JTextField textField, String tag) {
        String placeholder = tagDatabase.getPlaceholder(tag);
        if (placeholder == null) {
            placeholder = tag;
            tagDatabase.saveTag(tag);
        }

        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);
        textField.putClientProperty("placeholder", placeholder); // Сохраняем плейсхолдер в свойство текстового поля

        String finalPlaceholder = placeholder;
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(finalPlaceholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(finalPlaceholder);
                } else {
                    tagValuesMap.put(tag, textField.getText());
                }
            }
        });
    }




    // Способ динамической генерации текстовых полей тегов с заполнителями
    private void generateTextFields(List<String> tags) {
        textFieldPanel.removeAll();
        System.out.println(tagValuesMap);
        int padding = 10; // Отступы по бокам
        int topPadding = 10; // Отступ сверху для первого элемента
        textFieldPanel.setPreferredSize(new Dimension(textFieldPanel.getWidth(), tags.size() * 40 + topPadding));
        JTextField[] textFields = new JTextField[tags.size()];

        for (int i = 0; i < tags.size(); i++) {
            textFields[i] = new JTextField();
            textFields[i].setBounds(padding, topPadding + i * 40, textFieldPanel.getWidth() - 2 * padding, 30); // Учет отступов
            String tag = tags.get(i);
            addPlaceholder(textFields[i], tag); // Устанавливаем текст-заполнитель из обработанного тега
            ViewStyles.styleTextField(textFields[i]);

            // Устанавливаем текст, если он уже есть в tagValuesMap
            if (tagValuesMap.containsKey(tag)) {
                textFields[i].setText(tagValuesMap.get(tag));
                textFields[i].setForeground(Color.BLACK);
            }

            // Добавляем DocumentListener для обновления состояния кнопки генерации
            textFields[i].getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateGenerateButtonState(); // Проверка при вставке текста
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateGenerateButtonState(); // Проверка при удалении текста
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateGenerateButtonState(); // Проверка при изменении текста
                }
            });

            textFieldPanel.add(textFields[i]);
        }
        updateGenerateButtonState();
        textFieldPanel.revalidate();
        textFieldPanel.repaint();
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    public List<JTextField> findTextFields() {
        List<JTextField> textFields = new ArrayList<>();
        Component[] components = textFieldPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JTextField) {
                textFields.add((JTextField) component);
            }
        }
        return textFields;
    }

    private void removeTextFields() {
        textFieldPanel.removeAll();
        textFieldPanel.revalidate();
        textFieldPanel.repaint();
        scrollPane.revalidate();
        scrollPane.repaint();
    }



    private void generateFileButtons(HashMap<String, List<String>> fileTagMap) {
        buttonPanel.removeAll();
        chooseFileLabel.setText("Выбранные файлы: Показать все теги");

        // Кнопка "Показать все теги"
        showAllTagsButton = new JButton("Показать все теги");
        ViewStyles.styleButton(showAllTagsButton);
        showAllTagsButton.setPreferredSize(new Dimension(250, 30));
        showAllTagsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateTextFields(getAllTags(fileTagMap));
                chooseFileLabel.setText("Выбранный файл: " + showAllTagsButton.getText());
            }
        });
        if (documentGenerator.selectedFiles.length != 0)
            buttonPanel.add(showAllTagsButton);

        // Кнопки для конкретных файлов
        int yOffset = 40; // Начальная Y позиция для кнопок файлов
        for (String fileName : fileTagMap.keySet()) {
            JButton fileButton = new JButton(fileName);
            fileButton.setPreferredSize(new Dimension(250, 35)); // Фиксированный размер
            ViewStyles.styleButton(fileButton);
            fileButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    chooseFileLabel.setText("Выбранный файл: " + fileName); // Установка имени файла
                    List<String> tags = fileTagMap.get(fileName);
                    generateTextFields(tags); // Генерация текстовых полей для выбранного файла
                }
            });
            buttonPanel.add(fileButton);
            yOffset += 40; // Увеличиваем Y позицию для следующей кнопки
        }

        // Устанавливаем предпочитаемый размер buttonPanel
        buttonPanel.setPreferredSize(new Dimension(200, yOffset));

        // Обновляем buttonPanel
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }



    private void closeDatabase() {
        tagDatabase.close(); // Method to close the database connection
    }

    private void clearTextFields() {
        for (JTextField textField : findTextFields()) {
            textField.setText(""); // Очищаем текстовое поле
            textField.setForeground(Color.GRAY); // Возвращаем цвет плейсхолдера
            textField.setText((String) textField.getClientProperty("placeholder")); // Устанавливаем плейсхолдер
        }
    }



    private List<String> getAllTags(HashMap<String, List<String>> fileTagMap) {
        Set<String> allTags = new HashSet<>();
        for (List<String> tags : fileTagMap.values()) {
            allTags.addAll(tags);
        }
        return new ArrayList<>(allTags);
    }
}