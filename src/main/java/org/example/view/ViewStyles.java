package org.example.view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicCheckBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class ViewStyles {
    static final Font DEFAULT_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font LABEL_FONT = new Font("Arial",Font.BOLD,32);
    static final Color BACKGROUND_COLOR = new Color(255, 255, 255);  // светлый цвет фона
    private static final Color BUTTON_COLOR = new Color(7, 113, 181);      // цвет кнопок
    private static final Color TEXT_COLOR_BUTTON = Color.WHITE;// цвет текста
    static final Color TEXT_COLOR_LABEL = Color.BLACK;// цвет текста
    private static final Color COMBOBOX_BACKGROUND = new Color(255, 255, 255); // Белый фон
    private static final Color COMBOBOX_FOREGROUND = new Color(50, 50, 50);    // Темный текст
    private static final Color COMBOBOX_BORDER_COLOR = new Color(150, 150, 150); // Серый цвет границы

    // Метод для стилизации JLabel
    static void styleLabel(JLabel label) {
        label.setFont(DEFAULT_FONT);
        label.setForeground(TEXT_COLOR_LABEL);
    }

    static void styleStartLabel(JLabel label) {
        label.setFont(LABEL_FONT);
        label.setForeground(BUTTON_COLOR);
    }

    // Метод для стилизации JButton
    static void styleButton(JButton button) {
        button.setFont(DEFAULT_FONT);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(TEXT_COLOR_BUTTON);
        button.setFocusPainted(false); // Убираем рамку вокруг текста при фокусе
    }

    // Метод для стилизации JComboBox
    static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(DEFAULT_FONT);
        comboBox.setBackground(COMBOBOX_BACKGROUND);
        comboBox.setForeground(COMBOBOX_FOREGROUND);
        comboBox.setBorder(BorderFactory.createLineBorder(COMBOBOX_BORDER_COLOR, 1));
        comboBox.setFocusable(false); // Убираем фокусное выделение

        // Устанавливаем пользовательский рендерер
        comboBox.setRenderer(new CustomComboBoxRenderer());

        // Стилизация выпадающего списка
        comboBox.setUI(new ModernComboBoxUI());
    }

    // Метод для стилизации JCheckBox
    static void styleCheckBox(JCheckBox checkBox) {
        checkBox.setFont(DEFAULT_FONT);
        checkBox.setForeground(COMBOBOX_FOREGROUND);
        checkBox.setBackground(COMBOBOX_BACKGROUND);
        checkBox.setFocusPainted(false);
        checkBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Применяем ModernCheckBoxUI для кастомного отображения
        checkBox.setUI(new ModernCheckBoxUI());
    }


    // Внутренний класс для стилизации отдельных элементов выпадающего списка
    static class CustomComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(new EmptyBorder(5, 10, 5, 10)); // Устанавливаем отступы
            label.setFont(DEFAULT_FONT);
            label.setOpaque(true);

            if (isSelected) {
                label.setBackground(new Color(100, 149, 237)); // Синий фон при выборе
                label.setForeground(Color.WHITE);               // Белый текст при выборе
            } else {
                label.setBackground(COMBOBOX_BACKGROUND); // Обычный фон
                label.setForeground(COMBOBOX_FOREGROUND); // Обычный цвет текста
            }

            return label;
        }
    }

    // Внутренний класс для стилизации самой панели JComboBox
    static class ModernComboBoxUI extends javax.swing.plaf.basic.BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton arrowButton = new JButton("▼");
            arrowButton.setBorder(BorderFactory.createEmptyBorder());
            arrowButton.setBackground(COMBOBOX_BACKGROUND);
            arrowButton.setForeground(COMBOBOX_FOREGROUND);
            arrowButton.setFocusPainted(false);
            return arrowButton;
        }
    }
    public static void showStyledMessage(Component parentComponent,
                                         String message,
                                         String title,
                                         int messageType) {
        // Стилизация текста сообщения
        JLabel label = new JLabel(message);
        label.setFont(DEFAULT_FONT);
        label.setForeground(TEXT_COLOR_LABEL);

        // Стилизация кнопки "OK"
        JButton okButton = new JButton("OK");
        styleButton(okButton);
        okButton.setPreferredSize(new Dimension(100, 40));

        // Создаем кастомный диалог
        JOptionPane pane = new JOptionPane(
                label,
                messageType,
                JOptionPane.DEFAULT_OPTION,
                null,
                new Object[]{okButton},
                null // Убираем дефолтный выбор
        );

        JDialog dialog = pane.createDialog(parentComponent, title);
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);

        // Добавляем обработчик клика на кнопку OK
        okButton.addActionListener(e -> {
            pane.setValue(okButton);
            dialog.dispose();
        });

        dialog.setVisible(true);
    }
    // Метод для установки стандартного цвета и фона панели
    static void stylePanel(JPanel panel) {
        panel.setBackground(BACKGROUND_COLOR);
    }
    static void styleTextField(JTextField textField) {
        Font textFieldFont = new Font("Arial", Font.PLAIN, 14); // Шрифт текстового поля
        Color backgroundColor = new Color(250, 250, 250); // Светло-серый фон
        Color borderColor = new Color(150, 150, 150); // Цвет границы

        textField.setFont(textFieldFont);
        textField.setBackground(backgroundColor);
        textField.setForeground(Color.BLACK); // Цвет текста
        textField.setCaretColor(Color.BLACK); // Цвет курсора

        Border border = BorderFactory.createLineBorder(borderColor, 1); // Тонкая серая граница
        textField.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 10, 5, 10))); // Отступы внутри текстового поля
    }

    static void styleScrollBar(JScrollBar scrollBar) {
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                // Устанавливаем цвета
                this.thumbColor = new Color(100, 100, 200); // Цвет ползунка
                this.trackColor = new Color(220, 220, 220); // Цвет дорожки
                this.thumbDarkShadowColor = new Color(50, 50, 150); // Тень ползунка
            }
        });
    }

    static class ModernCheckBoxUI extends BasicCheckBoxUI {
        private static final Color CHECKBOX_BORDER_COLOR = new Color(150, 150, 150); // Цвет границы
        private static final Color CHECKBOX_SELECTED_BACKGROUND = new Color(7, 113, 181); // Цвет при выборе
        private static final Color CHECKBOX_SELECTED_TICK = Color.WHITE; // Цвет галочки при выборе

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton checkBox = (AbstractButton) c;
            ButtonModel model = checkBox.getModel();

            // Отрисовка квадрата
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = 18;
            int x = 2; // Смещаем квадрат немного вправо
            int y = (checkBox.getHeight() - size) / 2;

            if (model.isSelected()) {
                // Рисуем залитый квадрат при выборе
                g2.setColor(CHECKBOX_SELECTED_BACKGROUND);
                g2.fillRoundRect(x, y, size, size, 5, 5);
                // Рисуем галочку
                g2.setColor(CHECKBOX_SELECTED_TICK);
                g2.setStroke(new BasicStroke(2));
                g2.drawLine(x + 4, y + 9, x + 8, y + 13);
                g2.drawLine(x + 8, y + 13, x + 14, y + 5);
            } else {
                // Граница для невыбранного состояния
                g2.setColor(CHECKBOX_BORDER_COLOR);
                g2.drawRoundRect(x, y, size, size, 5, 5);
            }

            g2.dispose();

            // Отрисовка текста рядом с чекбоксом
            g.setFont(checkBox.getFont());
            g.setColor(checkBox.getForeground());
            FontMetrics fm = g.getFontMetrics();
            String text = checkBox.getText();
            int textX = size + 8; // Отступ текста от квадрата
            int textY = (checkBox.getHeight() + fm.getAscent()) / 2 - 2; // Центрируем текст по высоте
            g.drawString(text, textX, textY);
        }
    }

    //  Метод для стилизации JOptionPane
    static void styleOptionPane(JOptionPane optionPane) {
        // Стилизация содержимого JOptionPane
        Component[] components = optionPane.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                stylePanelComponents((JPanel) component, false); // Не стилизуем текст
            }
        }
        // Установка белого фона для самого JOptionPane
        optionPane.setBackground(Color.WHITE);
        optionPane.setOpaque(true);
    }

    // Рекурсивная стилизация компонентов JPanel
    private static void stylePanelComponents(JPanel panel, boolean styleText) {
        panel.setBackground(Color.WHITE); // Устанавливаем белый фон для панели
        for (Component component : panel.getComponents()) {
            if (component instanceof JLabel && styleText) {
                styleLabel((JLabel) component); // Стилизуем метку только при необходимости
            } else if (component instanceof JButton) {
                styleButton((JButton) component); // Стилизуем кнопку
            } else if (component instanceof JPanel) {
                stylePanelComponents((JPanel) component, styleText); // Рекурсивно стилизуем вложенные панели
            }
        }
    }
}

