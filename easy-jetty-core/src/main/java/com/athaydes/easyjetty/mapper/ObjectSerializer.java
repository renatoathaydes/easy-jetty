package com.athaydes.easyjetty.mapper;

/**
 * Simple ObjectMapper which can serialize Objects into Strings.
 * <p/>
 * The default implementation of <code>unmap(..)</code> will throw an
 * <code>UnsupportedOperationException</code>.
 */
public abstract class ObjectSerializer<T> implements ObjectMapper<T> {

    @Override
    public T unmap(String objectAsString) {
        throw new UnsupportedOperationException("unmap");
    }

    @Override
    public String getContentType() {
        return ACCEPT_EVERYTHING;
    }

}
