package com.athaydes.easyjetty;

import org.eclipse.jetty.server.Request;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * An user-provided Responder to a Request.
 */
public interface Responder {

    public static class Exchange {
        public final ServletOutputStream out;
        public final HttpServletRequest request;
        public final HttpServletResponse response;
        public final Request baseRequest;
        public final Map<String, String> params;
        public final String acceptedContentType;
        private final ObjectSupport objectSupport;

        Exchange(ServletOutputStream out, HttpServletRequest request, HttpServletResponse response,
                 Request baseRequest, Map<String, String> parameters, ObjectSupport objectSupport, String acceptedContentType) {
            this.out = out;
            this.request = request;
            this.response = response;
            this.baseRequest = baseRequest;
            this.params = parameters;
            this.objectSupport = objectSupport;
            this.acceptedContentType = acceptedContentType;
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
    }

    /**
     * Implement this method to respond to a request.
     *
     * @param exchange useful resources for an exchange (request/response)
     * @throws IOException in case the stream is closed or there's a problem accessing some resource
     */
    abstract public void respond(Exchange exchange) throws IOException;

}
