package com.athaydes.easyjetty;

import com.athaydes.easyjetty.http.MethodArbiter;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

final class UserHandler extends AbstractHandler implements EasyJettyHandler {

    private final MethodArbiter methodArbiter;
    private final Responder responder;
    private final Map<Integer, String> paramsByIndex;
    private final String defaultContentType;
    private final ObjectSender objectSender;

    public UserHandler(MethodArbiter methodArbiter,
                       Responder responder,
                       Map<Integer, String> paramsByIndex,
                       String defaultContentType,
                       ObjectSender objectSender) {
        this.methodArbiter = methodArbiter;
        this.responder = responder;
        this.paramsByIndex = paramsByIndex;
        this.defaultContentType = defaultContentType;
        this.objectSender = objectSender;
    }

    @Override
    public void handle(String target, Request baseReq, HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        if (res.isCommitted()) {
            return;
        }
        if (defaultContentType != null) {
            res.setContentType(defaultContentType);
        }
        res.setStatus(HttpServletResponse.SC_OK);
        baseReq.setHandled(true);
        Map<String, String> params = PathHelper.matchParams(paramsByIndex, baseReq.getPathInfo());
        responder.respond(new Responder.Exchange(res.getOutputStream(), req, res, baseReq, params, objectSender));
    }

    @Override
    public MethodArbiter getMethodArbiter() {
        return methodArbiter;
    }

}