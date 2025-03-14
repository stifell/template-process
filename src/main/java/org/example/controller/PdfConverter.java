package org.example.controller;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author stifell on 24.02.2025
 */
public class PdfConverter {
    private final String outputFolderPath;

    public PdfConverter(String outputFolderPath) {
        this.outputFolderPath = outputFolderPath;
    }

    // Метод для чтения всех Word документов и их конвертации в PDF
    void convertAllWordDocumentsToPdf() {
        File wordFolder = new File(outputFolderPath, "Word");
        File pdfFolder = new File(outputFolderPath, "PDF");
        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
        }

        // Читаем файлы из папки Word
        File[] wordFiles = wordFolder.listFiles((dir, name) -> name.endsWith(".docx"));

        if (wordFiles != null) {
            for (File wordFile : wordFiles) {
                convertFile(wordFile, pdfFolder);
            }
        }
    }

    private void convertFile(File wordFile, File pdfFolder) {
        String pdfFileName = wordFile.getName().replace(".docx", ".pdf");
        File pdfFile = new File(pdfFolder, pdfFileName);
        convertDocxToPdf(wordFile.getAbsolutePath(), pdfFile.getAbsolutePath());
    }

    private void convertDocxToPdf(String docPath, String pdfPath) {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            convertWithDocuments4j(docPath, pdfPath);
        } else {
            convertWithLibreOffice(docPath, pdfPath);
        }
    }

    private void convertWithDocuments4j(String docPath, String pdfPath) {
        try (InputStream docxInputStream = new FileInputStream(docPath);
             OutputStream outputStream = new FileOutputStream(pdfPath)) {

            IConverter converter = LocalConverter.builder().build();
            converter.convert(docxInputStream)
                    .as(DocumentType.DOCX)
                    .to(outputStream)
                    .as(DocumentType.PDF)
                    .execute();

            converter.shutDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void convertWithLibreOffice(String docPath, String pdfPath) {
        File pdfFile = new File(pdfPath);
        List<String> command = Arrays.asList(
                "libreoffice",
                "--headless",
                "--convert-to",
                "pdf",
                docPath,
                "--outdir",
                pdfFile.getParent()
        );

        try {
            Process process = new ProcessBuilder(command).start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Ошибка конвертации:: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}