package org.example.view;

import org.example.controller.DocumentGenerator;
import org.example.controller.FileManager;
import org.example.main.Main;
import org.example.model.TagExtractor;
import org.example.model.TagMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.main.Main.*;

public class ViewModelTable extends JPanel {
    public JButton generateButtonUsingTable;
    private JButton buttonBackSpace;
    public JButton createCSVButton;
    public JButton selectCSVButton;
    private JComboBox<String> selectFilesForTableComboBox;
    private JLabel fileLabel;
    // Константы для одинакового размера компонентов
    private static final Dimension COMPONENT_SIZE = new Dimension((int) (300*1.4), (int) (50*1.4));
    // Добавляем contentPanel на уровне класса
    private JPanel contentPanel;
    private JButton chooseFileButton;
    private File[] selectedFiles;
    private String csvFilePath;

    ViewModelTable() {
        ViewStyles.stylePanel(this);
        setLayout(new BorderLayout()); // Используем BorderLayout для основного компонента
        setFocusable(true);
        initializeTable();  // Убедимся, что initializeTable вызывается в конструкторе
    }

    private void initializeTable() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Выравнивание кнопки слева
        buttonPanel.setOpaque(false);
        buttonBackSpace = new JButton();
        buttonBackSpace.setText("⬅");
        Font font = buttonBackSpace.getFont();
        ViewStyles.styleButton(buttonBackSpace);
        buttonBackSpace.setFont(font.deriveFont(Font.PLAIN, 32));
        buttonBackSpace.setPreferredSize(new Dimension(100, 70));  // Устанавливаем фиксированные размеры
        buttonBackSpace.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.switchToPanel(PANEL_START_SCREEN);
            }
        });
        buttonPanel.add(buttonBackSpace);
        add(buttonPanel, BorderLayout.PAGE_START);

        // Инициализация contentPanel
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        chooseFileButton = new JButton("Выбор файлов (doc/docx)");
        chooseFileButton.setPreferredSize(COMPONENT_SIZE);
        ViewStyles.styleButton(chooseFileButton);
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog fileDialog = new FileDialog((Frame) null, "Выберите файл", FileDialog.LOAD);
                fileDialog.setFile("*.doc;*.docx");
                fileDialog.setMultipleMode(true);
                fileDialog.setVisible(true);
                selectedFiles = fileDialog.getFiles();
//                documentGenerator.setSelectedFiles(fileDialog.getFiles());
//                selectedFiles = documentGenerator.getSelectedFiles();

                if (selectedFiles != null && selectedFiles.length > 0) {
                    viewModelStartScreen.select = new String[selectedFiles.length];
                    for (int i = 0; i < selectedFiles.length; i++) {
                        viewModelStartScreen.select[i] = selectedFiles[i].getName();
                    }
                    clearComboBox();
                    updateComboBox(viewModelStartScreen.select);
                    getFileLabel().setText("Выбранные файлы: ");
                    createCSVButton.setEnabled(true);
                    selectCSVButton.setEnabled(true);
                }
            }
        });
        gbc.gridy = 0;
        contentPanel.add(chooseFileButton, gbc);

        // Метка для отображения выбранного файла
        fileLabel = new JLabel("Файл(ы) не выбран(ы)!");
        fileLabel.setPreferredSize(new Dimension(400, 30));
        ViewStyles.styleLabel(fileLabel);
        gbc.gridy = 4;
        contentPanel.add(fileLabel, gbc);

        // Кнопка генерации
        generateButtonUsingTable = new JButton("Генерация с помощью таблицы");
        generateButtonUsingTable.setEnabled(false);
        generateButtonUsingTable.setPreferredSize(COMPONENT_SIZE);
        ViewStyles.styleButton(generateButtonUsingTable);
        generateButtonUsingTable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fillTagsAndCallGeneration();
            }
        });
        gbc.gridy = 3;
        contentPanel.add(generateButtonUsingTable, gbc);
        createCSVButton = new JButton("Генерация с помощью новой таблицы");
        createCSVButton.setPreferredSize(COMPONENT_SIZE);
        createCSVButton.setEnabled(false);
        ViewStyles.styleButton(createCSVButton);
        createCSVButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedFiles = fileManager.preprocessBlockFiles(selectedFiles);
                generateButtonUsingTable.setEnabled(true);
                selectOrCreateCSV(true);
            }
        });
        gbc.gridy = 1;
        contentPanel.add(createCSVButton, gbc);
        selectCSVButton = new JButton("Генерация с помощью существующей таблицы");
        selectCSVButton.setPreferredSize(COMPONENT_SIZE);
        selectCSVButton.setEnabled(false);
        ViewStyles.styleButton(selectCSVButton);
        selectCSVButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedFiles = fileManager.preprocessBlockFiles(selectedFiles);
                selectOrCreateCSV(false);
            }
        });
        gbc.gridy = 2;
        contentPanel.add(selectCSVButton, gbc);


        add(contentPanel, BorderLayout.CENTER); // Добавляем панель с остальными компонентами в центр
    }

    void updateComboBox(String[] select) {
        if (selectFilesForTableComboBox != null) {
            contentPanel.remove(selectFilesForTableComboBox);  // Удаляем старый JComboBox из contentPanel
        }

        selectFilesForTableComboBox = new JComboBox<>(select);
        selectFilesForTableComboBox.setPreferredSize(new Dimension(COMPONENT_SIZE.width, COMPONENT_SIZE.height));
        selectFilesForTableComboBox.setMaximumRowCount(15);
        ViewStyles.styleComboBox(selectFilesForTableComboBox);

        // Добавляем ComboBox в contentPanel с корректными GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridy = 5;  // Убедитесь, что позиция соответствует другим элементам
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(selectFilesForTableComboBox, gbc);

        // Обновляем панель
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    void clearComboBox() {
        if (selectFilesForTableComboBox != null) {
            contentPanel.remove(selectFilesForTableComboBox);  // Удаляем JComboBox из contentPanel
            selectFilesForTableComboBox = null;
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    JLabel getFileLabel() {
        return fileLabel;
    }

    // Создание или выбор csv таблицы
    private void selectOrCreateCSV(boolean createNew) {
        // Вызываем метод создания основной папки
        fileManager.createFolder();
        csvFilePath = null;
        String outputFolderPath = fileManager.getOutputFolderPath();
        if (createNew) {
            csvFilePath = outputFolderPath + File.separator + "tags.csv";
            // Создание новой таблицы
            tagExtractor.writeTagsToCSV(selectedFiles, csvFilePath);
            // Открытие папки с таблицей для последующего редактирования
            fileManager.openFileOrFolder(csvFilePath);
        } else {
            // Выбор существующей таблицы
            FileDialog fileDialog = new FileDialog((Frame) null, "Выберите существующий файл tags.csv", FileDialog.LOAD);
            fileDialog.setFile("*.csv");
            fileDialog.setVisible(true);

            String selectedFile = fileDialog.getFile();
            String directory = fileDialog.getDirectory();

            if (selectedFile != null && directory != null) {
                File csvFile = new File(directory, selectedFile);
                if (csvFile.exists()) {
                    // Проверка тегов
                    List<String> missingTags = tagExtractor.verifyTagsInCSV(selectedFiles, csvFile);

                    if (!missingTags.isEmpty()) {
                        // Формируем строку с каждым тегом на новой строке
                        String errorMessage = "Отсутствующие теги:\n" + String.join("\n", missingTags);
                        JTextArea textArea = new JTextArea(errorMessage);
                        textArea.setEditable(false); // Запрет редактирования
                        textArea.setLineWrap(false); // Отключаем перенос строк
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        scrollPane.setPreferredSize(new Dimension(400, 300)); // Размер окна с прокруткой
                        generateButtonUsingTable.setEnabled(false);
                        // Выводим сообщение с прокруткой
                        JOptionPane.showMessageDialog(null, scrollPane, "Ошибка: вы выбрали таблицу с недостающими тегами!", JOptionPane.ERROR_MESSAGE);
                        return; // Прерываем выполнение, если теги отсутствуют
                    }
                    csvFilePath = csvFile.getAbsolutePath();
                    if (csvFilePath != null){
                       generateButtonUsingTable.setEnabled(true);
                    }
                }
            }
        }
    }

    // Генерация документов для одного или нескольких пакетов
    private void fillTagsAndCallGeneration() {
        List<TagMap> tagMaps = tagExtractor.readTableFile(csvFilePath);
        if (!checkTagValues(tagMaps)) {
            return;
        }
        String outputFolderPath = fileManager.getOutputFolderPath();
        for (int i = 0; i < tagMaps.size(); i++){
            fileManager.setOutputFolderPath(outputFolderPath + File.separator + "Пакет " + (i + 1));
            documentGenerator.generateDocument(tagMaps.get(i), selectedFiles);
        }
        fileManager.setOutputFolderPath(outputFolderPath);
        fileManager.openFileOrFolder(outputFolderPath);
    }

    // Проверяем значения для специфичных тегов
    public boolean checkTagValues(List<TagMap> tagMaps) {
        List<String> invalidTags = new ArrayList<>();

        for (TagMap tagMap : tagMaps) {
            // Считаем значения для специфичных тегов
            int countOnes = 0;
            int countZeros = 0;
            Map<String, String> specificTags = new HashMap<>();

            for (Map.Entry<String, String> entry : tagMap.entrySet()) {
                String tag = entry.getKey();
                String value = entry.getValue();

                // Проверяем значение тегов
                if ((tag.equals("${key_ria_type_x_pr}") || tag.equals("${key_ria_type_x_bd59}") || tag.equals("${key_ria_type_x_bd34}"))) {
                    specificTags.put(tag, value);

                    if (!value.equals("0") && !value.equals("1")) {
                        invalidTags.add(tag + ": " + value);
                    } else {
                        if (value.equals("1")) {
                            countOnes++;
                        } else if (value.equals("0")) {
                            countZeros++;
                        }
                    }
                }
            }

            // Проверяем взаимосвязь значений тегов
            if (!specificTags.isEmpty() && countOnes != 1) {
                invalidTags.add("У тегов ${key_ria_type_x_pr}, ${key_ria_type_x_bd59}, ${key_ria_type_x_bd34} должно быть одно значение 1 и два значения 0.");
            }
        }

        // Если есть ошибки, выводим их все
        if (!invalidTags.isEmpty()) {
            String errorMessage = "Ошибка!\nНайдены ошибочные значения в тегах:\n" +
                    String.join("\n", invalidTags) + "\nПожалуйста, проверьте правильность ввода.";
            JOptionPane.showMessageDialog(null, errorMessage, "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

}

