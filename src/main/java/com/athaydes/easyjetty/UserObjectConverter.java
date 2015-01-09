package com.athaydes.easyjetty;

import com.athaydes.easyjetty.http.MethodArbiter;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

final class UserObjectConverter {

    static Handler handlerFrom(final MethodArbiter methodArbiter, final Response response) {
        return new AbstractHandler() {
            @Override
            public void handle(String target, Request baseReq, HttpServletRequest req, HttpServletResponse res)
                    throws IOException, ServletException {
                if (res.isCommitted() || !methodArbiter.accepts(baseReq.getMethod())) {
                    return;
                }
                res.setContentType("text/html;charset=utf-8");
                res.setStatus(HttpServletResponse.SC_OK);
                baseReq.setHandled(true);
                response.respond(new Response.Exchange(res.getOutputStream(), req, res, baseReq));
            }
        };
    }

}
