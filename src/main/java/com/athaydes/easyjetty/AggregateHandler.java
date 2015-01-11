package com.athaydes.easyjetty;

import com.athaydes.easyjetty.PathTree.PathTreeValue;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandlerContainer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static com.athaydes.easyjetty.PathSanitizer.handlerPath;

class AggregateHandler extends AbstractHandlerContainer {

    private final PathTree<Handler> handlers;

    public AggregateHandler(Map<HandlerPath, Handler> handlers) {
        this.handlers = new PathTree<>(handlers);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        PathTreeValue<Handler> handler = handlers.get(handlerPath(target));
        if (handler != PathTreeValue.NULL_VALUE) {
            handler.value.handle(target, baseRequest, request, response);
        }
    }

    @Override
    public Handler[] getHandlers() {
        return handlers.values().toArray(new Handler[handlers.size()]);
    }
}
