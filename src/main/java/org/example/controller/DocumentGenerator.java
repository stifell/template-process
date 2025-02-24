package org.example.controller;

import org.example.model.*;
import org.example.view.ViewModelStartScreen;

import java.io.*;
import java.util.*;
import java.util.List;

import static org.example.view.ViewModelStartScreen.isConvertToPdfSelected;

/**
 * @author Денис on 21.05.2024
 */
public class DocumentGenerator {
    private FileManager fileManager;
    private TagMap copyTagMap;

    public DocumentGenerator(FileManager fileManager) {
        this.fileManager = fileManager;
        copyTagMap = new TagMap();
    }

    public void generateDocument(TagMap tagMap, File[] selectedFiles) {
        // Создаем изменяемый список для хранения файлов, которые нужно обработать
        List<File> filesToProcess = new ArrayList<>(List.of(selectedFiles));
        int countAuthors = ViewModelStartScreen.selectedNumber;
        if (countAuthors > 1) {
            // Проверяем, есть ли файлы с ключевыми словами
            boolean hasSpecialFiles = filesToProcess.stream().map(File::getName)
                    .anyMatch(name -> name.contains("main") || name.contains("additional") || name.contains("multi"));
            if (hasSpecialFiles)
                workWithSpecialFiles(filesToProcess, tagMap, countAuthors);
        }
        for (File file : filesToProcess) {
            replaceText(file, tagMap, file.getName());
        }

        // Конвертация в PDF, если включена опция convertToPdf
        if (isConvertToPdfSelected()) {
            PdfConverter pdfConverter = new PdfConverter(fileManager.getOutputFolderPath());
            pdfConverter.convertAllWordDocumentsToPdf();
        }
    }

    private void workWithSpecialFiles(List<File> filesToProcess, TagMap tagMap, int countAuthors) {
        copyTagMap = new TagMap(new HashMap<>(tagMap.getTagMap()));
        Authors additionalAuthors = new Authors(countAuthors);
        SharedTagProcessor sharedTagProcessor = new SharedTagProcessor();
        sharedTagProcessor.fillAuthorsTags(copyTagMap, additionalAuthors);

        Iterator<File> iterator = filesToProcess.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();
            String fileName = file.getName();
            if (fileName.contains("main")) {
                // Объединяем теги первого автора и общие теги
                processMainFile(file, fileName, additionalAuthors);
                iterator.remove();
            } else if (fileName.contains("additional")) {
                // Обрабатываем дополнительные файлы для остальных авторов
                processAdditionalFile(file, fileName, additionalAuthors);
                iterator.remove();
            } else if (fileName.contains("multi")) {
                processMultiFile(file, tagMap, countAuthors, fileName);
                iterator.remove();
            }
        }
    }

    private void processMainFile(File file, String fileName, Authors additionalAuthors) {
        TagMap combinedTagMap = new TagMap(new HashMap<>(copyTagMap.getTagMap()));
        combinedTagMap.combineTags(additionalAuthors.getMainTagMap());
        replaceText(file, combinedTagMap, fileName.replace("main_", "1_"));
    }

    private void processAdditionalFile(File file, String fileName, Authors additionalAuthors) {
        for (int i = 1; i < additionalAuthors.getTagMaps().size(); i += 3) {
            TagMap additionalTagMap = new TagMap(new HashMap<>(copyTagMap.getTagMap()));
            StringBuilder authorNumbers = new StringBuilder();
            for (int j = 0; j < 3; j++) {
                if (i + j < additionalAuthors.getTagMaps().size()) {
                    additionalTagMap.combineTags(additionalAuthors.getTagMapByIndex(i + j));
                    authorNumbers.append(i + j + 1 + "_");
                }
            }
            replaceText(file, additionalTagMap, fileName.replace("additional_", authorNumbers));
        }
    }

    private void processMultiFile(File file, TagMap tagMap, int countAuthors, String fileName) {
        copyTagMap = new TagMap(new HashMap<>(tagMap.getTagMap()));
        // Разбиваем теги по типу "key_ria_authorX..." для каждого автора
        Authors multiAuthors = new Authors(countAuthors);
        MultiTagProcessor multiTagProcessor = new MultiTagProcessor();
        multiTagProcessor.fillAuthorsTags(copyTagMap, multiAuthors);
        // Обрабатываем файлы, которые должны генерироваться для каждых авторов
        for (int i = 0; i < countAuthors; i++) {
            TagMap multiTagMap = new TagMap(new HashMap<>(copyTagMap.getTagMap()));
            multiTagMap.combineTags(multiAuthors.getTagMapByIndex(i));
            replaceText(file, multiTagMap, fileName.replace("multi_", (i + 1) + "_"));
        }
    }

    private void replaceText(File file, TagMap tags, String authorPrefix) {
        String fileName = file.getName();
        try {
            // Проверка наличия пустых значений в TagMap
            boolean hasEmptyValues = checkForEmptyValues(tags);
            if (!hasEmptyValues) {
                String newFolderPath = fileManager.getOutputFolderPath() + File.separator + "Word";
                fileManager.createFolderIfNotExists(new File(newFolderPath));
                String newFilePath = newFolderPath + File.separator + authorPrefix;
                if (fileName.endsWith(".doc")) {
                    WordDOC wordDOC = new WordDOC(tags, file);
                    wordDOC.changeFile(newFilePath);
                } else if (fileName.endsWith(".docx")) {
                    WordDOCX wordDOCX = new WordDOCX(tags, file);
                    wordDOCX.changeFile(newFilePath);
                } else
                    System.out.println("Файл " + fileName + " не формата doc/docx");
            } else {
                System.out.println("Не удалось изменить файл" + fileName + ".Обнаружены пустые значения.");
            }
        } catch (IOException e) {
            // обработка исключения в случае возникновения ошибки при изменении файла
            System.err.println("Ошибка при изменении файла " + fileName);
            throw new RuntimeException(e);
        }
    }

    // функция проверяет есть ли в теге что-то или там пусто, или null и выводит соответсвующее сообщение в консоль,
    // а если значение есть, то возвращает false
    private boolean checkForEmptyValues(TagMap tagMap) {
        boolean hasEmptyValues = false;
        for (Map.Entry<String, String> entry : tagMap.getTagMap().entrySet()) {
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