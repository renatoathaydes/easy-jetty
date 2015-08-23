package com.athaydes.easyjetty;

import com.athaydes.easyjetty.http.MethodArbiter;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandlerContainer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static com.athaydes.easyjetty.Filter.FilterAdapter.filterAsHandler;
import static com.athaydes.easyjetty.PathHelper.handlerPath;

class AggregateHandler extends AbstractHandlerContainer {

    private final EasyJetty easyJetty;
    private final PathTree<EasyJettyHandler> handlers = new PathTree<>();

    public AggregateHandler(EasyJetty easyJetty) {
        this.easyJetty = easyJetty;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        List<EasyJettyHandler> availableHandlers = handlers.get(handlerPath(target));
        if (!availableHandlers.isEmpty()) {
            for (EasyJettyHandler handler : availableHandlers) {
                if (handler.getMethodArbiter().accepts(baseRequest.getMethod())) {
                    try {
                        handler.handle(target, baseRequest, request, response);
                    } catch (Exception e) {
                        // necessary to set the context so that the ErrorHandler can handle this request
                        baseRequest.setContext(easyJetty.getServletContext());
                        throw e;
                    }
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

    public void addFilter(HandlerPath handlerPath, Filter filter, ObjectSupport objectSupport) {
        handlers.putFirst(handlerPath, filterAsHandler(filter,
                handlerPath.getParametersByIndex(), objectSupport));
    }

    public void clear() {
        handlers.clear();
    }

    public boolean remove(MethodArbiter methodArbiter, HandlerPath handlerPath) {
        List<EasyJettyHandler> userHandlers = handlers.get(handlerPath);
        boolean result = false;
        for (Iterator<EasyJettyHandler> iter = userHandlers.iterator(); iter.hasNext(); ) {
            EasyJettyHandler handler = iter.next();
            if (handler.getMethodArbiter().accepts(methodArbiter.toString())) {
                iter.remove();
                result = true;
            }
        }
        return result;
    }
}
