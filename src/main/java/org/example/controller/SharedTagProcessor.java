package org.example.controller;

import org.example.model.Authors;
import org.example.model.TagMap;

import java.util.HashMap;
import java.util.Map;

/**
 * SharedTagProcessor обрабатывает теги в additional‑файлах.
 * Для main‑файла теги остаются без изменений, а для дополнительных
 * тег для автора с индексом больше 3 переиндексируется, начиная с 2.
 *
 * @author stifell on 07.02.2025
 */
public class SharedTagProcessor implements TagProcessor {
    // Меняем нумерацию тегов в additional-файле, для main-файла не меняется, замена индексов начинается с 2
    @Override
    public void fillAuthorsTags(TagMap originalTagMap, Authors authors) {
        if (authors == null || authors.getTagMaps().isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : originalTagMap.copyTagMap().entrySet()) {
            String tag = entry.getKey();
            String newTag = tag;
            // Проверяем, что тег содержит "key_ria_author" и цифры
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
                    // Заменяем число сразу после "key_ria_author" на newIndex
                    newTag = tag.replaceFirst("(?<=key_ria_author)\\d+", String.valueOf(newIndex));
                }
                authors.addTagToAuthor(authorIndex, newTag, entry.getValue());
                originalTagMap.removeTag(tag); // Удаляем тег из tagMap
            }
        }
    }
}
