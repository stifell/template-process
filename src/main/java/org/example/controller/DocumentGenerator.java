package org.example.controller;

import org.example.model.*;
import org.example.view.ViewModelStartScreen;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static org.example.view.ViewModelStartScreen.convertToPdfCheckBox;

/**
 * @author Денис on 21.05.2024
 */
public class DocumentGenerator {
    private FileManager fileManager;
    private TagMap copyTagMap;

    public DocumentGenerator(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void generateDocument(TagMap tagMap, File[] selectedFiles) {
        // Создаем изменяемый список для хранения файлов, которые нужно обработать
        List<File> filesToProcess = new ArrayList<>(List.of(selectedFiles));
        int countAuthors = ViewModelStartScreen.selectedNumber;
        if (countAuthors > 1) {
            fileManager.preprocessAdditionalFiles(filesToProcess, countAuthors);
            // Проверяем, есть ли файлы с ключевыми словами
            boolean hasSpecialFiles = filesToProcess.stream().map(File::getName)
                    .anyMatch(name -> name.contains("main") || name.contains("additional") || name.contains("multi"));
            if (hasSpecialFiles)
                workWithSpecialFiles(filesToProcess, tagMap, countAuthors);
        }

        // В цикле обработки обычных файлов
        for (File file : filesToProcess) {
            String fileName = file.getName();
            String prefix = getFileNumberPrefix(fileName);
            String cleanedName = fileName
                    .replaceAll("^(main_|additional_|multi_|block_)", "")
                    .replace(".docx", "");
            String newFileName = !prefix.isEmpty() ?
                    prefix + cleanedName + ".docx" :
                    fileName;

            replaceText(file, tagMap, newFileName);
        }

        // Конвертация в PDF, если включена опция convertToPdf
        if (convertToPdfCheckBox.isSelected()) {
            PdfConverter pdfConverter = new PdfConverter(fileManager.getOutputFolderPath());
            pdfConverter.convertAllWordDocumentsToPdf();
        }
    }

    private void workWithSpecialFiles(List<File> filesToProcess, TagMap tagMap, int countAuthors) {
        copyTagMap = tagMap.copyTagMap();
        Authors additionalAuthors = new Authors(countAuthors);
        SharedTagProcessor sharedTagProcessor = new SharedTagProcessor();
        sharedTagProcessor.fillAuthorsTags(copyTagMap, additionalAuthors);

        int counterForMulti = 3;
        Iterator<File> iterator = filesToProcess.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();
            String fileName = file.getName();
            if (fileName.contains("main")) {
                // Объединяем теги первого автора и общие теги
                processMainFile(file, fileName, additionalAuthors);
                iterator.remove();
            } else if (fileName.contains("multi")) {
                processMultiFile(file, tagMap, countAuthors, fileName, counterForMulti);
                iterator.remove();
                counterForMulti++;
            }
        }
    }

    private void processMainFile(File file, String fileName, Authors additionalAuthors) {
        TagMap combinedTagMap = copyTagMap.copyTagMap();
        TagMap tagMapOne = additionalAuthors.getMainTagMap();
        combinedTagMap.combineTags(tagMapOne);

        String lastname = getLastname(tagMapOne);
        String cleanedFileName = fileName.replace("main_", "").replace(".docx", "");

        replaceText(file, combinedTagMap, fileName.replace(fileName,
                "2.1 " + cleanedFileName + "_" + lastname + ".docx"));
    }

    private void processMultiFile(File file, TagMap tagMap, int countAuthors, String fileName, int counterForMulti) {
        copyTagMap = tagMap.copyTagMap();
        // Разбиваем теги по типу "key_ria_authorX..." для каждого автора
        Authors multiAuthors = new Authors(countAuthors);
        MultiTagProcessor multiTagProcessor = new MultiTagProcessor();
        multiTagProcessor.fillAuthorsTags(copyTagMap, multiAuthors);
        // Обрабатываем файлы, которые должны генерироваться для каждых авторов
        for (int i = 0; i < countAuthors; i++) {
            TagMap multiTagMap = copyTagMap.copyTagMap();
            TagMap tagMapOne = multiAuthors.getTagMapByIndex(i);
            multiTagMap.combineTags(tagMapOne);

            String lastname = getLastname(tagMapOne);
            String cleanedFileName = fileName.replace("multi_", "").replace(".docx", "");

            replaceText(file, multiTagMap, fileName.replace(fileName,
                    counterForMulti + "." + (i + 1) + " " + cleanedFileName + "_" + lastname + ".docx"));
        }
    }

    private String getLastname(TagMap tagMap) {
        if (tagMap == null) {
            return "";
        }

        Pattern pattern = Pattern.compile("\\$\\{key_ria_author[1-9]_lastname\\}");
        for (String key : tagMap.keySet()) {
            if (pattern.matcher(key).matches()) {
                return tagMap.get(key);
            }
        }
        return "";
    }

    private void replaceText(File file, TagMap tags, String authorPrefix) {
        String fileName = file.getName();
        // Проверка наличия пустых значений в TagMap
        boolean hasEmptyValues = checkForEmptyValues(tags);
        if (!hasEmptyValues) {
            String newFolderPath = fileManager.getOutputFolderPath() + File.separator + "Word";
            fileManager.createFolderIfNotExists(new File(newFolderPath));
            String newFilePath = newFolderPath + File.separator + authorPrefix;
            if (fileName.endsWith(".doc")) {
                WordDOC.createFile(tags, file, newFilePath);
            } else if (fileName.endsWith(".docx")) {
                WordDOCX.createFile(tags, file, newFilePath);
            } else
                System.out.println("Файл " + fileName + " не формата doc/docx");
        } else {
            System.out.println("Не удалось изменить файл" + fileName + ".Обнаружены пустые значения.");
        }
    }

    // Добавляем метод для определения префикса обычных файлов
    private String getFileNumberPrefix(String fileName) {
        if (fileName.contains("Титульный лист для листинга")) return "1. ";
        if (fileName.contains("Заявление РП (доп)")) return "2.";
        if (fileName.contains("Реферат программы ЭВМ")) return "6. ";
        if (fileName.contains("Уведомление заявка")) return "7. ";
        if (fileName.contains("Уведомление о создании РИД")) return "8. ";
        if (fileName.startsWith("Договор ЭВМ")) return "9. ";
        return "";
    }

    // функция проверяет есть ли в теге что-то или там пусто, или null и выводит соответсвующее сообщение в консоль,
    // а если значение есть, то возвращает false
    private boolean checkForEmptyValues(TagMap tagMap) {
        boolean hasEmptyValues = false;
        for (Map.Entry<String, String> entry : tagMap.entrySet()) {
            String value = entry.getValue();
            if (value == null || value.isEmpty()) {
                // Обработка пустого значения
                System.out.println("Пустое значение для ключа: " + entry.getKey());
                hasEmptyValues = true;
            }
        }
        return hasEmptyValues;
    }
}