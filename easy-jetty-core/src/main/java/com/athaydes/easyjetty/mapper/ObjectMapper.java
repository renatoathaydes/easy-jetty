package com.athaydes.easyjetty.mapper;

/**
 * A mapper from user-defined Objects to Strings and vice-versa.
 * <p/>
 * This can be used to serialize an Object to a String and send that in a response,
 * and to read a request content as an Object.
 *
 * @see com.athaydes.easyjetty.mapper.ObjectSerializer
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
     * Turn the given String into an Object of type T.
     *
     * @param objectAsString to be transformed
     * @return Object of type T
     */
    T unmap(String objectAsString);

    /**
     * @return top super-type of the mapped Objects.
     */
    Class<? extends T> getMappedType();

}
