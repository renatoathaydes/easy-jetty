package com.athaydes.easyjetty.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Simple CollectionMapper implementation based on parameters.
 */
public class CollectionMapperParams extends CollectionMapper {

    final String contentType;
    final String separator;
    final String opener;
    final String ender;

    public CollectionMapperParams(String contentType, String separator, String opener, String ender) {
        this.contentType = contentType;
        this.separator = separator;
        this.opener = opener;
        this.ender = ender;
    }

    @Override
    public String map(Collection collection) {
        StringBuilder sb = new StringBuilder();
        sb.append(opener);
        boolean firstItem = true;
        for (Object item : collection) {
            if (!firstItem) {
                sb.append(separator);
            }
            firstItem = false;
            sb.append(mapperGroup.map(item, contentType));
        }
        sb.append(ender);
        return sb.toString();
    }

    @Override
    public Collection unmap(String objectAsString) {
        return unmapAll(objectAsString, Object.class);
    }

    @Override
    public <T> Collection<T> unmapAll(String objectAsString, Class<T> type) {
        String trimmedInput = objectAsString.trim();
        if (!objectAsString.startsWith(opener)) {
            throw new IllegalArgumentException("Input does not start with " + opener);
        }
        if (!objectAsString.endsWith(ender)) {
            throw new IllegalArgumentException("Input does not end with " + ender);
        }
        String[] items = trimmedInput.substring(opener.length(), trimmedInput.length() - ender.length())
                .split(Pattern.compile(separator).pattern());
        Collection<T> unmappedItems = new ArrayList<>();
        for (String item : items) {
            T unmappedItem = mapperGroup.unmap(item, type, getContentType());
            unmappedItems.add(unmappedItem);
        }
        return unmappedItems;
    }

    @Override
    public String getContentType() {
        return contentType;
    }
}
