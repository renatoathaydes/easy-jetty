package com.athaydes.easyjetty;

import com.athaydes.easyjetty.http.MethodArbiter;
import com.athaydes.easyjetty.mapper.ObjectMapper;
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

/**
 * Easy-Jetty Server Builder.
 */
public class EasyJetty {

    private final CanChangeWhenServerNotRunningProperties notRunningProperties = new CanChangeWhenServerNotRunningProperties();

    private final Map<String, Class<? extends Servlet>> servlets = new HashMap<>(5);
    private final AggregateHandler aggregateHandler = new AggregateHandler();
    private final ObjectSender objectSender = new ObjectSender();

    private volatile String defaultContentType;
    private volatile Server server;

    public EasyJetty() {
        restoreDefaults();
    }

    private void restoreDefaults() {
        notRunningProperties.setPort(8080, server);
        notRunningProperties.setContextPath("/", server);
        notRunningProperties.setAllowDirectoryListing(true, server);
        notRunningProperties.setRequestLog(null, server);
        notRunningProperties.setResourcesLocation(null, server);
        defaultContentType = null;
    }

    /**
     * Set the port to run the Jetty server on.
     *
     * @param port port
     * @return this
     */
    public EasyJetty port(int port) {
        notRunningProperties.setPort(port, server);
        return this;
    }

    /**
     * Set the context path for the server.
     *
     * @param path server context path
     * @return this
     */
    public EasyJetty contextPath(String path) {
        notRunningProperties.setContextPath(sanitize(path), server);
        return this;
    }

    /**
     * Set the resources location (ie. directory from which to serve static files).
     *
     * @param location of static files
     * @return this
     */
    public EasyJetty resourcesLocation(String location) {
        notRunningProperties.setResourcesLocation(location, server);
        return this;
    }

    public EasyJetty disableDirectoryListing() {
        notRunningProperties.setAllowDirectoryListing(false, server);
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
     * Add a handler for requests to the given path that accepts the given contentType.
     *
     * @param methodArbiter arbiter for which methods should be accepted
     * @param path          request path
     * @param contentType   accepted content type
     * @param responder     the response that may be computed for each request to this path
     * @return this
     */
    public EasyJetty on(MethodArbiter methodArbiter, String path, String contentType, Responder responder) {
        HandlerPath handlerPath = handlerPath(path);
        aggregateHandler.add(handlerPath, new UserHandler(
                methodArbiter,
                contentType,
                responder,
                handlerPath.getParametersByIndex(),
                defaultContentType,
                objectSender));
        return this;
    }

    /**
     * Add a handler for requests to the given path.
     *
     * @param methodArbiter arbiter for which methods should be accepted
     * @param path          request path
     * @param responder     the response that may be computed for each request to this path
     * @return this
     */
    public EasyJetty on(MethodArbiter methodArbiter, String path, Responder responder) {
        return on(methodArbiter, path, UserHandler.ACCEPT_EVERYTHING, responder);
    }

    /**
     * Remove a handler that accepts the given methodArbiter for the given path.
     * <p/>
     * Notice that any handlers which accept the given MethodArbiter will be removed, which means
     * that more than one handler may be removed, and that aggregate MethodArbiters such as
     * `anyMethod()` can be removed using any MethodArbiter.
     *
     * @param methodArbiter methodArbiter
     * @param path          path
     * @return true if any handler is removed, false otherwise
     */
    public boolean remove(MethodArbiter methodArbiter, String path) {
        HandlerPath handlerPath = handlerPath(path);
        return aggregateHandler.remove(methodArbiter, handlerPath);
    }

    /**
     * Set the requestLog
     *
     * @param requestLog a request log
     * @return this
     */
    public EasyJetty requestLog(RequestLog requestLog) {
        notRunningProperties.setRequestLog(requestLog, server);
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

    /**
     * Stop the server.
     * <p/>
     * To stop the server and clear all configuration, removing all handlers
     * and servlets, use {@code stop(true)}.
     *
     * @return this
     */
    public synchronized EasyJetty stop() {
        return stop(false);
    }

    /**
     * Stop the server.
     *
     * @param clearConfig true to clear all configuration, removing all handlers
     *                    and servlets.
     * @return this
     */
    public synchronized EasyJetty stop(boolean clearConfig) {
        if (isRunning()) {
            try {
                server.stop();
                server = null;
                if (clearConfig) {
                    servlets.clear();
                    aggregateHandler.clear();
                    objectSender.clear();
                    restoreDefaults();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    private void initializeServer() {
        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletHandler.setContextPath(notRunningProperties.getContextPath());
        String resourcesLocation = notRunningProperties.getResourcesLocation();
        if (resourcesLocation != null) {
            servletHandler.setResourceBase(resourcesLocation);
            servletHandler.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed",
                    Boolean.toString(notRunningProperties.isAllowDirectoryListing()));
        }

        RequestLogHandler requestLogHandler = new RequestLogHandler();
        RequestLog requestLog = notRunningProperties.getRequestLog();
        if (requestLog != null) {
            requestLogHandler.setRequestLog(requestLog);
        }

        servletHandler.addServlet(DefaultServlet.class, "/");

        for (Map.Entry<String, Class<? extends Servlet>> entry : servlets.entrySet()) {
            servletHandler.addServlet(entry.getValue(), entry.getKey());
        }

        HandlerCollection allHandler = new HandlerCollection();
        allHandler.addHandler(aggregateHandler);
        allHandler.addHandler(servletHandler);
        allHandler.addHandler(new DefaultHandler());

        server = new Server(notRunningProperties.getPort());
        server.setHandler(allHandler);
    }

}
