package org.example.controller;

import org.example.model.Authors;
import org.example.model.TagMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author stifell on 07.02.2025
 */
public class MultiTagProcessor implements TagProcessor {
    // Меняем нумерацию тегов в multi-файле
    @Override
    public void fillAuthorsTags(TagMap originalTagMap, Authors authors) {
        if (authors == null || authors.getTagMaps().isEmpty()) {
            return;
        }
        Map<String, String> tagMapCopy = new HashMap<>(originalTagMap.getTagMap());
        for (Map.Entry<String, String> entry : tagMapCopy.entrySet()) {
            String tag = entry.getKey();
            if (tag.contains("key_ria_author") && tag.matches(".*\\d+.*")) {
                int authorIndex = extractAuthorIndex(tag);
                String newTag = tag.replaceFirst("X\\d+", "X" + 1);
                authors.addTagToAuthor(authorIndex, newTag, entry.getValue());
                originalTagMap.removeTag(tag); // Удаляем тег из tagMap
            }
        }
    }

}
