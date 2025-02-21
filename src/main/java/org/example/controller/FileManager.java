package org.example.controller;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;

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

    // Метод для чтения всех Word документов и их конвертации в PDF
    void convertAllWordDocumentsToPdf() {
        File wordFolder = new File(outputFolderPath, "Word");
        File pdfFolder = new File(outputFolderPath, "PDF");

        createFolderIfNotExists(pdfFolder);
        // Читаем файлы из папки Word
        File[] wordFiles = wordFolder.listFiles((dir, name) -> name.endsWith(".docx"));

        if (wordFiles != null) {
            for (File wordFile : wordFiles) {
                String pdfFileName = wordFile.getName().replace(".docx", ".pdf");
                File pdfFile = new File(pdfFolder, pdfFileName);
                convertDocxToPdf(wordFile.getAbsolutePath(), pdfFile.getAbsolutePath());
            }
        }
    }

    // Метод конвертации DOCX в PDF
    private void convertDocxToPdf(String docPath, String pdfPath) {
        IConverter converter = null;
        try {
            InputStream docxInputStream = new FileInputStream(docPath);
            OutputStream outputStream = new FileOutputStream(pdfPath);
            converter = LocalConverter.builder().build();
            converter.convert(docxInputStream).as(DocumentType.DOCX).to(outputStream).as(DocumentType.PDF).execute();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (converter != null) {
                // Явно закрываем конвертер
                converter.shutDown();
            }
        }
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
    public File[] preprocessBlockFiles(File [] selectedFiles){
        List<File> processedFiles = new ArrayList<>();
        // Проходим по всем выбранным файлам
        for (File file : selectedFiles) {
            // Если имя файла начинается с "block_", его нужно предварительно обработать
            if (file.getName().startsWith("block_")) {
                try {
                    // Формируем новый путь для файла без префикса "block_"
                    // Например, "C:\Documents\block_example.docx" -> "C:\Documents\example.docx"
                    String originalPath = file.getAbsolutePath();
                    String newFilePath = originalPath.replace("block_", "");

                    // Создаём объект BlockProcessor для обработки данного файла
                    BlockProcessor processor = new BlockProcessor(file);

                    // сохраняет обновлённое содержимое в том же пути но с новым названием
                    processor.processBlockFile(newFilePath);

                    // Добавляем новый обработанный файл в список
                    processedFiles.add(new File(newFilePath));
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
}
