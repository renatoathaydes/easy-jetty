package com.athaydes.easyjetty.mapper;

/**
 * Simple ObjectMapper which can only serialize Objects into Strings.
 * <p/>
 * It will throw an <code>UnsupportedOperationException</code> if the
 * <code>unmap</code> method is called.
 */
public abstract class ObjectSerializer<T> implements ObjectMapper<T> {

    @Override
    public T unmap(String objectAsString) {
        throw new UnsupportedOperationException("unmap");
    }

}
