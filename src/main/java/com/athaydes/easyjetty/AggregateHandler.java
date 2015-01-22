package com.athaydes.easyjetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandlerContainer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.athaydes.easyjetty.PathHelper.handlerPath;

class AggregateHandler extends AbstractHandlerContainer {

    private final PathTree<EasyJettyHandler> handlers = new PathTree<>();

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        List<EasyJettyHandler> availableHandlers = handlers.get(handlerPath(target));
        if (!availableHandlers.isEmpty()) {
            for (EasyJettyHandler handler : availableHandlers) {
                if (handler.getMethodArbiter().accepts(baseRequest.getMethod())) {
                    handler.handle(target, baseRequest, request, response);
                    if (baseRequest.isHandled()) {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public Handler[] getHandlers() {
        return handlers.values().toArray(new Handler[handlers.size()]);
    }

    public void add(HandlerPath handlerPath, UserHandler handler) {
        handlers.put(handlerPath, handler);
    }

    public void clear() {
        handlers.clear();
    }
}
