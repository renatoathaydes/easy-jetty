package com.athaydes.easyjetty;

import com.athaydes.easyjetty.external.MIMEParse;
import com.athaydes.easyjetty.http.MethodArbiter;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.athaydes.easyjetty.mapper.ObjectMapper.ACCEPT_EVERYTHING;

final class UserHandler extends AbstractHandler implements EasyJettyHandler {

    private final MethodArbiter methodArbiter;
    private final Responder responder;
    private final Map<Integer, String> paramsByIndex;
    private final String defaultContentType;
    private final ObjectSupport objectSupport;
    private final List<String> acceptedContentTypes;
    private final boolean acceptEverything;

    public UserHandler(MethodArbiter methodArbiter,
                       String acceptedContentType,
                       Responder responder,
                       Map<Integer, String> paramsByIndex,
                       String defaultContentType,
                       ObjectSupport objectSupport) {
        this.methodArbiter = methodArbiter;
        this.acceptEverything = acceptedContentType.equals(ACCEPT_EVERYTHING);
        this.responder = responder;
        this.acceptedContentTypes = acceptEverything ? null : parseAcceptedContentTypes(acceptedContentType);
        this.paramsByIndex = paramsByIndex;
        this.defaultContentType = defaultContentType;
        this.objectSupport = objectSupport;
    }

    protected static List<String> parseAcceptedContentTypes(String accepted) {
        if (accepted.contains("*")) {
            throw new RuntimeException("Invalid contentType (must not contain wildcard)");
        }
        List<String> result = new ArrayList<>(2);
        String[] parts = accepted.split(",");
        for (String part : parts) {
            if (part.startsWith("/") || part.endsWith("/")) {
                throw new RuntimeException("Invalid contentType (must not start/end with '/'): " + accepted);
            }
            String[] subParts = part.split("/");
            if (subParts.length != 2) {
                throw new RuntimeException("Invalid contentType (does not contain 'type/subtype'): " + accepted);
            }
            result.add(part.trim());
        }
        return result;
    }

    @Override
    public void handle(String target, Request baseReq, HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        if (res.isCommitted()) {
            return;
        }

        final String acceptedContentType = getAcceptedContentType(req.getHeader(HttpHeader.ACCEPT.asString()));
        if (acceptedContentType.isEmpty()) {
            return;
        }

        if (!ACCEPT_EVERYTHING.equals(acceptedContentType)) {
            res.setHeader(HttpHeader.CONTENT_TYPE.asString(), acceptedContentType);
        } else if (defaultContentType != null) {
            res.setContentType(defaultContentType);
        }

        res.setStatus(HttpServletResponse.SC_OK);
        baseReq.setHandled(true);
        Map<String, String> params = PathHelper.matchParams(paramsByIndex, baseReq.getPathInfo());
        responder.respond(new Responder.Exchange(res.getOutputStream(), req, res, baseReq, params, objectSupport, acceptedContentType));
    }

    @Override
    public MethodArbiter getMethodArbiter() {
        return methodArbiter;
    }

    private String getAcceptedContentType(String acceptHeader) {
        return acceptEverything || (acceptHeader == null) ?
                ACCEPT_EVERYTHING :
                MIMEParse.bestMatch(acceptedContentTypes, acceptHeader);
    }

}