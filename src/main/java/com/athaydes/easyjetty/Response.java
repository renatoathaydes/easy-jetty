package com.athaydes.easyjetty;

import org.eclipse.jetty.server.Request;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * An user-provided Response to a Request.
 */
public abstract class Response {

    public static class Exchange {
        public final ServletOutputStream out;
        public final HttpServletRequest request;
        public final HttpServletResponse response;
        public final Request baseRequest;

        public Exchange(ServletOutputStream out, HttpServletRequest request, HttpServletResponse response, Request baseRequest) {
            this.out = out;
            this.request = request;
            this.response = response;
            this.baseRequest = baseRequest;
        }
    }

    abstract public void respond(Exchange exchange) throws IOException;

}
