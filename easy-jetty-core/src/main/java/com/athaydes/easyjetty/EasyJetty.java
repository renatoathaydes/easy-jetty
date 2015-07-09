package com.athaydes.easyjetty;

import com.athaydes.easyjetty.extension.EasyJettyEvent;
import com.athaydes.easyjetty.extension.EasyJettyExtension;
import com.athaydes.easyjetty.extension.event.*;
import com.athaydes.easyjetty.http.MethodArbiter;
import com.athaydes.easyjetty.mapper.ObjectMapperGroup;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.Servlet;
import java.net.BindException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.athaydes.easyjetty.PathHelper.handlerPath;
import static com.athaydes.easyjetty.PathHelper.sanitize;
import static com.athaydes.easyjetty.mapper.ObjectMapper.ACCEPT_EVERYTHING;

/**
 * Easy-Jetty Server Builder.
 */
public class EasyJetty {

    private final CanChangeWhenServerNotRunningProperties notRunningProperties = new CanChangeWhenServerNotRunningProperties();

    private final Map<String, Object> servlets = new HashMap<>(5);
    private final AggregateHandler aggregateHandler = new AggregateHandler(this);
    private final ObjectSupport objectSupport = new ObjectSupport(this);
    private final List<EasyJettyExtension> extensions = new ArrayList<>(2);

    private volatile HandlerCollection allHandler;
    private volatile EasyJettyErrorHandler errorHandler;
    private volatile String defaultContentType;
    private volatile String defaultAccept;
    private volatile Server server;
    private volatile ServletContextHandler servletHandler;
    private volatile boolean sslOnly;
    private volatile int maxFormSize;

    /**
     * Build an EasyJetty instance using the default values for all settings.
     * <p/>
     * You can configure the server by calling methods using EasyJetty's fluent API.
     * For example:
     * <p/>
     * <code>
     * new EasyJetty()<br>
     * .on(GET, "hello", ex -> ex.send("Hello EasyJetty!"))<br>
     * .resourcesLocation("public/")<br>
     * .disableDirectoryListing()<br>
     * .start();
     * </code>
     * <p/>
     * Notice the last call to <code>start()</code> is necessary to actually start the server.
     * You can stop the server at any time by calling <code>stop()</code>.
     * <p/>
     * Many of the settings can only be set while the server is NOT running.
     * However, you can add/remove handlers without stopping the server.
     */
    public EasyJetty() {
        restoreDefaults();
    }

    private void restoreDefaults() {
        sslOnly = false;
        notRunningProperties.setPort(8080, server);
        notRunningProperties.setContextPath("/", server);
        notRunningProperties.setAllowDirectoryListing(true, server);
        notRunningProperties.setRequestLog(null, server);
        notRunningProperties.setResourcesLocation(null, server);
        notRunningProperties.setVirtualHosts(server);
        allHandler = new HandlerCollection();
        defaultContentType = null;
        defaultAccept = null;
        errorHandler = null;
        maxFormSize = -1;
    }

    /**
     * Add the given EasyJetty extension to this instance.
     *
     * @param extension to add
     * @return this
     */
    public EasyJetty withExtension(EasyJettyExtension extension) {
        extensions.add(extension);
        fireEvent(new ExtensionAddedEvent(this, extension));
        return this;
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
     * Set the virtual hosts for this Server.
     *
     * @param virtualHosts virtual host names
     * @return this
     */
    public EasyJetty withVirtualHosts(String... virtualHosts) {
        notRunningProperties.setVirtualHosts(server, virtualHosts);
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

    /**
     * Disables directory listing for static resources.
     * <p/>
     * Ignored if a resourcesLocation is not provided.
     *
     * @return this
     */
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
     * Add a servlet.
     *
     * @param path    request path
     * @param servlet the Servlet instance which will handle requests to this path
     * @return this
     */
    public EasyJetty servlet(String path, Servlet servlet) {
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
                objectSupport));
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
        return on(methodArbiter, path, defaultAccept != null ? defaultAccept : ACCEPT_EVERYTHING, responder);
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

    EasyJettyErrorHandler getErrorHandler(boolean createIfNull) {
        synchronized (this) {
            if (errorHandler == null && createIfNull) {
                errorHandler = new EasyJettyErrorHandler(this);
            }
        }
        return errorHandler;
    }

    /**
     * Path for the resource to return in case of an error with the given statusCode.
     *
     * @param statusCode error status code
     * @param path       of resource to be provided
     * @return this
     */
    public EasyJetty errorPage(int statusCode, String path) {
        getErrorHandler(true).addErrorPage(statusCode, PathHelper.sanitize(path));
        return this;
    }

    /**
     * Path for the resource to return in case of an error code within the given range.
     *
     * @param lowestStatusCode  lowest error status code
     * @param highestStatusCode highest error status code
     * @param path              of resource to be provided
     * @return this
     */
    public EasyJetty errorPage(int lowestStatusCode, int highestStatusCode, String path) {
        getErrorHandler(true).addErrorPage(lowestStatusCode, highestStatusCode, PathHelper.sanitize(path));
        return this;
    }

    /**
     * Set the default content type to be used in responses provided by the Server.
     * If the request handler does not set the response content-type, this value will be used.
     *
     * @param contentType the default content type to use.
     * @return this
     */
    public EasyJetty defaultContentType(String contentType) {
        this.defaultContentType = contentType;
        return this;
    }

    /**
     * Set the default content type to be accepted by handlers when receiving requests.
     * <p/>
     * If not set, the Accept header of incoming requests will be ignored by any
     * handlers that were declared without a content type.
     *
     * @param contentType to accept by default
     * @return this
     * @see com.athaydes.easyjetty.EasyJetty#on(MethodArbiter, String, String, Responder)
     */
    public EasyJetty defaultAccept(String contentType) {
        this.defaultAccept = contentType;
        return this;
    }

    /**
     * Set the mapperGroup to be used in (un)-marshalling Objects to/from Strings received
     * and sent by the Server.
     *
     * @param mapperGroup the mapper group to use
     * @return this
     */
    public EasyJetty withMapperGroup(ObjectMapperGroup mapperGroup) {
        objectSupport.setMapperGroup(mapperGroup);
        return this;
    }

    /**
     * Provide the SSL configuration to enable SSL and serve traffic over HTTPS.
     * <p/>
     * Notice that the server will still accept HTTP connections! If you do not want that,
     * use {@link com.athaydes.easyjetty.EasyJetty#sslOnly(SSLConfig)} instead.
     *
     * @param config SSL configuration
     * @return this
     */
    public EasyJetty ssl(SSLConfig config) {
        notRunningProperties.setSsl(config, server);
        return this;
    }

    /**
     * Provide the SSL configuration to enable SSL and serve traffic over HTTPS.
     * <p/>
     * Instructs EasyJetty to use only SSL with HTTPS, ie. it will NOT server any traffic over HTTP.
     *
     * @param config SSL configuration
     * @return this
     */
    public EasyJetty sslOnly(SSLConfig config) {
        sslOnly = true;
        return ssl(config);
    }

    /**
     * Maximum form size allowable. This uses the same mechanism as Jetty does for Servlets,
     * but for user-provided Responders, you should use the {@link com.athaydes.easyjetty.Responder.Exchange#receiveAs}
     * method to read Objects from a request in order for this setting to have effect.
     *
     * @param maxBytes maximum amount of bytes a form, or request body, may have
     * @return this
     */
    public EasyJetty maxFormSize(int maxBytes) {
        if (maxBytes < 0) {
            throw new IllegalArgumentException("MaxBytes must  be 0 or larger");
        }
        this.maxFormSize = maxBytes;
        return this;
    }

    public ObjectMapperGroup getObjectMapperGroup() {
        return objectSupport.getObjectMapperGroup();
    }

    int getMaxFormSize() {
        return maxFormSize < 0 ? Integer.MAX_VALUE : maxFormSize;
    }

    public boolean isRunning() {
        final Server current = server;
        return current != null && current.isRunning();
    }

    /**
     * Start the server.
     * <p/>
     * If the server is already running, this call will be ignored.
     *
     * @return this
     */
    public synchronized EasyJetty start() {
        if (server == null) {
            initializeServer();
        }
        if (!server.isStarted()) {
            ErrorEvent errorEvent = null;
            try {
                fireEvent(new BeforeStartEvent(this));
                server.start();
                fireEvent(new AfterStartEvent(this));
            } catch (BindException be) {
                errorEvent = new ErrorEvent(this, "Port " + notRunningProperties.getPort() + " is already taken", be);
            } catch (Exception e) {
                errorEvent = new ErrorEvent(this, e);
            }
            if (errorEvent != null) {
                fireEvent(errorEvent);
                try {
                    server.stop(); // kill the server Thread so the process can die
                } catch (Exception e) {
                    // ignore
                }
                throw new RuntimeException(errorEvent.getThrowable());
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
                fireEvent(new BeforeStopEvent(this));
                server.stop();
                fireEvent(new AfterStopEvent(this));
                server = null;
                servletHandler = null;
                if (clearConfig) {
                    servlets.clear();
                    aggregateHandler.clear();
                    objectSupport.clear();
                    restoreDefaults();
                }
            } catch (Exception e) {
                fireEvent(new ErrorEvent(this, e));
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Tried to stop but server not running");
        }
        return this;
    }

    /**
     * @return the Jetty server if it is running, null otherwise.
     * <p/>
     * A new server instance is created every time the server is started.
     * <p/>
     * The Server is provided only to enable users to use features which are not
     * yet exposed in EasyJetty. However, most users should avoid using the Server directly.
     * One reason, as an example, is that if the Server is started or stopped directly (rather
     * than by calling the EasyJetty start/stop methods), EasyJettyExtensions will not have their
     * callbacks run and may enter an inconsistent state.
     * Another reason is that the Server instance changes on server re-start.
     */
    public Server getServer() {
        return server;
    }

    /**
     * @return the current ServletContextHandler if running, null otherwise.
     */
    public ServletContextHandler getServletHandler() {
        return servletHandler;
    }

    HandlerCollection getAllHandler() {
        return allHandler;
    }

    ContextHandler.Context getServletContext() {
        return servletHandler.getServletContext();
    }

    private void fireEvent(EasyJettyEvent event) {
        for (EasyJettyExtension extension : extensions) {
            try {
                extension.handleEvent(event);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private void initializeServer() {
        initializeServletHandler();
        initializeRequestLogHandler();
        configHandlers();

        server = new Server(notRunningProperties.getPort());
        server.setHandler(allHandler);

        initializeErrorHandler();
        initializeSSL();
        initializeServerAttributes();
    }

    private void configHandlers() {
        allHandler.addHandler(aggregateHandler);
        allHandler.addHandler(servletHandler);
        allHandler.addHandler(new DefaultHandler());
    }

    private void initializeRequestLogHandler() {
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        RequestLog requestLog = notRunningProperties.getRequestLog();
        if (requestLog != null) {
            requestLogHandler.setRequestLog(requestLog);
        }
    }

    private void initializeServletHandler() {
        servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletHandler.setContextPath(notRunningProperties.getContextPath());
        if (notRunningProperties.getVirtualHosts().length > 0) {
            servletHandler.setVirtualHosts(notRunningProperties.getVirtualHosts());
        }
        String resourcesLocation = notRunningProperties.getResourcesLocation();
        if (resourcesLocation != null) {
            servletHandler.setResourceBase(resourcesLocation);
            servletHandler.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed",
                    Boolean.toString(notRunningProperties.isAllowDirectoryListing()));
        }
        servletHandler.addServlet(DefaultServlet.class, "/");
    }

    @SuppressWarnings("unchecked")
    private void initializeErrorHandler() {
        for (Map.Entry<String, ?> entry : servlets.entrySet()) {
            Object servlet = entry.getValue();
            if (servlet instanceof Servlet) {
                servletHandler.addServlet(new ServletHolder((Servlet) servlet), entry.getKey());
            } else if (servlet instanceof Class) {
                servletHandler.addServlet((Class) servlet, entry.getKey());
            }
        }
        EasyJettyErrorHandler errorHandler = getErrorHandler(false);
        if (errorHandler != null) {
            server.addBean(errorHandler);
        }
    }

    private void initializeSSL() {
        SSLConfig sslConfig = notRunningProperties.getSSLConfig();
        if (sslConfig != null) {
            while (sslOnly && server.getConnectors().length == 1) {
                server.removeConnector(server.getConnectors()[0]);
            }
            server.addConnector(sslConfig.createSslConnector(server));
        }
    }

    private void initializeServerAttributes() {
        if (maxFormSize >= 0) {
            System.setProperty("org.eclipse.jetty.server.Request.maxFormContentSize", Integer.toString(maxFormSize));
            getServletHandler().setMaxFormContentSize(maxFormSize);
            server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", maxFormSize);
        }
    }
}
