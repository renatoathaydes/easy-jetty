package com.athaydes.easyjetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * SSL configuration.
 */
public final class SSLConfig {

    final SslContextFactory sslContextFactory;
    int port = 8443;
    String host = "localhost";
    long idleTimeout = 60_000L;

    public SSLConfig(String keyStorePath, String keyStorePassword, String keyManagerPassword) {
        sslContextFactory = new SslContextFactory(keyStorePath);
        sslContextFactory.setKeyStorePassword(keyStorePassword);
        sslContextFactory.setKeyManagerPassword(keyManagerPassword);
        sslContextFactory.setTrustStorePath(keyStorePath);
        sslContextFactory.setTrustStorePassword(keyStorePassword);

        // recommended defaults as in http://www.eclipse.org/jetty/documentation/current/configuring-ssl.html
        sslContextFactory.setExcludeCipherSuites(".*NULL.*", ".*RC4.*", ".*MD5.*", ".*DES.*", ".*DSS.*");
        sslContextFactory.setIncludeCipherSuites("TLS_DHE_RSA.*", "TLS_ECDHE.*");
        sslContextFactory.setExcludeProtocols("SSLv3");
        sslContextFactory.setRenegotiationAllowed(false);
    }

    public SSLConfig port(int port) {
        this.port = port;
        return this;
    }

    public SSLConfig host(String host) {
        this.host = host;
        return this;
    }

    public SSLConfig idleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    public SSLConfig trustStorePath(String trustStorePath) {
        sslContextFactory.setTrustStorePath(trustStorePath);
        return this;
    }

    public SSLConfig trustStorePassword(String trustStorePassword) {
        sslContextFactory.setTrustStorePassword(trustStorePassword);
        return this;
    }

    public SSLConfig certAlias(String certAlias) {
        sslContextFactory.setCertAlias(certAlias);
        return this;
    }

    public SSLConfig excludeCipherSuites(String... excludeCipherSuites) {
        sslContextFactory.setExcludeCipherSuites(excludeCipherSuites);
        return this;
    }

    public SSLConfig includeCipherSuites(String... includeCipherSuites) {
        sslContextFactory.setIncludeCipherSuites(includeCipherSuites);
        return this;
    }

    public SSLConfig sslSessionTimeout(int sslSessionTimeout) {
        sslContextFactory.setSslSessionTimeout(sslSessionTimeout);
        return this;
    }

    public SSLConfig needClientAuth(boolean needClientAuth) {
        sslContextFactory.setNeedClientAuth(needClientAuth);
        return this;
    }

    public SSLConfig includeProtocols(String... includeProtocols) {
        sslContextFactory.setIncludeProtocols(includeProtocols);
        return this;
    }

    public SSLConfig excludeProtocols(String... excludeProtocols) {
        sslContextFactory.setExcludeProtocols(excludeProtocols);
        return this;
    }

    public SSLConfig crlPath(String crlPath) {
        sslContextFactory.setCrlPath(crlPath);
        return this;
    }

    public SSLConfig enableCRLDP(boolean enableCRLDP) {
        sslContextFactory.setEnableCRLDP(enableCRLDP);
        return this;
    }

    public SSLConfig enableOCSP(boolean enableOCSP) {
        sslContextFactory.setEnableOCSP(enableOCSP);
        return this;
    }

    public SSLConfig keyStoreProvider(String keyStoreProvider) {
        sslContextFactory.setKeyStoreProvider(keyStoreProvider);
        return this;
    }

    public SSLConfig keyStoreType(String keyStoreType) {
        sslContextFactory.setKeyStoreType(keyStoreType);
        return this;
    }

    public SSLConfig allowRenegotiation(boolean allow) {
        sslContextFactory.setRenegotiationAllowed(allow);
        return this;
    }

    ServerConnector createSslConnector(Server server) {
        ServerConnector sslConnector = new ServerConnector(server, sslContextFactory);
        sslConnector.setPort(port);
        sslConnector.setHost(host);
        sslConnector.setIdleTimeout(idleTimeout);
        return sslConnector;
    }
}
