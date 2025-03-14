package org.example.view;

import org.example.controller.DocumentGenerator;
import org.example.controller.FileManager;
import org.example.main.Main;
import org.example.model.TagDatabase;
import org.example.model.TagExtractor;
import org.example.model.TagMap;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ViewModelTextFields extends JPanel {
    private Main main;
    private DocumentGenerator documentGenerator;
    private TagExtractor tagExtractor;
    private JButton generateButton;
    public JButton buttonBackSpace;
    public JButton chooseFileButton;
    private JButton showAllTagsButton;
    private JButton clearButton;
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
    private File[] selectedFiles;
    private TagMap tagMap;
    private final Map<String, JTextField> specificFields = new HashMap<>();
    private boolean isEditMode = false;
    private FileManager fileManager;

    ViewModelTextFields(Main main, ViewModelStartScreen viewModelStartScreen, DocumentGenerator documentGenerator, ViewModelTable viewModelTable, FileManager fileManager, TagExtractor tagExtractor) {
        this.main = main;
        this.viewModelStartScreen = viewModelStartScreen;
        this.documentGenerator = documentGenerator;
        this.viewModelTable = viewModelTable;
        this.tagDatabase = new TagDatabase();
        this.fileManager = fileManager;
        this.tagExtractor = tagExtractor;
        ViewStyles.stylePanel(this);
        setLayout(null);
        setFocusable(true);
        tagValuesMap = new HashMap<>();
        initializeUI();
        setupMode();
        clearAll();
        // Добавляем слушатель для изменения размеров панели
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustScrollPaneSizes();
            }
        });
    }

    private void setupMode() {
        System.out.println(isEditMode);
        if (isEditMode) {
            enterEditMode();
        } else {
            exitEditMode();
        }
    }

    private void enterEditMode() {
        generateButton.setVisible(false);
        clearButton.setText("Сохранить в БД");

        // Удаляем все старые обработчики кнопки
        for (ActionListener al : chooseFileButton.getActionListeners()) {
            chooseFileButton.removeActionListener(al);
        }

        // Добавляем новый обработчик
        chooseFileButton.addActionListener(e -> handleFileSelectionInEditMode());

        // Очищаем панель с кнопками файлов
        removeFileButtons();
        chooseFileLabel.setText("Файлы не выбраны");

        // Загружаем теги: если есть выбранные файлы, их теги, иначе все из БД
        if (fileTagMap != null && !fileTagMap.isEmpty()) {
            generateTextFields(getAllTags(fileTagMap));
        } else {
            loadAllTagsFromDatabase();
        }
    }

    private void handleFileSelectionInEditMode() {
        FileDialog fileDialog = new FileDialog((Frame) null, "Выберите файл", FileDialog.LOAD);
        fileDialog.setFile("*.doc;*.docx");
        fileDialog.setMultipleMode(true);
        fileDialog.setVisible(true);
        File[] newSelectedFiles = fileDialog.getFiles();

        if (newSelectedFiles != null && newSelectedFiles.length > 0) {
            selectedFiles = fileManager.preprocessBlockFiles(newSelectedFiles);
            fileTagMap = tagExtractor.writeTagsToMap(selectedFiles);

            // Добавляем новые теги в БД
            Set<String> newTags = new HashSet<>(getAllTags(fileTagMap));
            for (String tag : newTags) {
                if (!tagDatabase.getAllTags().contains(tag)) {
                    tagDatabase.saveTag(tag);
                }
            }
            adjustScrollPaneSizes();
            generateFileButtons(fileTagMap);
            generateTextFields(getAllTags(fileTagMap)); // Заменяем loadAllTagsFromDatabase()
        }
    }

    private void exitEditMode() {
        generateButton.setVisible(true);
        clearButton.setText("Очистить ввод");

        // Удаляем все старые обработчики кнопки
        for (ActionListener al : chooseFileButton.getActionListeners()) {
            chooseFileButton.removeActionListener(al);
        }

        chooseFileButton.addActionListener(e -> handleNormalModelFileSelection());
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        setupMode();
        revalidate();
        repaint();
    }

    private void adjustScrollPaneSizes() {
        // Получаем родительский контейнер (окно)
        Window window = SwingUtilities.getWindowAncestor(this);

        if (window != null) {
            int windowWidth = window.getWidth();
            int windowHeight = window.getHeight();

            int marginTop = windowHeight / 6;   // Верхний отступ от окна
            int marginBottom = windowHeight / 6;  // Нижний отступ от окна
            int marginLeft = windowWidth / 18;  // Левый отступ от окна

            // Установим размеры для scrollPane с учетом отступов
            int scrollPaneWidth = (windowWidth / 2) - marginLeft;
            int scrollPaneHeight = windowHeight - marginTop - marginBottom;
            scrollPane.setBounds(marginLeft, marginTop, scrollPaneWidth, scrollPaneHeight);

            // Установим размеры для scrollPaneButton с учетом отступов
            int scrollPaneButtonX = ((windowWidth / 2 + 50) + marginLeft / 2);
            scrollPaneButton.setBounds(scrollPaneButtonX, marginTop, scrollPaneWidth - scrollPaneWidth / 7, scrollPaneHeight);

            // Изменение размеров кнопок и текстовых полей относительно размеров панели
            int buttonWidth = (int) ((scrollPaneButton.getWidth() / 3) * 1.6);
            int buttonHeight = scrollPaneButton.getHeight() / 10;

            // Изменяем размеры и положение кнопок
            int buttonY = scrollPaneHeight / 15;
            int buttonSpacing = 20; // Фиксированное расстояние между кнопками

            chooseFileButton.setBounds(scrollPaneButtonX - scrollPaneButtonX / 4, buttonY, buttonWidth, buttonHeight);
            clearButton.setBounds(scrollPaneButtonX - scrollPaneButtonX / 4 + buttonWidth + buttonSpacing, buttonY, buttonWidth, buttonHeight);

            chooseFileLabel.setBounds(scrollPaneButtonX - scrollPaneButtonX / 4, scrollPaneHeight / 6, buttonWidth, buttonHeight);
            generateButton.setBounds(scrollPaneButtonX - scrollPaneButtonX / 4, scrollPaneHeight + scrollPaneHeight / 3 - 20, buttonWidth, buttonHeight);
            adjustTextFieldSizes();
            revalidate();
            repaint();
        }
    }

    private void adjustTextFieldSizes() {
        // Получаем текущие размеры scrollPane
        int scrollPaneWidth = scrollPane.getWidth();

        // Проверяем, есть ли текстовые поля на панели
        if (textFieldPanel != null && textFieldPanel.getComponentCount() > 0) {
            // Рассчитываем размеры текстовых полей
            int textFieldWidth = (int) (scrollPaneWidth * 0.92); // Ширина текстового поля - 90% ширины scrollPane
            // Максимальная и минимальная высота текстового поля
            int textFieldHeight = 30;

            int yOffset = 10; // Отступ между текстовыми полями
            int yPosition = yOffset;

            // Обновляем размеры и положение каждого текстового поля
            for (Component component : textFieldPanel.getComponents()) {
                if (component instanceof JTextField) {
                    component.setBounds(10, yPosition, textFieldWidth, textFieldHeight);
                    yPosition += textFieldHeight + yOffset; // Смещаем позицию для следующего текстового поля
                }
            }

            // Обновляем размер textFieldPanel, чтобы все текстовые поля уместились
            textFieldPanel.setPreferredSize(new Dimension(scrollPaneWidth, yPosition + yOffset));
            textFieldPanel.revalidate();
            textFieldPanel.repaint();
        }
    }


    private boolean areAllTextFieldsFilled() {
        for (JTextField textField : findTextFields()) {
            String text = textField.getText().trim();
            if (isEditMode) {
                String tag = (String) textField.getClientProperty("originalTag");
                boolean isPlaceholder = text.equals("Введите подсказку для " + tag) && textField.getForeground() == Color.GRAY;
                if (text.isEmpty() || isPlaceholder) {
                    return false;
                }
            } else {
                String placeholder = (String) textField.getClientProperty("placeholder");
                String tag = (String) textField.getClientProperty("tag");
                if (placeholder == null || tag == null) {
                    if (text.isEmpty()) {
                        return false;
                    }
                } else {
                    String displayedPlaceholder = placeholder + " (" + tag + ")";
                    if (text.isEmpty() || text.equals(displayedPlaceholder)) {
                        return false;
                    }
                }
            }
        }
        return true;
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
        buttonBackSpace.setBounds(5, 5, 100, 70);  // Устанавливаем фиксированные размеры
        buttonBackSpace.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isEditMode) {
                    clearAll();
                }
                main.switchToPanel(viewModelStartScreen);
            }
        });
        add(buttonBackSpace);

        chooseFileLabel = new JLabel("Файлы не выбраны");
        chooseFileLabel.setBounds(300, 40, 400, 30); // Устанавливаем фиксированные размеры
        ViewStyles.styleLabel(chooseFileLabel);
        add(chooseFileLabel);

        chooseFileButton = new JButton("Выбор файлов (doc/docx)");
        chooseFileButton.setBounds(300, 80, 200, 50); // Устанавливаем фиксированные размеры
        ViewStyles.styleButton(chooseFileButton);
        add(chooseFileButton);
        clearButton = new JButton("Очистить ввод");
        ViewStyles.styleButton(clearButton);
        clearButton.setBounds(500, 80, 150, 50); // Увеличиваем ширину
        clearButton.addActionListener(e -> {
            if (isEditMode) {
                savePlaceholdersToDatabase();
            } else {
                clearTextFields();
                tagValuesMap.clear();
                generateButton.setEnabled(false);
            }
        });
        add(clearButton);

        generateButton = new JButton("Генерация документов");
        ViewStyles.styleButton(generateButton);
        generateButton.setEnabled(false);
        generateButton.setFocusable(false); // Убираем фокус с кнопки
        generateButton.setBounds(300, 150, 200, 50); // Устанавливаем фиксированные размеры
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showAllTagsButton != null) {
                    showAllTagsButton.doClick();  // Имитируем нажатие на кнопку "Показать все теги"
                }
                // Генерируем документ
                fillTagsAndCallGeneration();

                // Очищаем текстовые поля
//                clearTextFields();
//                tagValuesMap.clear();
            }
        });
        add(generateButton);

        textFieldPanel = new JPanel();
        textFieldPanel.setLayout(null);
        ViewStyles.stylePanel(textFieldPanel);

        scrollPane = new JScrollPane(textFieldPanel);
        scrollPane.setBounds(70, 125, 300, 360); // Устанавливаем фиксированные размеры
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        ViewStyles.styleScrollBar(scrollPane.getVerticalScrollBar());
        add(scrollPane);

        buttonPanel = new JPanel();
        ViewStyles.stylePanel(buttonPanel);
        add(buttonPanel);

        scrollPaneButton = new JScrollPane(buttonPanel);
        scrollPaneButton.setBounds(450, 125, 400, 360); // Устанавливаем фиксированные размеры
        scrollPaneButton.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPaneButton.getVerticalScrollBar().setUnitIncrement(16);
        ViewStyles.styleScrollBar(scrollPaneButton.getVerticalScrollBar());
        add(scrollPaneButton);
    }

    private void handleNormalModelFileSelection() {
        FileDialog fileDialog = new FileDialog((Frame) null, "Выберите файл", FileDialog.LOAD);
        fileDialog.setFile("*.doc;*.docx");
        fileDialog.setMultipleMode(true);
        fileDialog.setVisible(true);
        File[] newSelectedFiles = fileDialog.getFiles();

        // Если выбор не произведен (массив пуст или null), сохраняем предыдущие выбранные файлы
        if (newSelectedFiles == null || newSelectedFiles.length == 0) {
            JOptionPane optionPane = new JOptionPane(
                    "Выбор файлов не изменён.",
                    JOptionPane.INFORMATION_MESSAGE,
                    JOptionPane.DEFAULT_OPTION
            );

            // Стилизуем окно с сообщением
            ViewStyles.styleOptionPane(optionPane);

            // Создаем и стилизуем диалог
            JDialog dialog = optionPane.createDialog(null, "Информация");
            dialog.setVisible(true);

            return;
        }

        // Если выбор был произведён, обновляем массив и остальные элементы
        selectedFiles = newSelectedFiles;
        selectedFiles = fileManager.preprocessBlockFiles(selectedFiles);
        tagValuesMap.clear();

        chooseFileLabel.setText("Выбранные файлы: ");
        viewModelStartScreen.select = new String[selectedFiles.length];
        for (int i = 0; i < selectedFiles.length; i++) {
            viewModelStartScreen.select[i] = selectedFiles[i].getName();
        }
        fileTagMap = tagExtractor.writeTagsToMap(selectedFiles);
        fileTagMap = filterTagsByAuthorCount(fileTagMap, viewModelStartScreen.selectedNumber);
        generateFileButtons(fileTagMap);
        generateTextFields(getAllTags(fileTagMap));
    }

    private HashMap<String, List<String>> filterTagsByAuthorCount(
            HashMap<String, List<String>> fileTagMap,
            int authorCount
    ) {
        HashMap<String, List<String>> filteredMap = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : fileTagMap.entrySet()) {
            List<String> filteredTags = new ArrayList<>();
            for (String tag : entry.getValue()) {
                if (isTagRelevantForAuthorCount(tag, authorCount)) {
                    filteredTags.add(tag);
                }
            }
            filteredMap.put(entry.getKey(), filteredTags);
        }
        return filteredMap;
    }

    private boolean isTagRelevantForAuthorCount(String tag, int authorCount) {
        // Пример фильтрации тегов вида ${authorX_email}
        Pattern pattern = Pattern.compile("\\$\\{author(\\d+)_");
        Matcher matcher = pattern.matcher(tag);

        if (matcher.find()) {
            int tagAuthorNumber = Integer.parseInt(matcher.group(1));
            return tagAuthorNumber <= authorCount;
        }
        return true; // Оставляем теги, не связанные с авторами
    }

    private void loadAllTagsFromDatabase() {
        List<String> allTags = tagDatabase.getAllTags();
        generateTextFields(allTags);
    }

    private void savePlaceholdersToDatabase() {
        for (JTextField textField : findTextFields()) {
            String tag = (String) textField.getClientProperty("originalTag");
            if (tag == null) continue;

            String placeholder = textField.getText().trim();
            // Проверяем, является ли текст подсказкой
            if (textField.getForeground() == Color.GRAY && placeholder.equals("Введите подсказку для " + tag)) {
                placeholder = ""; // Сохраняем пустую строку
            }
            tagDatabase.saveTag(tag, placeholder);
        }
        JOptionPane.showMessageDialog(this, "Все подсказки сохранены в БД");
    }

    private boolean isTagRelevant(String tag, int selectedAuthors) {
        // Логика определения релевантности тега для выбранного количества авторов
        if (tag.contains("author")) {
            Pattern p = Pattern.compile("author(\\d+)");
            Matcher m = p.matcher(tag);
            if (m.find()) {
                int authorNum = Integer.parseInt(m.group(1));
                return authorNum <= selectedAuthors;
            }
        }
        return true;
    }

    private String extractTagFromTextField(JTextField textField) {
        String placeholder = (String) textField.getClientProperty("placeholder");
        if (placeholder != null) {
            return placeholder.substring(placeholder.indexOf("(") + 1, placeholder.indexOf(")"));
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }


    // Способ динамической генерации текстовых полей тегов с заполнителями
    private void generateTextFields(List<String> tags) {
        textFieldPanel.removeAll();
        tags = tags.stream()
                .sorted()
                .filter(tag -> isTagRelevant(tag, viewModelStartScreen.selectedNumber))
                .collect(Collectors.toList());

        System.out.println("Generating text fields for " + tags.size() + " tags");

        List<JTextField> textFields = new ArrayList<>(); // Список для хранения всех полей
        int padding = 10;
        int topPadding = 10;
        int fieldHeight = 30;

        textFieldPanel.setPreferredSize(new Dimension(
                textFieldPanel.getWidth(),
                tags.size() * (fieldHeight + topPadding) + topPadding
        ));

        for (int i = 0; i < tags.size(); i++) {
            String tag = tags.get(i);
            JTextField textField = new JTextField();
            if (SPECIAL_TAGS.contains(tag)) {
                specificFields.put(tag, textField);
            }
            textFields.add(textField); // Добавляем поле в список

            final int currentIndex = i; // Фиксируем индекс для лямбда-выражения

            // Настройка позиционирования и стиля
            int yPos = topPadding + i * (fieldHeight + topPadding);
            textField.setBounds(padding, yPos, textFieldPanel.getWidth() - 2 * padding, fieldHeight);

            // Настройка режима
            if (isEditMode) {
                setupTextFieldForEditMode(textField, tag);
            } else {
                setupTextFieldForNormalMode(textField, tag);
            }

            ViewStyles.styleTextField(textField);
            textFieldPanel.add(textField);
            adjustTextFieldSizes();
            // Добавляем слушатель клавиш
            textField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        if (currentIndex > 0) {
                            JTextField prevField = textFields.get(currentIndex - 1);
                            prevField.requestFocusInWindow();
                            fieldVisibility(prevField);
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        if (currentIndex < textFields.size() - 1) {
                            JTextField nextField = textFields.get(currentIndex + 1);
                            nextField.requestFocusInWindow();
                            fieldVisibility(nextField);
                        }
                    }
                }
            });

            // Существующий DocumentListener
            textField.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    updateGenerateButtonState();
                }

                public void removeUpdate(DocumentEvent e) {
                    updateGenerateButtonState();
                }

                public void insertUpdate(DocumentEvent e) {
                    updateGenerateButtonState();
                }
            });
        }

        textFieldPanel.revalidate();
        textFieldPanel.repaint();
    }

    private void setupTextFieldForEditMode(JTextField textField, String tag) {
        // Получаем текущий плейсхолдер из БД
        String currentPlaceholder = tagDatabase.getPlaceholder(tag);

        // Устанавливаем текст поля
        if (currentPlaceholder == null || currentPlaceholder.isEmpty()) {
            textField.setText("Введите подсказку для " + tag);
            textField.setForeground(Color.GRAY);
        } else {
            textField.setText(currentPlaceholder);
            textField.setForeground(Color.BLACK);
        }

        // Сохраняем оригинальный тег в свойствах поля
        textField.putClientProperty("originalTag", tag);

        // Добавляем обработчики фокуса
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getForeground() == Color.GRAY) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String input = textField.getText().trim();
                if (input.isEmpty()) {
                    textField.setText("Введите подсказку для " + tag);
                    textField.setForeground(Color.GRAY);
                }
            }
        });

        // Добавляем слушатель документа для обновления кнопки
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateGenerateButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateGenerateButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateGenerateButtonState();
            }
        });
    }

    private final Set<String> SPECIAL_TAGS = Set.of(
            "${key_ria_type_x_pr}",
            "${key_ria_type_x_bd59}",
            "${key_ria_type_x_bd34}"
    );

    private void setupTextFieldForNormalMode(JTextField textField, String tag) {
        // Получаем плейсхолдер из БД
        String placeholder = tagDatabase.getPlaceholder(tag);

        // Если значение уже введено - используем его
        String currentValue = tagValuesMap.getOrDefault(tag, "");

        if (!currentValue.isEmpty()) {
            textField.setText(currentValue);
            textField.setForeground(Color.BLACK);
        } else {
            // Форматируем отображение плейсхолдера
            String displayText = placeholder + " (" + tag + ")";
            textField.setText(displayText);
            textField.setForeground(Color.GRAY);
        }

        textField.putClientProperty("placeholder", placeholder);
        textField.putClientProperty("tag", tag); // Сохраняем оригинальный тег

        // Обработчики фокуса
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder + " (" + tag + ")")) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String input = textField.getText().trim();
                if (input.isEmpty()) {
                    String displayedPlaceholder = placeholder + " (" + tag + ")";
                    textField.setText(displayedPlaceholder);
                    textField.setForeground(Color.GRAY);
                    tagValuesMap.remove(tag); // Удаляем значение, если поле пустое
                } else {
                    tagValuesMap.put(tag, input);
                }
            }
        });
        if (SPECIAL_TAGS.contains(tag)) {
            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    handleSpecialTagInput(tag, textField);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    handleSpecialTagInput(tag, textField);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    handleSpecialTagInput(tag, textField);
                }
            });
        }
    }

    private void handleSpecialTagInput(String changedTag, JTextField changedField) {
        String input = changedField.getText().trim();

        if ("1".equals(input)) {
            changedField.setBackground(new Color(220, 255, 220));
            for (String specialTag : SPECIAL_TAGS) {
                if (!specialTag.equals(changedTag)) {
                    JTextField otherField = findTextFieldByTag(specialTag);
                    if (otherField != null) {
                        otherField.setText("0");
                        tagValuesMap.put(specialTag, "0");
                    }
                }
            }
        } else {
            changedField.setBackground(Color.WHITE);
        }
    }

    private JTextField findTextFieldByTag(String tag) {
        for (Component comp : textFieldPanel.getComponents()) {
            if (comp instanceof JTextField) {
                JTextField tf = (JTextField) comp;
                String fieldTag = (String) tf.getClientProperty("tag");
                if (tag.equals(fieldTag)) {
                    return tf;
                }
            }
        }
        return null;
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

    void removeTextFields() {
        textFieldPanel.removeAll();
        textFieldPanel.revalidate();
        textFieldPanel.repaint();
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    void removeFileButtons() {
        buttonPanel.removeAll();
        buttonPanel.revalidate();
        buttonPanel.repaint();
        scrollPaneButton.revalidate();
        scrollPaneButton.repaint();
    }

    private void generateFileButtons(HashMap<String, List<String>> fileTagMap) {
        buttonPanel.removeAll();
        chooseFileLabel.setText("Выбранные файлы: Показать все теги");

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createVerticalStrut(10));

        showAllTagsButton = new JButton("Показать все теги");
        ViewStyles.styleButton(showAllTagsButton);
        showAllTagsButton.setPreferredSize(new Dimension(400, 50));
        showAllTagsButton.setMaximumSize(new Dimension(400, 50));
        showAllTagsButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Модифицированный обработчик для кнопки "Показать все теги"
        showAllTagsButton.addActionListener(e -> {
            if (isEditMode) {
                loadAllTagsFromDatabase();
            } else {
                generateTextFields(getAllTags(fileTagMap));
            }
            chooseFileLabel.setText("Выбранный файл: " + showAllTagsButton.getText());
        });

        if (selectedFiles.length != 0)
            buttonPanel.add(showAllTagsButton);
        buttonPanel.add(Box.createVerticalStrut(10));

        for (String fileName : fileTagMap.keySet()) {
            JButton fileButton = new JButton(fileName);
            fileButton.setPreferredSize(new Dimension(400, 50));
            fileButton.setMaximumSize(new Dimension(400, 50));
            fileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            ViewStyles.styleButton(fileButton);
            fileButton.addActionListener(e -> {
                chooseFileLabel.setText("Выбранный файл: " + fileName);
                List<String> tags = fileTagMap.get(fileName);
                generateTextFields(tags);
            });
            buttonPanel.add(fileButton);
            buttonPanel.add(Box.createVerticalStrut(10));
        }

        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

    private void clearTextFields() {
        for (JTextField textField : findTextFields()) {
            if (isEditMode) {
                // В режиме редактирования загружаем текущие значения из БД
                String tag = (String) textField.getClientProperty("originalTag");
                String placeholder = tagDatabase.getPlaceholder(tag);
                textField.setText(placeholder != null ? placeholder : "");
            } else {
                // В обычном режиме устанавливаем плейсхолдеры
                String placeholder = (String) textField.getClientProperty("placeholder");
                if (placeholder != null) {
                    textField.setText(placeholder + " (" + textField.getClientProperty("tag") + ")");
                    textField.setForeground(Color.BLACK);
                }
            }
        }
    }

    private List<String> getAllTags(HashMap<String, List<String>> fileTagMap) {
        Set<String> allTags = new HashSet<>();
        for (List<String> tags : fileTagMap.values()) {
            allTags.addAll(tags);
        }
        return new ArrayList<>(allTags);
    }

    // Обеспечение видимости поля ввода
    private void fieldVisibility(JTextField field) {
        SwingUtilities.invokeLater(() -> {
            field.scrollRectToVisible(field.getBounds()); // Прокрутка к полю
            scrollPane.getViewport().setViewPosition(new Point(0, field.getY())); // Принудительное обновление позиции
        });
    }

    // Функция заполнения значений тегов
    private void fillTags() {
        tagMap = new TagMap();
        for (JTextField textField : findTextFields()) {
            String tag = (String) textField.getClientProperty("tag");
            String value = textField.getForeground() == Color.GRAY ? "" : textField.getText().trim();
            tagMap.addTag(tag, value);
        }
    }

    private void fillTagsAndCallGeneration() {
        if (!checkSpecialTags()) return;

        fileManager.createFolder();
        fillTags();
        documentGenerator.generateDocument(tagMap, selectedFiles);
        fileManager.openFileOrFolder(fileManager.getOutputFolderPath());
    }

    private boolean checkSpecialTags() {
        // Собираем все специальные теги с проверкой на null
        List<JTextField> specialFields = findTextFields().stream()
                .filter(tf -> {
                    String tag = (String) tf.getClientProperty("tag");
                    String originalTag = (String) tf.getClientProperty("originalTag");

                    // Проверяем, что тег не null и содержится в SPECIAL_TAGS
                    boolean isSpecial = (tag != null && SPECIAL_TAGS.contains(tag)) ||
                            (originalTag != null && SPECIAL_TAGS.contains(originalTag));
                    return isSpecial;
                })
                .collect(Collectors.toList());

        // Если нет полей с специальными тегами - пропускаем проверку
        if (specialFields.isEmpty()) return true;

        // Проверяем наличие хотя бы одной "1"
        boolean hasOne = specialFields.stream()
                .anyMatch(field -> {
                    String value = field.getText().trim();

                    // В режиме редактирования проверяем напрямую
                    if (isEditMode) {
                        return "1".equals(value);
                    }

                    // В обычном режиме игнорируем плейсхолдеры
                    return field.getForeground() != Color.GRAY && "1".equals(value);
                });

        if (!hasOne) {
            JOptionPane.showMessageDialog(
                    this,
                    "Должна быть выбрана минимум одна опция:\n"
                            + String.join("\n", SPECIAL_TAGS),
                    "Ошибка ввода",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        return true;
    }

    private void clearAll() {
        // Очищаем текстовые поля
        removeTextFields();

        // Очищаем кнопки
        removeFileButtons();

        // Очищаем выбранные файлы
        selectedFiles = null;

        // Очищаем карту тегов
        tagValuesMap.clear();

        // Сбрасываем состояние кнопки генерации
        generateButton.setEnabled(false);

        // Обновляем метку выбора файла
        chooseFileLabel.setText("Файлы не выбраны");

        // Очищаем карту файлов и тегов
        if (fileTagMap != null) {
            fileTagMap.clear();
        }
    }
}