package org.example.view;

import org.example.controller.DocumentGenerator;
import org.example.main.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ViewModelTable extends JPanel {
    private Main main;
    private ViewModelStartScreen viewModelStartScreen;
    private DocumentGenerator documentGenerator;
    JButton generateButtonUsingTable;
    private JButton buttonBackSpace;
    private JComboBox<String> selectFilesForTableComboBox;
    private ViewModelTextFields viewModelTextFields;
    private JLabel fileLabel;
    // Константы для одинакового размера компонентов
    private static final Dimension COMPONENT_SIZE = new Dimension(300, 50);
    // Добавляем contentPanel на уровне класса
    private JPanel contentPanel;

    ViewModelTable(Main main, ViewModelStartScreen viewModelStartScreen, DocumentGenerator documentGenerator) {
        this.viewModelStartScreen = viewModelStartScreen;
        this.documentGenerator = documentGenerator;
        this.main = main;
        this.viewModelTextFields = new ViewModelTextFields(main, viewModelStartScreen, documentGenerator, this);
        ViewStyles.stylePanel(this);
        setLayout(new BorderLayout()); // Используем BorderLayout для основного компонента
        setFocusable(true);
        initializeTable();  // Убедимся, что initializeTable вызывается в конструкторе
    }

    private void initializeTable() {
        // Панель для кнопки BackSpace, размещенной в верхнем левом углу
        JPanel backButtonPanel = new JPanel();
        backButtonPanel.setLayout(new BorderLayout());
        backButtonPanel.setPreferredSize(new Dimension(70, 50));

        // Кнопка "Назад"
        buttonBackSpace = new JButton("⬅");
        Font font = buttonBackSpace.getFont();
        ViewStyles.styleButton(buttonBackSpace);
        buttonBackSpace.setFont(font.deriveFont(Font.PLAIN, 32));
        buttonBackSpace.setPreferredSize(new Dimension(70, 50)); // Размер кнопки
        buttonBackSpace.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                main.switchToPanel(viewModelStartScreen);
            }
        });
        backButtonPanel.add(buttonBackSpace, BorderLayout.NORTH);
        add(backButtonPanel, BorderLayout.WEST); // Добавляем кнопку в верхний левый угол

        // Инициализация contentPanel
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        // Кнопка выбора файла из ViewModelTextFields
        viewModelTextFields.chooseFileButton.setPreferredSize(COMPONENT_SIZE);
        gbc.gridy = 0;
        contentPanel.add(viewModelTextFields.chooseFileButton, gbc);

        // Метка для отображения выбранного файла
        fileLabel = new JLabel("Файл(ы) не выбран(ы):");
        fileLabel.setPreferredSize(new Dimension(290, 30));
        gbc.gridy = 2;
        contentPanel.add(fileLabel, gbc);

        // Кнопка генерации
        generateButtonUsingTable = new JButton("Генерация с помощью таблицы");
        generateButtonUsingTable.setEnabled(false);
        generateButtonUsingTable.setPreferredSize(COMPONENT_SIZE);
        ViewStyles.styleButton(generateButtonUsingTable);
        generateButtonUsingTable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                documentGenerator.generateDocument();
            }
        });
        gbc.gridy = 1;
        contentPanel.add(generateButtonUsingTable, gbc);

        add(contentPanel, BorderLayout.CENTER); // Добавляем панель с остальными компонентами в центр
    }

    void updateComboBox(String[] select) {
        if (selectFilesForTableComboBox != null) {
            contentPanel.remove(selectFilesForTableComboBox);  // Удаляем старый JComboBox из contentPanel
        }

        selectFilesForTableComboBox = new JComboBox<>(select);
        selectFilesForTableComboBox.setPreferredSize(new Dimension(300, 30));
        selectFilesForTableComboBox.setMaximumRowCount(15);
        ViewStyles.styleComboBox(selectFilesForTableComboBox);

        // Добавляем ComboBox в contentPanel с корректными GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridy = 3;  // Убедитесь, что позиция соответствует другим элементам
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
}

