package com.athaydes.easyjetty;

import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;

/**
 * Group of properties that can only change when the server is not running.
 */
class CanChangeWhenServerNotRunningProperties {

    private int port;
    private String contextPath;
    private boolean allowDirectoryListing;
    private RequestLog requestLog;
    private String resourcesLocation;
    private String[] virtualHosts;
    private SSLConfig sslConfig;

    private static void errorIfServerStarted(Server server) {
        if (server != null && server.isStarted()) {
            throw new IllegalStateException("Server already started");
        }
    }

    public int getPort() {
        return port;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setPort(int port, Server server) {
        errorIfServerStarted(server);
        this.port = port;
    }

    public void setContextPath(String contextPath, Server server) {
        errorIfServerStarted(server);
        this.contextPath = contextPath;
    }

    public boolean isAllowDirectoryListing() {
        return allowDirectoryListing;
    }

    public void setAllowDirectoryListing(boolean allowDirectoryListing, Server server) {
        errorIfServerStarted(server);
        this.allowDirectoryListing = allowDirectoryListing;
    }

    public RequestLog getRequestLog() {
        return requestLog;
    }

    public void setRequestLog(RequestLog requestLog, Server server) {
        errorIfServerStarted(server);
        this.requestLog = requestLog;
    }

    public String getResourcesLocation() {
        return resourcesLocation;
    }

    public void setResourcesLocation(String resourcesLocation, Server server) {
        errorIfServerStarted(server);
        this.resourcesLocation = resourcesLocation;
    }

    public String[] getVirtualHosts() {
        return virtualHosts;
    }

    public void setVirtualHosts(Server server, String... virtualHosts) {
        errorIfServerStarted(server);
        this.virtualHosts = virtualHosts;
    }

    public SSLConfig getSSLConfig() {
        return sslConfig;
    }

    public void setSsl(SSLConfig sslConfig, Server server) {
        errorIfServerStarted(server);
        this.sslConfig = sslConfig;
    }

}
