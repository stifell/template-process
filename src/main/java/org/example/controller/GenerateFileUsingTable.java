package org.example.controller;

import org.example.model.TagMap;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GenerateFileUsingTable {
    private TagMap tagMap;
    // Загрузка данных из CSV файла
    private Map<String, String> dataMap = new HashMap<>();
    GenerateFileUsingTable(TagMap tagMap, String outputFolderPath) {
        this.tagMap = tagMap;
        // Путь к CSV файлу с данными
        String csvFilePath = outputFolderPath + File.separator + "tags.csv";
        try (// Укажите правильную кодировку вашего файла CSV
             BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFilePath),"cp1251"));
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";", 2);
                if (parts.length == 2) {
                    String tag = parts[0].trim(); // Убираем лишние пробелы
                    // Преобразование строки в байтовый массив с указанной кодировкой (например, windows-1251)
                    byte[] bytes = parts[1].trim().getBytes("cp1251");
                    // Преобразование байтового массива обратно в строку с другой кодировкой (например, windows-1251)
                    String newValue = new String(bytes, "cp1251");
                    if (!newValue.isEmpty()) {
                        dataMap.put(tag, newValue);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void fillTagsUsingTable() {
        for(HashMap.Entry<String, String> entry: dataMap.entrySet()) {
            String tag = entry.getKey();
            tagMap.addTag(tag, dataMap.get(tag));
        }
    }
}