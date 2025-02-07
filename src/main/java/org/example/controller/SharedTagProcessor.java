package org.example.controller;

import org.example.model.Authors;
import org.example.model.TagMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author stifell on 07.02.2025
 */
public class SharedTagProcessor implements TagProcessor {
    // Меняем нумерацию тегов в additional-файле, для main-файла не меняется, замена индексов начинается с 2
    @Override
    public void fillAuthorsTags(TagMap originalTagMap, Authors authors) {
        if (authors == null || authors.getTagMaps().isEmpty()) {
            return;
        }
        Map<String, String> tagMapCopy = new HashMap<>(originalTagMap.getTagMap());
        for (Map.Entry<String, String> entry : tagMapCopy.entrySet()) {
            String tag = entry.getKey();
            String newTag = tag;
            if (tag.contains("key_ria_author") && tag.matches(".*\\d+.*")) {
                int authorIndex = extractAuthorIndex(tag);
                if (authorIndex > 3) {
                    // Определяем новый индекс автора, начиная с 2
                    int newIndex;
                    if (authorIndex > 6) {
                        newIndex = 2 + (authorIndex - 7) % 3;
                    } else {
                        newIndex = 2 + (authorIndex - 4) % 4;
                    }
                    newTag = tag.replaceFirst("X\\d+", "X" + newIndex);
                }
                authors.addTagToAuthor(authorIndex, newTag, entry.getValue());
                originalTagMap.removeTag(tag); // Удаляем тег из tagMap
            }
        }
    }
}
