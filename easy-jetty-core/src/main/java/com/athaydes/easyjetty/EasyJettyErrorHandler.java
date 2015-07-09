package com.athaydes.easyjetty;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

class EasyJettyErrorHandler extends ErrorHandler {

    public static final String EASY_JETTY_ERROR = "_easy_jetty_error";

    private final Map<Integer, String> pathByStatusCode = new HashMap<>();
    private final EasyJetty easyJetty;

    public EasyJettyErrorHandler(EasyJetty easyJetty) {
        this.easyJetty = easyJetty;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (request.getAttribute(EASY_JETTY_ERROR) != null) {
            return;
        }

        request.setAttribute(EASY_JETTY_ERROR, true);
        String path = pathByStatusCode.get(response.getStatus());
        if (path == null) {
            return;
        }

        final AtomicBoolean wrappedResponseOk = new AtomicBoolean(false);

        HttpServletResponse wrappedResponse = new HttpServletResponseWrapper(response) {
            @Override
            public void setStatus(int sc) {
                wrappedResponseOk.set(sc == HttpStatus.OK_200);
            }
        };
        HandlerCollection allHandler = easyJetty.getAllHandler();

        for (Handler handler : allHandler.getHandlers()) {
            try {
                handler.handle(path, baseRequest, request, wrappedResponse);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (wrappedResponseOk.get()) {
                break;
            }
        }
    }

    void addErrorPage(int statusCode, String path) {
        addErrorPage(statusCode, statusCode, path);
    }

    void addErrorPage(int lowestStatusCode, int highestStatusCode, String path) {
        verifyStatusCodeRange(lowestStatusCode, highestStatusCode);
        for (int code = lowestStatusCode; code <= highestStatusCode; code++) {
            pathByStatusCode.put(code, path);
        }
    }

    private static void verifyStatusCodeRange(int low, int high) {
        verifyWithinValidStatusCodes(low);
        verifyWithinValidStatusCodes(high);
        if (low > high) {
            throw new RuntimeException("Low status code is larger than high status code");
        }
    }

    private static void verifyWithinValidStatusCodes(int statusCode) {
        if (statusCode > 599) {
            throw new RuntimeException("Invalid status code -> too large: " + statusCode);
        }
        if (statusCode < 100) {
            throw new RuntimeException("Invalid status code -> too low: " + statusCode);
        }
    }

}
