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

/**
 * @author Денис on 20.05.2024
 */
public class TagExtractor {
    private final String regex = "\\$\\{[^}]+\\}";
    private Pattern pattern;
    private Set<String> uniqueTags = new HashSet<>();
    private HashMap<String, List<String>> fileTagMap = new HashMap<>();
    private TagDatabase tagDatabase;

    public TagExtractor() {
        this.pattern = Pattern.compile(regex);
        this.tagDatabase = new TagDatabase();
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
                                String value = tagDatabase.getPlaceholder(tag);
                                writer.println(value + ";"+tag + ";1");
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
            // Для каждого автора от 1 до countAuthors заменяем число после "key_ria_author"
            for (int i = 1; i <= countAuthors; i++) {
                for (String tag : additionTags) {
                    // Используем replaceAll с группой, чтобы заменить только часть, например:
                    // "key_ria_author1_lastname" → "key_ria_author2_lastname" для i=2
                    String authorTag = tag.replaceAll("(key_ria_author)1", "$1" + i);
                    if (!uniqueTags.contains(authorTag)) {
                        uniqueTags.add(authorTag);

                        // Добавляем новый authorTag в fileTagMap
                        for (Map.Entry<String, List<String>> entry : fileTagMap.entrySet()) {
                            List<String> tagsList = entry.getValue();
                            if (!tagsList.contains(authorTag)) {
                                tagsList.add(authorTag);
                            }
                        }
                        // Работа с базой данных: получаем значение из базы данных или добавляем новый тег
                        String placeholder = tagDatabase.getPlaceholder(authorTag);

                        // Запись в CSV, если это требуется
                        if (useCSV && writer != null) {
                            writer.println(placeholder + ";" + authorTag + ";1");
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

    public Set<String> getUniqueTags() {
        return uniqueTags;
    }
}