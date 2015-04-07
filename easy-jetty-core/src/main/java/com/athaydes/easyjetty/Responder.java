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
        private final ObjectSupport sender;

        Exchange(ServletOutputStream out, HttpServletRequest request, HttpServletResponse response,
                 Request baseRequest, Map<String, String> parameters, ObjectSupport objectSupport, String acceptedContentType) {
            this.out = out;
            this.request = request;
            this.response = response;
            this.baseRequest = baseRequest;
            this.params = parameters;
            this.sender = objectSupport;
            this.acceptedContentType = acceptedContentType;
        }

        public void send(Object object) throws IOException {
            sender.send(object, response);
        }

        public <T> T receiveAs(Class<T> type) {
            try {
                return sender.receive(request, type);
            } catch (RuntimeException rte) {
                throw rte;
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
