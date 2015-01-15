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

    abstract public void respond(Exchange exchange) throws IOException;

}
