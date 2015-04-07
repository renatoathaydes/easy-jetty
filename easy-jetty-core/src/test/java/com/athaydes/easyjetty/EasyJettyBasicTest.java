package com.athaydes.easyjetty;


import groovy.servlet.AbstractHttpServlet;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.POST;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

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

    public static final String CACERTS = "../ssl/renatokeystore";
    public static final String KEYPASS = "renatopass";
    public static final String MANAGER_PASS = "mypass";

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

    @Test
    public void maxBytesAcceptedWorksInServlets() throws Exception {
        class MyServlet extends AbstractHttpServlet {
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                req.getParameterMap(); // building the map will attempt to consume the payload
            }
        }

        easy.maxFormSize(10).servlet("/data", new MyServlet()).start();

        ContentResponse response = sendReqAndWait("POST", "http://localhost:8080/data",
                Collections.<String, String>emptyMap(), randomPayloadOfSize(10));
        assertEquals(HttpStatus.OK_200, response.getStatus());

        ContentResponse response2 = sendReqAndWait("POST", "http://localhost:8080/data",
                Collections.<String, String>emptyMap(), randomPayloadOfSize(11));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response2.getStatus());
    }

    @Test
    public void maxBytesAcceptedWorksInHandlers() throws Exception {
        easy.maxFormSize(10).on(POST, "data", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                System.out.println(exchange.receiveAs(String.class));
            }
        }).start();

        ContentResponse response = sendReqAndWait("POST", "http://localhost:8080/data",
                Collections.<String, String>emptyMap(), randomPayloadOfSize(10));
        assertEquals(HttpStatus.OK_200, response.getStatus());

        ContentResponse response2 = sendReqAndWait("POST", "http://localhost:8080/data",
                Collections.<String, String>emptyMap(), randomPayloadOfSize(11));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response2.getStatus());
    }

}
