package org.example.model;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.example.main.Main;

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
    private List<String> tags = new ArrayList<>();
    public Set<String> uniqueTags = new HashSet<>();
    private HashMap<String, List<String>> fileTagMap = new HashMap<>();
    private Main main;
    public TagExtractor(Main main) {
        this.main = main;
        this.pattern = Pattern.compile(regex);

    }
    public void writeTagsToCSV(File[] Files, String folderPath) {
        uniqueTags = new HashSet<>();
        String csvFilePath = folderPath + File.separator + "tags.csv";
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
                                writer.println(tag + ";1");
                                uniqueTags.add(tag);
                            }
                        }
//                        writer.println();
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

    public Set<String> writeTagsToSet(File[] files) {
        uniqueTags = new HashSet<>();
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
        int countAuthors = main.viewModelStartScreen.selectedNumber;

        if (countAuthors > 4) {
            for (String tag : uniqueTags) {
                if (tag.contains("key_ria_authorX1")) {
                    additionTags.add(tag);
                }
            }

            for (int i = 1; i <= countAuthors; i++) {
                for (String tag : additionTags) {
                    String authorTag = tag.replace("X1", "X" + i); // Заменяем "X1" на нужный номер автора
                    if (!uniqueTags.contains(authorTag)) {
                        uniqueTags.add(authorTag);

                        // Добавляем новый authorTag в fileTagMap
                        for (Map.Entry<String, List<String>> entry : fileTagMap.entrySet()) {
                            List<String> tagsList = entry.getValue();
                            if (!tagsList.contains(authorTag)) {
                                tagsList.add(authorTag);
                            }
                        }

                        if (useCSV && writer != null) {
                            writer.println(authorTag + ";1");
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
}