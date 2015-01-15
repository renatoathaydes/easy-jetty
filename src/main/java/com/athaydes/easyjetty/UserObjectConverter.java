package com.athaydes.easyjetty;

import com.athaydes.easyjetty.http.MethodArbiter;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

final class UserObjectConverter {

    static Handler handlerFrom(final MethodArbiter methodArbiter, final Response response,
                               final Map<Integer, String> paramsByIndex) {
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
                Map<String, String> params = PathSanitizer.matchParams(paramsByIndex, baseReq.getPathInfo());
                response.respond(new Response.Exchange(res.getOutputStream(), req, res, baseReq, params));
            }
        };
    }

}
