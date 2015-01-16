package com.athaydes.easyjetty;

import org.eclipse.jetty.server.Request;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * An user-provided Response to a Request.
 */
public abstract class Response {

    public static class Exchange {
        public final ServletOutputStream out;
        public final HttpServletRequest request;
        public final HttpServletResponse response;
        public final Request baseRequest;
        public final Map<String, String> params;

        Exchange(ServletOutputStream out, HttpServletRequest request, HttpServletResponse response,
                 Request baseRequest, Map<String, String> parameters) {
            this.out = out;
            this.request = request;
            this.response = response;
            this.baseRequest = baseRequest;
            this.params = parameters;
        }
    }

    /**
     * Implement this method to respond to a request.
     * @param exchange useful resources for an exchange (request/response)
     * @throws IOException in case the stream is closed or there's a problem accessing some resource
     */
    abstract public void respond(Exchange exchange) throws IOException;

}
