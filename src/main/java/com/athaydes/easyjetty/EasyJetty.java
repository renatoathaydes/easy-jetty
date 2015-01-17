package com.athaydes.easyjetty;

import com.athaydes.easyjetty.http.MethodArbiter;
import com.athaydes.easyjetty.mapper.ObjectMapper;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.Servlet;
import java.net.BindException;
import java.util.HashMap;
import java.util.Map;

import static com.athaydes.easyjetty.PathHelper.handlerPath;
import static com.athaydes.easyjetty.PathHelper.sanitize;
import static com.athaydes.easyjetty.UserHandlerCreator.handlerFrom;

/**
 * Easy-Jetty Server Builder.
 */
public class EasyJetty {

    private int port = 8080;
    private final Map<String, Class<? extends Servlet>> servlets = new HashMap<>(5);
    private final Map<HandlerPath, Handler> handlers = new HashMap<>(5);
    private final ObjectSender objectSender = new ObjectSender();
    private String contextPath = "/";
    private String resourcesLocation;
    private boolean allowDirectoryListing = true;
    private RequestLog requestLog;
    private String defaultContentType;

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
        this.contextPath = sanitize(path);
        return this;
    }

    /**
     * Set the resources location (ie. directory from which to serve static files).
     *
     * @param location of static files
     * @return this
     */
    public EasyJetty resourcesLocation(String location) {
        this.resourcesLocation = location;
        return this;
    }

    public EasyJetty disableDirectoryListing() {
        errorIfServerStarted();
        this.allowDirectoryListing = false;
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
        servlets.put(sanitize(path), servlet);
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
        HandlerPath handlerPath = handlerPath(path);
        handlers.put(handlerPath, handlerFrom(
                methodArbiter,
                response,
                handlerPath.getParametersByIndex(),
                defaultContentType,
                objectSender));
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

    public EasyJetty defaultContentType(String contentType) {
        this.defaultContentType = contentType;
        return this;
    }

    public EasyJetty addMapper(ObjectMapper<?> mapper) {
        objectSender.addMapper(mapper);
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
            } catch (BindException be) {
                System.out.println("Could not start the server! " + be.getMessage());
                try {
                    server.stop(); // kill the server Thread so the process can die
                } catch (Exception e) {
                    // ignore
                }
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
                server = null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    private void initializeServer() {
        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletHandler.setContextPath(contextPath);
        if (resourcesLocation != null) {
            servletHandler.setResourceBase(resourcesLocation);
            servletHandler.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed",
                    Boolean.toString(allowDirectoryListing));
        }

        RequestLogHandler requestLogHandler = new RequestLogHandler();
        if (requestLog != null) {
            requestLogHandler.setRequestLog(requestLog);
        }

        servletHandler.addServlet(DefaultServlet.class, "/");

        for (Map.Entry<String, Class<? extends Servlet>> entry : servlets.entrySet()) {
            servletHandler.addServlet(entry.getValue(), entry.getKey());
        }

        HandlerCollection allHandler = new HandlerCollection();
        allHandler.addHandler(new AggregateHandler(handlers));
        allHandler.addHandler(servletHandler);
        allHandler.addHandler(new DefaultHandler());

        server = new Server(port);
        server.setHandler(allHandler);
    }

}
