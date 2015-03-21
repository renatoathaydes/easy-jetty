package com.athaydes.easyjetty.mapper;

/**
 * A mapper from user-defined Objects to Strings
 * (which will be sent in responses from the server).
 */
public interface ObjectMapper<T> {

    /**
     * Turn the given Object into a String representing the body of a response.
     *
     * @param object
     * @return response body
     */
    String map(T object);

    /**
     * @return top super-type of the mapped Objects.
     */
    Class<? extends T> getMappedType();

}
