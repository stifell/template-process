package org.example.controller;

import org.example.model.TagDatabase;
import org.example.model.TagMap;
import org.example.view.ViewModelTable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateFileUsingTable {
    // Загрузка данных из CSV файла
    private List<TagMap> tagMaps;
    public GenerateFileUsingTable() {
        tagMaps = new ArrayList<>();
    }

    public List<TagMap> readTableFile(String csvFilePath) {
        try (// Укажите правильную кодировку вашего файла CSV
             BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFilePath),"cp1251"));
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length > 2) {
                    String tag = parts[1].trim(); // Тег
                    for (int i = 2; i < parts.length; i++) { // Начинаем со столбца данных
                        if (tagMaps.size() < i - 1) {
                            tagMaps.add(new TagMap()); // Инициализируем Map для нового столбца
                        }
                        String value = parts[i].trim();
                        tagMaps.get(i - 2).addTag(tag, value); // Добавляем значение в соответствующий Map
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tagMaps;
    }
}