package org.example.controller;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.example.model.TagMap;

import java.io.*;
import java.util.HashMap;

/**
 * Класс WordDOC предоставляет функционал для изменения содержимого документа типа .doc.
 * Он позволяет заменить определенные текстовые метки в документе на указанные значения.
 * Для работы с документом используются библиотека Apache POI и объект POIFSFileSystem.
 */
public class WordDOC extends WordProcessor<HWPFDocument>{
    WordDOC(TagMap tagMap, File file){
        super(tagMap, file);
    }

    public static void createFile(TagMap tagMap, File file, String newFilePath) {
        WordDOC doc = new WordDOC(tagMap, file);
        try {
            doc.changeFile(newFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при изменении файла " + newFilePath, e);
        }
    }

    /**
     * Метод changeFile() извлекает документ test.doc из ресурсов класспути,
     * заменяет определенные текстовые метки в документе и сохраняет изменения.
     * @throws IOException если возникает ошибка ввода-вывода при чтении или записи файла
     */
    @Override
    protected void changeFile(String newFilePath) throws IOException {
        // inputStream - входной поток данных, FileInputStream - чтения байтов из файла
        // POIFSFileSystem - объект для работы с документом Word
        try (InputStream inputStream = new FileInputStream(file);
             POIFSFileSystem fileSystem = new POIFSFileSystem(inputStream)){
            // создание объект для работы с .doc
            HWPFDocument doc = new HWPFDocument(fileSystem);
            // замена текста в doc и сохранение изменений
            doc = replaceText(doc);
            saveFile(newFilePath, doc);
            doc.close();
        }
    }

    /**
     * Метод replaceText() заменяет текстовые метки в документе на указанные значения.
     * @param doc объект HWPFDocument, представляющий документ типа .doc
     * @return объект HWPFDocument с замененным текстом
     */
    @Override
    protected HWPFDocument replaceText(HWPFDocument doc){
        // диапазон, охватывающий весь текст документа
        Range range = doc.getRange();
        for(HashMap.Entry<String, String> entry: tagMap.entrySet()) {
            // получение ключа
            String tag = entry.getKey();
            // получение значения
            String replaceWord = entry.getValue();
            // замена текста в диапазоне
            range.replaceText(tag, replaceWord);
        }
        return doc;
    }

    /**
     * Метод saveFile() сохраняет содержимое документа в файле.
     * @param filePath путь к файлу, в который нужно сохранить документ
     * @param doc объект HWPFDocument, содержащий измененное содержимое документа
     * @throws IOException если возникает ошибка ввода-вывода при записи файла
     */
    @Override
    protected void saveFile(String filePath, HWPFDocument doc) throws IOException {
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            // записывание содержимого документа в файл, который был открыт для записи
            doc.write(out);
        }
    }
}