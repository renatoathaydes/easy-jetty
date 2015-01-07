package com.athaydes.easyjetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandlerContainer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

class TrieHandler extends AbstractHandlerContainer {

    private final Map<String, Handler> handlers;

    public TrieHandler(Map<String, Handler> handlers) {
        this.handlers = handlers;
    }


    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Handler handler = handlers.get(target);
        if (handler != null) {
            handler.handle(target, baseRequest, request, response);
        }
    }

    @Override
    public Handler[] getHandlers() {
        return handlers.values().toArray(new Handler[handlers.size()]);
    }
}
