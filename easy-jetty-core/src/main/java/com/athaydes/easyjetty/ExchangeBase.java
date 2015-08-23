package com.athaydes.easyjetty;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public abstract class ExchangeBase {

    public final ServletOutputStream out;
    public final HttpServletRequest request;
    public final HttpServletResponse response;
    public final Map<String, String> params;
    private final ObjectSupport objectSupport;

    ExchangeBase(ServletOutputStream out, HttpServletRequest request,
                 HttpServletResponse response,
                 Map<String, String> params,
                 ObjectSupport objectSupport) {
        this.out = out;
        this.request = request;
        this.response = response;
        this.params = params;
        this.objectSupport = objectSupport;
    }

    /**
     * Sends the given object as the response content.
     * <p/>
     * If object is null, a 404 response is sent instead.
     *
     * @param object to send back, or null if 404 (Not found)
     * @throws java.lang.RuntimeException if no ObjectMapper can be found for the given object.
     * @throws IOException                if a problem occurs while writing the data.
     */
    public void send(Object object) throws IOException {
        if (object == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        objectSupport.send(object, response);
    }

    /**
     * Receives the request content and map it to an instance of the given type.
     *
     * @param type to map the request content to.
     * @param <T>  type
     * @return instance of T, mapped from the request content
     * @throws java.lang.IllegalArgumentException if the request's content length is too big.
     * @throws java.lang.RuntimeException         if an IOException occurs while reading the request data
     *                                            or no ObjectMapper can be found for the given type.
     */
    public <T> T receiveAs(Class<T> type) {
        try {
            return objectSupport.receive(request, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Receives the request content and map it to a List of instances of the given type.
     *
     * @param type to map the request content to.
     * @param <T>  type
     * @return instance of T, mapped from the request content
     * @throws java.lang.IllegalArgumentException if the request's content length is too big.
     * @throws java.lang.RuntimeException         if an IOException occurs while reading the request data
     *                                            or no ObjectMapper can be found for the given type.
     */
    public <T> List<T> receiveAll(Class<T> type) {
        try {
            return new ArrayList<>(objectSupport.receiveAll(request, type));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
