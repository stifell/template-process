package org.example.controller;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.example.view.ViewModelStartScreen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author stifell on 13.02.2025
 */
public class BlockProcessor {
    private final File file;
    // Регулярное выражение для обоих типов команд
    private static final Pattern DUPLICATE_ANY_PATTERN = Pattern.compile(
            "\\$\\{(DUPLICATE|DUPLICATE_AUTHORS)\\(((?:\\d+)|count_authors)(?:,\\s*(\\w+))?\\)\\[(.*?)\\]\\}",
            Pattern.DOTALL
    );
    private static final String MODE_NEWLINE = "newline";
    private static final String MODE_SPACE = "space";
    private static final int DEFAULT_FONT_SIZE = 12;

    public BlockProcessor(File file) {
        this.file = file;
    }

    public void processBlockFile(String newFilePath) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new FileInputStream(file))) {
            // Обработка параграфов документа – итерируемся по копии списка, поскольку могут быть ситуации,
            // когда необходимо добавить параграфы
            List<XWPFParagraph> paragraphs = new ArrayList<>(doc.getParagraphs());
            for (XWPFParagraph p : paragraphs) {
                processContainer(p);
            }
            // Обработка таблиц
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        // Итерируемся по копии списка параграфов ячейки
                        List<XWPFParagraph> cellParagraphs = new ArrayList<>(cell.getParagraphs());
                        for (XWPFParagraph p : cellParagraphs) {
                            processContainer(p);
                        }
                    }
                }
            }
            saveFile(newFilePath, doc);
        }
    }

    private void processContainer(XWPFParagraph paragraph) {
        String text = paragraph.getText();
        Matcher matcher = DUPLICATE_ANY_PATTERN.matcher(text);
        if (!matcher.find()) return;

        // Группа 1: тип команды (DUPLICATE или DUPLICATE_AUTHORS)
        boolean authorsBlock = "DUPLICATE_AUTHORS".equals(matcher.group(1));
        // Группа 2: количество копий
        int copies = authorsBlock ? ViewModelStartScreen.selectedNumber : Integer.parseInt(matcher.group(2));
        // Группа 3: режим (например, newline или space)
        String mode = matcher.group(3) != null ? matcher.group(3) : MODE_NEWLINE;
        // Группа 4: текст, который нужно продублировать
        String content = matcher.group(4);
        int contentStart = text.indexOf(content, matcher.start());
        int contentEnd = contentStart + content.length();

        // Находим оригинальные стили
        List<RunStyle> styles = extractStyles(paragraph, contentStart, contentEnd);
        // Удаляем всю оригинальную команду
        removeCommandRuns(paragraph, matcher.start(), matcher.end());

        // Выбираем нужный метод дублирования по типу команды
        if (authorsBlock) {
            if (MODE_SPACE.equals(mode)) {
                processAuthorsBlockSpace(paragraph, copies, styles);
            } else {
                processAuthorsBlockNewline(paragraph, copies, styles);
            }
        } else { // DUPLICATE
            if (MODE_SPACE.equals(mode)) {
                insertDuplicatedContentSpace(paragraph, copies, styles);
            } else {
                insertDuplicatedContent(paragraph, copies, styles);
            }
        }
    }

    // Режим newline для DUPLICATE_AUTHORS: все копии выделяются новыми параграфами для разделения новой строки
    private void processAuthorsBlockNewline(XWPFParagraph originalParagraph, int copies, List<RunStyle> styles) {
        IBody body = originalParagraph.getBody();
        String fullText = buildFullText(styles);
        String firstAuthorText = replaceXInVariables(fullText, 1);
        insertStyledText(originalParagraph, styles, firstAuthorText);

        // Вставляем новые параграфы после оригинального
        XWPFParagraph currentPara = originalParagraph;
        for (int i = 2; i <= copies; i++) {
            // Создаем курсор после текущего параграфа
            XmlCursor cursor = currentPara.getCTP().newCursor();
            cursor.toNextSibling(); // Перемещаемся за текущий параграф
            XWPFParagraph newPara = body.insertNewParagraph(cursor);
            cursor.dispose();
            copyParagraphStyle(originalParagraph, newPara);
            String replacedText = replaceXInVariables(fullText, i);
            insertStyledText(newPara, styles, replacedText);
            currentPara = newPara; // Обновляем текущий параграф для следующей итерации
        }
    }

    // Режим space для DUPLICATE_AUTHORS: все копии вставляются в один параграф, разделённые пробелом
    private void processAuthorsBlockSpace(XWPFParagraph originalParagraph, int copies, List<RunStyle> styles) {
        // Собираем полный текст из стилей
        String fullText = buildFullText(styles);
        // Вставляем все копии в один абзац, разделяя пробелами
        for (int i = 1; i <= copies; i++) {
            if (i > 1) {
                XWPFRun spaceRun = originalParagraph.createRun();
                spaceRun.setText(" ");
            }
            String replacedText = replaceXInVariables(fullText, i);
            insertStyledText(originalParagraph, styles, replacedText);
        }
    }

    // Режим newline для DUPLICATE
    private void insertDuplicatedContent(XWPFParagraph p, int copies, List<RunStyle> styles) {
        IBody body = p.getBody();
        for (int i = 0; i < copies; i++) {
            XWPFParagraph currentParagraph = (i == 0) ? p : body.insertNewParagraph(p.getCTP().newCursor());
            // Копируем стиль из исходного абзаца
            if (i > 0) {
                copyParagraphStyle(p, currentParagraph);
            }

            // Вставляем текст с сохранением стилей для каждого Run
            for (RunStyle runStyle : styles) {
                XWPFRun newRun = currentParagraph.createRun();
                applyStyle(newRun, runStyle);
                newRun.setText(runStyle.text);
            }
        }
    }

    // Режим space для DUPLICATE
    private void insertDuplicatedContentSpace(XWPFParagraph p, int copies, List<RunStyle> styles) {
        for (int i = 0; i < copies; i++) {
            if (i > 0) {
                XWPFRun spaceRun = p.createRun();
                spaceRun.setText(" ");
            }
            for (RunStyle runStyle : styles) {
                XWPFRun newRun = p.createRun();
                applyStyle(newRun, runStyle);
                newRun.setText(runStyle.text);
            }
        }
    }

    // Вставляет текст в абзац, распределяя его по Run-ам с сохранением стилей
    private void insertStyledText(XWPFParagraph paragraph, List<RunStyle> styles, String text) {
        List<String> textParts = splitTextByOriginalLengths(text, styles);
        for (int i = 0; i < textParts.size(); i++) {
            String part = textParts.get(i);
            RunStyle runStyle = (i < styles.size()) ? styles.get(i) : styles.get(styles.size() - 1);
            XWPFRun newRun = paragraph.createRun();
            applyStyle(newRun, runStyle);
            newRun.setText(part);
        }
    }

    private List<String> splitTextByOriginalLengths(String text, List<RunStyle> styles) {
        List<String> parts = new ArrayList<>();
        int currentIndex = 0;
        for (RunStyle runStyle : styles) {
            int length = runStyle.text.length();
            if (currentIndex >= text.length()) break;

            int endIndex = Math.min(currentIndex + length, text.length());
            parts.add(text.substring(currentIndex, endIndex));
            currentIndex = endIndex;
        }
        // Добавляем остаток текста, если он есть
        if (currentIndex < text.length()) {
            parts.add(text.substring(currentIndex));
        }

        return parts;
    }

    private String buildFullText(final List<RunStyle> styles) {
        final StringBuilder sb = new StringBuilder();
        for (RunStyle style : styles) {
            sb.append(style.text);
        }
        return sb.toString();
    }

    private String replaceXInVariables(String text, int authorNumber) {
        // 1. Замена переменных внутри ${...}
        Pattern varPattern = Pattern.compile("\\$\\{(.*?)\\}");
        Matcher varMatcher = varPattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (varMatcher.find()) {
            String varContent = varMatcher.group(1);
            String replacedVar = varContent.replaceAll("key_ria_authorX", "key_ria_author" + authorNumber);
            varMatcher.appendReplacement(sb, Matcher.quoteReplacement("${" + replacedVar + "}"));
        }
        varMatcher.appendTail(sb);
        String result = sb.toString();

        // 2. Замена "Автор X" в обычном тексте
        result = result.replaceAll("Автор X", "Автор " + authorNumber);
        return result;
    }

    private void removeCommandRuns(XWPFParagraph paragraph, int cmdStart, int cmdEnd) {
        List<XWPFRun> runs = new ArrayList<>(paragraph.getRuns());
        int pos = 0;
        for (XWPFRun run : runs) {
            String runText = run.getText(0);
            if (runText == null) continue;
            int runStart = pos;
            int runEnd = pos + runText.length();
            if (runEnd > cmdStart && runStart < cmdEnd) {
                paragraph.removeRun(paragraph.getRuns().indexOf(run));
            }
            pos = runEnd;
        }
    }

    private void copyParagraphStyle(XWPFParagraph source, XWPFParagraph target) {
        target.setAlignment(source.getAlignment());
        target.setIndentationFirstLine(source.getIndentationFirstLine());
        target.setIndentationLeft(source.getIndentationLeft());
        target.setIndentationRight(source.getIndentationRight());
        target.setSpacingBefore(source.getSpacingBefore());
        target.setSpacingAfter(source.getSpacingAfter());
        target.setSpacingBetween(source.getSpacingBetween());
        target.setStyle(source.getStyle());
    }

    private List<RunStyle> extractStyles(XWPFParagraph p, int start, int end) {
        List<RunStyle> styles = new ArrayList<>();
        XWPFDocument doc = p.getDocument();
        int pos = 0;
        for (XWPFRun run : p.getRuns()) {
            String runText = run.getText(0);
            if (runText == null) continue;
            int runStart = pos;
            int runEnd = pos + runText.length();
            pos = runEnd;
            if (runEnd < start) continue;
            if (runStart > end) break;
            // Вычисляем пересечение с целевой областью
            int intersectStart = Math.max(runStart, start);
            int intersectEnd = Math.min(runEnd, end);
            if (intersectStart >= intersectEnd) continue;
            String part = runText.substring(
                    intersectStart - runStart,
                    intersectEnd - runStart
            );
            // Иногда возникает ошибка: getFontSize() возвращает -1
            int fontSize = run.getFontSize();
            if (fontSize == -1) {
                // Пробуем взять размер по умолчанию из стилей документа
                if (doc.getStyles() != null && doc.getStyles().getDefaultRunStyle() != null) {
                    int defaultFontSize = doc.getStyles().getDefaultRunStyle().getFontSize();
                    fontSize = (defaultFontSize != -1) ? defaultFontSize : DEFAULT_FONT_SIZE;
                } else {
                    fontSize = DEFAULT_FONT_SIZE; // значение по умолчанию
                }
            }
            styles.add(new RunStyle(
                    part,
                    run.isBold(),
                    run.isItalic(),
                    run.getFontFamily(),
                    fontSize,
                    run.getColor(),
                    run.getUnderline()
            ));
        }
        return styles;
    }

    private void applyStyle(XWPFRun run, RunStyle style) {
        run.setBold(style.bold);
        run.setItalic(style.italic);
        run.setFontFamily(style.fontFamily);
        run.setFontSize(style.fontSize);
        run.setColor(style.color);
        run.setUnderline(style.underline);
        // Можно добавить другие свойства стиля по необходимости
    }

    private class RunStyle {
        final String text;
        final boolean bold;
        final boolean italic;
        final String fontFamily;
        final int fontSize;
        final String color;
        final UnderlinePatterns underline;

        RunStyle(String text, boolean bold, boolean italic, String fontFamily, int fontSize, String color, UnderlinePatterns underline) {
            this.text = text;
            this.bold = bold;
            this.italic = italic;
            this.fontFamily = fontFamily;
            this.fontSize = fontSize;
            this.color = color;
            this.underline = underline;
        }
    }

    private void saveFile(String filePath, XWPFDocument doc) throws IOException {
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            doc.write(out);
        }
    }
}