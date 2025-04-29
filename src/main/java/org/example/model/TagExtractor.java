package org.example.model;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.example.view.ViewModelStartScreen;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.main.Main.tagDatabase;

/**
 * @author Денис on 20.05.2024
 */
public class TagExtractor {
    private final String regex = "\\$\\{[^}]+\\}";
    private Pattern pattern;
    private Set<String> uniqueTags = new HashSet<>();
    private HashMap<String, List<String>> fileTagMap = new HashMap<>();// это не тег мап

    public TagExtractor() {
        this.pattern = Pattern.compile(regex);
    }

    public void writeTagsToCSV(File[] Files, String csvFilePath) {
        uniqueTags = new HashSet<>();
        Pattern pattern = Pattern.compile(regex);
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(csvFilePath, true), "cp1251")))) {
            for (File file : Files) {
                if (file.isFile() && (file.getName().endsWith(".doc") || file.getName().endsWith(".docx"))) {
                    try {
                        String text = readTextFromFile(file);
                        Matcher matcher = pattern.matcher(text);
                        while (matcher.find()) {
                            String tag = matcher.group();
                            if (!uniqueTags.contains(tag)) {
                                String placeholder = tagDatabase.getPlaceholder(tag);
                                String placeholder_Long = tagDatabase.getPlaceholderLong(tag);
                                if (placeholder_Long == null){
                                    placeholder_Long = placeholder;
                                }

                                // Обработка длинной подсказки для CSV
                                placeholder_Long = placeholder_Long.replace("\n", " ").replace("\r", " "); // Убираем переносы строк
                                        placeholder_Long = placeholder_Long.replace("\"", "\"\""); // Экранируем кавычки

                                // Записываем строку в CSV, заключая длинную подсказку в кавычки
                                writer.println(tag + ";" + placeholder + ";\"" + placeholder_Long + "\";1");
                                uniqueTags.add(tag);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            addCountAuthors(true, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Set<String> writeTagsToSet(File[] files) {
        Set<String> uniqueTags = new HashSet<>();
        Pattern pattern = Pattern.compile(regex);
        for (File file : files) {
            if (file.isFile() && (file.getName().endsWith(".doc") || file.getName().endsWith(".docx"))) {
                try {
                    String text = readTextFromFile(file);
                    Matcher matcher = pattern.matcher(text);
                    while (matcher.find()) {
                        String tag = matcher.group();
                        if (!uniqueTags.contains(tag)) {
//                            System.out.println(tag);
                            uniqueTags.add(tag);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        addCountAuthors(false, null);
        return uniqueTags;
    }

    public HashMap<String, List<String>> writeTagsToMap(File[] files) {
        fileTagMap = new HashMap<>();
        uniqueTags = new HashSet<>();

        for (File file : files) {
            if (file.isFile() && (file.getName().endsWith(".doc") || file.getName().endsWith(".docx"))) {
                try {
                    String text = readTextFromFile(file);
                    Matcher matcher = pattern.matcher(text);
                    List<String> fileTags = new ArrayList<>();

                    while (matcher.find()) {
                        String tag = matcher.group();
                        if (!uniqueTags.contains(tag)) {
                            uniqueTags.add(tag);
                        }
                        if (!fileTags.contains(tag)) {
                            fileTags.add(tag);
                        }
                    }
                    fileTagMap.put(file.getName(), fileTags);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        addCountAuthors(false, null);
        System.out.println("--------------------------------------------------------");
        System.out.println(fileTagMap);
        return fileTagMap;
    }



    private void addCountAuthors(boolean useCSV, PrintWriter writer) {
        Set<String> additionTags = new HashSet<>();
        int countAuthors = ViewModelStartScreen.selectedNumber;

        if (countAuthors > 4) {
            for (String tag : uniqueTags) {
                if (tag.contains("key_ria_author1")) {
                    additionTags.add(tag);
                }
            }
            for (int i = 1; i <= countAuthors; i++) {
                for (String tag : additionTags) {
                    String authorTag = tag.replaceAll("(key_ria_author)1", "$1" + i);
                    if (!uniqueTags.contains(authorTag)) {
                        uniqueTags.add(authorTag);

                        for (Map.Entry<String, List<String>> entry : fileTagMap.entrySet()) {
                            List<String> tagsList = entry.getValue();
                            if (!tagsList.contains(authorTag)) {
                                tagsList.add(authorTag);
                            }
                        }

                        String placeholder = tagDatabase.getPlaceholder(authorTag);
                        String placeholder_Long = placeholder; // Пока дублируем placeholder

                        if (useCSV && writer != null) {
                            writer.println(authorTag + ";" + placeholder + ";" + placeholder_Long + ";1");
                        }
                        System.out.println(authorTag);
                    }
                }
            }
        }
    }



    private String readTextFromFile(File file) throws IOException {
        StringBuilder text = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file.getAbsolutePath())) {
            if (file.getName().endsWith(".doc")) {
                HWPFDocument document = new HWPFDocument(fis);
                WordExtractor extractor = new WordExtractor(document);
                text.append(extractor.getText());
                extractor.close();
            } else if (file.getName().endsWith(".docx")) {
                XWPFDocument document = new XWPFDocument(fis);
                XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                text.append(extractor.getText());
                extractor.close();
            }
        }
        return text.toString();
    }

    public List<String> verifyTagsInCSV(File[] files, File csvFile) {
        Set<String> documentTags = writeTagsToSet(files); // Получаем теги из документов
        Set<String> csvTags = new HashSet<>();
        // Загружаем теги из файла CSV
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "cp1251"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";", 3);
                if (parts.length == 3) {
                    csvTags.add(parts[1].trim()); // Загружаем тег в set
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Проверяем наличие всех тегов в CSV
        List<String> missingTags = new ArrayList<>();
        for (String tag : documentTags) {
            if (!csvTags.contains(tag)) {
                missingTags.add(tag); // Добавляем отсутствующий тег в список
            }
        }

        return missingTags;
    }

    // Загрузка данных из CSV файла
    public List<TagMap> readTableFile(String csvFilePath) {
        List<TagMap> tagMaps = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFilePath), "cp1251"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length > 3) { // Проверяем, что есть минимум 4 колонки
                    String tag = parts[0].trim(); // Тег теперь в первой колонке (индекс 0)
                    // Начинаем обработку данных с 4-й колонки (индекс 3)
                    for (int i = 3; i < parts.length; i++) {
                        if (tagMaps.size() < i - 2) {
                            tagMaps.add(new TagMap()); // Инициализируем Map для нового столбца
                        }
                        String value = parts[i].trim();
                        tagMaps.get(i - 3).addTag(tag, value); // Добавляем значение в соответствующий Map
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tagMaps;
    }
}