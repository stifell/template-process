package org.example.controller;

import org.example.model.Authors;
import org.example.model.TagMap;

/**
 * @author stifell on 07.02.2025
 */
public interface TagProcessor {
    /**
     * Удаляет из исходного TagMap все теги, относящиеся к авторам.
     * Изменяет их нумерацию согласно логике конкретного типа файла.
     * Сохраняет обновлённые теги в объекте Authors для последующего объединения.
     *
     * @param originalTagMap исходный набор тегов (модифицируется в процессе работы).
     * @param authors объект, в который сохраняются обновлённые теги.
     */
    void fillAuthorsTags(TagMap originalTagMap, Authors authors);

    /**
     * Метод для извлечения индекса автора из тега.
     * Например, для "key_ria_authorX3" возвращает 2 (индексация с 0).
     */
    default int extractAuthorIndex(String tag) {
        String indexStr = tag.replaceAll("\\D+", ""); // Удаляем все нецифровые символы
        return Integer.parseInt(indexStr) - 1;
    }
}
