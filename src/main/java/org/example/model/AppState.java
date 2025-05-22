package org.example.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppState {
    private int numberOfAuthors = 1;
    private String saveDirectory;
    private List<String> selectedFilePaths;
    private boolean convertToPdf = false;
    private Map<String, String> tagValues = new HashMap<>();

    // Геттеры и сеттеры
    public int getNumberOfAuthors() { return numberOfAuthors; }
    public void setNumberOfAuthors(int numberOfAuthors) { this.numberOfAuthors = numberOfAuthors; }
    public String getSaveDirectory() { return saveDirectory; }
    public void setSaveDirectory(String saveDirectory) { this.saveDirectory = saveDirectory; }
    public List<String> getSelectedFilePaths() { return selectedFilePaths; }
    public void setSelectedFilePaths(List<String> selectedFilePaths) { this.selectedFilePaths = selectedFilePaths; }
    public Map<String, String> getTagValues() { return tagValues; }
    public boolean isConvertToPdf() { return convertToPdf; }
    public void setConvertToPdf(boolean convertToPdf) { this.convertToPdf = convertToPdf; }
}