package org.example.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Денис on 22.05.2024
 */
public class Authors {
    private List<TagMap> tagAuthors;

    public Authors(int countAuthors) {
        tagAuthors = new ArrayList<>();
        for (int i = 0; i < countAuthors; i++){
            tagAuthors.add(new TagMap());
        }
    }

    // Метод для получения тегов первого автора (основной TagMap)
    public TagMap getMainTagMap() {
        if (tagAuthors == null || tagAuthors.isEmpty()) {
            return new TagMap();
        }
        return tagAuthors.get(0);
    }

    // Метод для получения тегов автора по индексу
    public TagMap getTagMapByIndex(int index) {
        if (tagAuthors == null || tagAuthors.size() <= index) {
            return new TagMap();
        }
        return tagAuthors.get(index);
    }

    public List<TagMap> getTagMaps() {
        return tagAuthors;
    }

    public void addTagToAuthor(int authorIndex, String tag, String value) {
        if (authorIndex >= 0 && authorIndex < tagAuthors.size()) {
            tagAuthors.get(authorIndex).addTag(tag, value);
        }
    }
}
