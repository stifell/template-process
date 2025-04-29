package org.example.controller;

import java.awt.*;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileManager {
    private String outputFolderPath;
    private String targetFolderPath;

    public void setTargetFolderPath(String targetFolderPath) {
        this.targetFolderPath = targetFolderPath;
    }

    public String getOutputFolderPath() {
        return outputFolderPath;
    }

    public void setOutputFolderPath(String outputFolderPath) {
        this.outputFolderPath = outputFolderPath;
    }

    // Метод для создания папки сохранения
    public void createFolder() {
        // Получаем текущую дату и время
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        String currentDateTime = sdf.format(new Date());
        targetFolderPath = targetFolderPath != null ? targetFolderPath : getClass().getClassLoader().getResource("").getPath();
        targetFolderPath = URLDecoder.decode(targetFolderPath, StandardCharsets.UTF_8);
        outputFolderPath = targetFolderPath + File.separator + currentDateTime;
        // Создаем папку
        File outputFolder = new File(outputFolderPath);
        createFolderIfNotExists(outputFolder);
    }

    // Метод для открытия папки или файла
    public void openFileOrFolder(String path) {
        File file = new File(path);
        if (file.exists()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Файл или папка не найдены: " + path);
        }
    }

    // Создание папки при ее отсутствии
    void createFolderIfNotExists(File folder) {
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    // Дублирование текста с помощью команды-тега
    public File[] preprocessBlockFiles(File[] selectedFiles) {
        List<File> processedFiles = new ArrayList<>();
        // Проходим по всем выбранным файлам
        for (File file : selectedFiles) {
            // Если имя файла начинается с "block_", его нужно предварительно обработать
            if (file.getName().startsWith("block_") && file.getName().endsWith(".docx")) {
                try {
                    // Получаем родительскую директорию исходного файла
                    String parentDir = file.getParent();
                    // Формируем объект новой директории "block_files"
                    File blockFilesDir = new File(parentDir, "block_files");
                    // Если директория не существует, создаём её
                    if (!blockFilesDir.exists()) {
                        blockFilesDir.mkdirs();
                    }

                    // Удаляем префикс "block_" из имени файла
                    String newFileName = file.getName().replace("block_", "");
                    // Формируем новый путь файла в директории block_files
                    File newFile = new File(blockFilesDir, newFileName);

                    // Создаём объект BlockProcessor для обработки файла
                    BlockProcessor processor = new BlockProcessor(file);
                    // Сохраняем обработанный файл по новому пути
                    processor.processBlockFile(newFile.getAbsolutePath());

                    // Добавляем новый обработанный файл в список
                    processedFiles.add(newFile);
                } catch (IOException exception) {
                    System.err.println("Ошибка обработки block-файла: " + file.getName());
                    exception.printStackTrace();
                }
            } else {
                // Если файл не содержит префикс "block_", добавляем его без изменений
                processedFiles.add(file);
            }
        }
        // Возвращаем массив с новыми файлами для дальнейшей генерации
        return processedFiles.toArray(new File[0]);
    }

    // метод для обработки additional файла
    public void preprocessAdditionalFiles(List<File> filesToProcess, int countAuthors) {
        for (File file : filesToProcess) {
            // Если имя файла начинается с "additional_", его нужно предварительно обработать
            if (file.getName().startsWith("additional_") && file.getName().endsWith(".docx")) {
                // Получаем родительскую директорию исходного файла
                String parentDir = file.getParent();
                // Формируем объект новой директории "additional_files"
                File additionalDir = new File(parentDir, "additional_files");

                String templateName = countAuthors + "_Заявление РП (доп).docx";
                File newFile = new File(additionalDir, templateName);

                if (!newFile.exists()) {
                    System.err.println("Шаблон для additional (" + templateName + ") не найден в " + additionalDir);
                    return;
                }

                filesToProcess.add(newFile);
                filesToProcess.remove(file);

                return;
            }
        }
    }
}