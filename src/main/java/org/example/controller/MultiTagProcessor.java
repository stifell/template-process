package org.example.controller;

import org.example.model.Authors;
import org.example.model.TagMap;

import java.util.HashMap;
import java.util.Map;

/**
 * MultiTagProcessor обрабатывает теги в multi‑файле.
 * Теперь теги должны быть вида ${key_ria_author1_lastname} и т.д.
 * Логика:
 *  1. Если тег содержит "key_ria_author" и цифры, извлекается индекс автора.
 *  2. Затем число сразу после "key_ria_author" заменяется на "1" (для основного тега).
 *  3. Новый тег добавляется в объект Authors, а исходный удаляется из TagMap.
 *
 * @author stifell on 07.02.2025
 */
public class MultiTagProcessor implements TagProcessor {
    // Меняем нумерацию тегов в multi-файле
    @Override
    public void fillAuthorsTags(TagMap originalTagMap, Authors authors) {
        if (authors == null || authors.getTagMaps().isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : originalTagMap.copyTagMap().entrySet()) {
            String tag = entry.getKey();
            // Если тег содержит "key_ria_author" и содержит цифры (например, key_ria_author1_lastname)
            if (tag.contains("key_ria_author") && tag.matches(".*\\d+.*")) {
                int authorIndex = extractAuthorIndex(tag);
                // Заменяем число сразу после "key_ria_author" на "1"
                String newTag = tag.replaceFirst("(?<=key_ria_author)\\d+", "1");
                authors.addTagToAuthor(authorIndex, newTag, entry.getValue());
                originalTagMap.removeTag(tag); // удаляем исходный тег
            }
        }
    }

}
