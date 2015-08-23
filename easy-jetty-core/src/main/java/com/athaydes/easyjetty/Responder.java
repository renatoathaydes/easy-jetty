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

    class Exchange extends ExchangeBase {
        public final Request baseRequest;
        public final String acceptedContentType;

        Exchange(ServletOutputStream out, HttpServletRequest request, HttpServletResponse response,
                 Request baseRequest, Map<String, String> parameters, ObjectSupport objectSupport, String acceptedContentType) {
            super(out, request, response,  parameters, objectSupport);
            this.baseRequest = baseRequest;
            this.acceptedContentType = acceptedContentType;
        }

    }

    /**
     * Implement this method to respond to a request.
     *
     * @param exchange useful resources for an exchange (request/response)
     * @throws IOException in case the stream is closed or there's a problem accessing some resource
     */
    void respond(Exchange exchange) throws IOException;

}
