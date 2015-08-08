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
     * A content-type that includes everything
     */
    static final String ACCEPT_EVERYTHING = "*/*";

    /**
     * Turn the given Object into a String representing the body of a response.
     *
     * @param object to be mapped to a String
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
     * Turn the given String into an Object of type S, which is a sub-type of T.
     *
     * @param objectAsString to be transformed
     * @param type
     * @return Object of type S
     */
    <S extends T> S unmap(String objectAsString, Class<S> type);

    /**
     * @return top super-type of the mapped Objects.
     */
    Class<? extends T> getMappedType();

    /**
     * @return the content-type(s) that this ObjectMapper can handle. More than one
     * content-type may be provided comma-separated.
     */
    String getContentType();

}
