package com.athaydes.easyjetty;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class EasyJettyBasicTest extends EasyJettyTest {


    @Test
    public void serverStarts() {
        easy.start();
        assertTrue(easy.isRunning());
    }

    @Test
    public void canSelectPort() throws Exception {
        easy.resourcesLocation("src/").port(8088).start();
        ContentResponse response1 = sendReqAndWait("GET", "http://localhost:8088/");
        assertEquals(HttpStatus.OK_200, response1.getStatus());

        easy.stop();

        easy.resourcesLocation("src/").port(8085).start();
        ContentResponse response2 = sendReqAndWait("GET", "http://localhost:8085/");
        assertEquals(HttpStatus.OK_200, response2.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void cannotChangePortWhileServerIsRunning() {
        easy.port(8085).start().port(8088);
    }

    @Test
    public void resourcesLocationIsRespected() throws Exception {
        easy.resourcesLocation("src/").start();
        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/");
        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertThat(response.getContentAsString(), containsString("test"));
    }

    @Test
    public void simpleContextPathIsRespected() throws Exception {
        easy.resourcesLocation("src/").contextPath("/ctx").start();
        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/ctx/");
        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertThat(response.getContentAsString(), containsString("test"));
    }

    @Test
    public void directoryListingOptionIsRespected() throws Exception {
        easy.resourcesLocation("src/").disableDirectoryListing().start();
        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/");
        assertEquals(HttpStatus.FORBIDDEN_403, response.getStatus());
    }

    @Test
    public void virtualHostsCanBeSet() throws Exception {
        easy.withVirtualHosts("127.0.0.1")
                .resourcesLocation("src/").start();
        ContentResponse response = sendReqAndWait("GET", "http://127.0.0.1:8080/");
        assertEquals(HttpStatus.OK_200, response.getStatus());
        ContentResponse response2 = sendReqAndWait("GET", "http://localhost:8080/");
        assertEquals(HttpStatus.NOT_FOUND_404, response2.getStatus());
    }

    static final String CACERTS = "../ssl/renatokeystore";
    static final String KEYPASS = "renatopass";
    static final String MANAGER_PASS = "mypass";

    @Test
    public void sslConfigWorks() throws Exception {
        easy.ssl(new SSLConfig(CACERTS, KEYPASS, MANAGER_PASS))
                .resourcesLocation("src/")
                .start();

        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/");
        ContentResponse sslResponse = sendReqAndWait("GET", "https://localhost:8443/");

        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertEquals(HttpStatus.OK_200, sslResponse.getStatus());
    }

    @Test
    public void sslOnlyConfigWorks() throws Exception {
        easy.sslOnly(new SSLConfig(CACERTS, KEYPASS, MANAGER_PASS))
                .resourcesLocation("src/")
                .start();

        ContentResponse sslResponse = sendReqAndWait("GET", "https://localhost:8443/");
        assertEquals(HttpStatus.OK_200, sslResponse.getStatus());

        try {
            ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/");
            fail("Should have failed but returned " + response.getStatus());
        } catch (Exception e) {
            // ok
        }
    }

}
