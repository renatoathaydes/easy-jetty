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

    /**
     * Builds a SSLConfig with the minimum mandatory settings.
     * <p/>
     * More settings can be configured by using the methods of this class,
     * similar to how EasyJetty can be configured.
     *
     * @param keyStorePath       path to the keystore
     * @param keyStorePassword   password for the keystore
     * @param keyManagerPassword password for the key manager
     */
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

    /**
     * Sets the port to be used by the HTTPS connector.
     *
     * @param port to use
     * @return this
     */
    public SSLConfig port(int port) {
        this.port = port;
        return this;
    }

    /**
     * Sets the host to be used by the HTTPS connector.
     *
     * @param host to use
     * @return this
     */
    public SSLConfig host(String host) {
        this.host = host;
        return this;
    }

    /**
     * Sets the connection idle timeout.
     *
     * @param idleTimeout timeout in milliseconds
     * @return this
     */
    public SSLConfig idleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    /**
     * Sets the path to the trustStore. If not set, the keystore path is used.
     *
     * @param trustStorePath to use
     * @return this
     */
    public SSLConfig trustStorePath(String trustStorePath) {
        sslContextFactory.setTrustStorePath(trustStorePath);
        return this;
    }

    /**
     * Sets the trustStore password. If not set, the keystore password is used.
     *
     * @param trustStorePassword
     * @return
     */
    public SSLConfig trustStorePassword(String trustStorePassword) {
        sslContextFactory.setTrustStorePassword(trustStorePassword);
        return this;
    }

    /**
     * Sets the alias of the SSL certificate to be used by the HTTPS connector.
     *
     * @param certAlias alias to use
     * @return this
     */
    public SSLConfig certAlias(String certAlias) {
        sslContextFactory.setCertAlias(certAlias);
        return this;
    }

    /**
     * Sets the cipher suites to exclude.
     * <p/>
     * By default, some cipher suites are already excluded as recommended by the Jetty documentation.
     * Only set this if you know what you're doing.
     *
     * @param excludeCipherSuites cipher suites to exclude
     * @return this
     */
    public SSLConfig excludeCipherSuites(String... excludeCipherSuites) {
        sslContextFactory.setExcludeCipherSuites(excludeCipherSuites);
        return this;
    }

    /**
     * Sets the cipher suites to include.
     * <p/>
     * By default, some cipher suites are included as recommended by the Jetty documentation.
     * Only set this if you know what you're doing.
     *
     * @param includeCipherSuites cipher suites to include
     * @return this
     */
    public SSLConfig includeCipherSuites(String... includeCipherSuites) {
        sslContextFactory.setIncludeCipherSuites(includeCipherSuites);
        return this;
    }

    /**
     * Sets the SSL session timeout.
     *
     * @param sslSessionTimeout in seconds
     * @return this
     */
    public SSLConfig sslSessionTimeout(int sslSessionTimeout) {
        sslContextFactory.setSslSessionTimeout(sslSessionTimeout);
        return this;
    }

    /**
     * Require clients to provide a SSL certificate.
     *
     * @param needClientAuth whether to require a client SSL certificate.
     * @return this
     */
    public SSLConfig needClientAuth(boolean needClientAuth) {
        sslContextFactory.setNeedClientAuth(needClientAuth);
        return this;
    }

    /**
     * Sets the protocols to include.
     * <p/>
     * By default, some protocols are included as recommended by the Jetty documentation.
     * Only set this if you know what you're doing.
     *
     * @param includeProtocols protocols to include
     * @return this
     */
    public SSLConfig includeProtocols(String... includeProtocols) {
        sslContextFactory.setIncludeProtocols(includeProtocols);
        return this;
    }


    /**
     * Sets the protocols to exclude.
     * <p/>
     * By default, some cipher suites are already excluded as recommended by the Jetty documentation.
     * Only set this if you know what you're doing.
     *
     * @param excludeProtocols protocols to exclude
     * @return this
     */
    public SSLConfig excludeProtocols(String... excludeProtocols) {
        sslContextFactory.setExcludeProtocols(excludeProtocols);
        return this;
    }

    /**
     * Sets the path to file that contains Certificate Revocation List.
     *
     * @param crlPath path
     * @return this
     */
    public SSLConfig crlPath(String crlPath) {
        sslContextFactory.setCrlPath(crlPath);
        return this;
    }

    /**
     * Sets whether CRL Distribution Points Support should be enabled.
     *
     * @param enableCRLDP enabled
     * @return this
     */
    public SSLConfig enableCRLDP(boolean enableCRLDP) {
        sslContextFactory.setEnableCRLDP(enableCRLDP);
        return this;
    }

    /**
     * Sets whether On-Line Certificate Status Protocol support should be enabled.
     *
     * @param enableOCSP enabled
     * @return this
     */
    public SSLConfig enableOCSP(boolean enableOCSP) {
        sslContextFactory.setEnableOCSP(enableOCSP);
        return this;
    }

    /**
     * Sets the provider of the keystore
     *
     * @param keyStoreProvider provider
     * @return this
     */
    public SSLConfig keyStoreProvider(String keyStoreProvider) {
        sslContextFactory.setKeyStoreProvider(keyStoreProvider);
        return this;
    }

    /**
     * Sets the keystore type. By default, "JKS" will be used.
     *
     * @param keyStoreType type
     * @return this
     */
    public SSLConfig keyStoreType(String keyStoreType) {
        sslContextFactory.setKeyStoreType(keyStoreType);
        return this;
    }

    /**
     * Sets whether TLS renegotiation should be allowed.
     *
     * @param allow enable
     * @return this
     */
    public SSLConfig allowRenegotiation(boolean allow) {
        sslContextFactory.setRenegotiationAllowed(allow);
        return this;
    }

    public SslContextFactory getSslContextFactory() {
        return sslContextFactory;
    }

    ServerConnector createSslConnector(Server server) {
        ServerConnector sslConnector = new ServerConnector(server, sslContextFactory);
        sslConnector.setPort(port);
        sslConnector.setHost(host);
        sslConnector.setIdleTimeout(idleTimeout);
        return sslConnector;
    }
}
