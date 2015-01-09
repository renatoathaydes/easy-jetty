package com.athaydes.easyjetty;

import com.athaydes.easyjetty.http.MethodArbiter;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.Servlet;
import java.util.HashMap;
import java.util.Map;

import static com.athaydes.easyjetty.UserObjectConverter.handlerFrom;

/**
 * Easy-Jetty Server Builder.
 */
public class EasyJetty {

    private int port = 8080;
    private final Map<String, Class<? extends Servlet>> servlets = new HashMap<>(5);
    private final Map<String, Handler> handlers = new HashMap<>(5);
    private String contextPath = "/";
    private RequestLog requestLog;

    private volatile Server server;

    /**
     * Set the port to run the Jetty server on.
     *
     * @param port port
     * @return this
     */
    public EasyJetty port(int port) {
        errorIfServerStarted();
        this.port = port;
        return this;
    }

    /**
     * Set the context path for the server.
     *
     * @param path server context path
     * @return this
     */
    public EasyJetty contextPath(String path) {
        errorIfServerStarted();
        this.contextPath = path;
        return this;
    }

    /**
     * Add a servlet.
     *
     * @param path    request path
     * @param servlet the Servlet class which will handle requests to this path
     * @return this
     */
    public EasyJetty servlet(String path, Class<? extends Servlet> servlet) {
        servlets.put(path, servlet);
        return this;
    }

    /**
     * Add a handler for requests to the given path.
     *
     * @param methodArbiter arbiter for which methods should be accepted
     * @param path          request path
     * @param response      the response that may be computed for each request to this path
     * @return this
     */
    public EasyJetty on(MethodArbiter methodArbiter, String path, Response response) {
        handlers.put(path, handlerFrom(methodArbiter, response));
        return this;
    }

    /**
     * Set the requestLog
     *
     * @param requestLog a request log
     * @return this
     */
    public EasyJetty requestLog(RequestLog requestLog) {
        errorIfServerStarted();
        this.requestLog = requestLog;
        return this;
    }

    private void errorIfServerStarted() {
        final Server current = server;
        if (current != null && current.isStarted()) {
            throw new IllegalStateException("Server already started");
        }
    }

    public boolean isRunning() {
        final Server current = server;
        return current != null && current.isRunning();
    }

    /**
     * Start the server.
     *
     * @return this
     */
    public synchronized EasyJetty start() {
        if (server == null) {
            initializeServer();
        }
        if (!server.isStarted()) {
            try {
                server.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    public synchronized EasyJetty stop() {
        if (isRunning()) {
            try {
                server.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    private void initializeServer() {
        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletHandler.setContextPath(contextPath);
        servletHandler.setResourceBase(".");

        RequestLogHandler requestLogHandler = new RequestLogHandler();
        if (requestLog != null) {
            requestLogHandler.setRequestLog(requestLog);
        }

        servletHandler.addServlet(DefaultServlet.class, "/");

        for (Map.Entry<String, Class<? extends Servlet>> entry : servlets.entrySet()) {
            servletHandler.addServlet(entry.getValue(), entry.getKey());
        }

        HandlerCollection allHandler = new HandlerCollection();
        allHandler.addHandler(new TrieHandler(handlers));
        allHandler.addHandler(servletHandler);
        allHandler.addHandler(new DefaultHandler());

        server = new Server(port);
        server.setHandler(allHandler);
    }

}
