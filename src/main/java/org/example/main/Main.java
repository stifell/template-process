package org.example.main;

import org.example.controller.DocumentGenerator;
import org.example.view.ViewModelStartScreen;

import javax.swing.*;
import java.awt.*;

public class Main {
    public JFrame frame;
    public ViewModelStartScreen viewModelStartScreen;
    private DocumentGenerator documentGenerator;
    private Main() {
        documentGenerator = new DocumentGenerator(this);
        viewModelStartScreen = new ViewModelStartScreen(this, documentGenerator);
        generateFrame();
    }
    public void generateFrame(){
        frame = new JFrame("Генерация документов"); // Создаем главное окно
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Устанавливаем операцию закрытия
        frame.setSize(300,600);
        frame.setMinimumSize(new Dimension(500, 820));
        frame.getContentPane().add(viewModelStartScreen); // Добавляем ViewModel в контейнер главного окна
        frame.setLocationRelativeTo(null);
        frame.setVisible(true); // Делаем окно видимым
    }

    public void disposeFrame(Frame frame){
        frame.dispose();
    }

    public static void main(String[] args) {
        new Main();
    }
}