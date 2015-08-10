package com.athaydes.easyjetty;


import groovy.servlet.AbstractHttpServlet;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.RequestLog;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.GET;
import static com.athaydes.easyjetty.http.MethodArbiter.Method.POST;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
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

    @Test
    public void requestLogHandlerIsCalledOnRequest() throws Exception {
        final String logName = "requestLogHandlerIsCalledOnRequest.log";
        final File logFile = new File("build/" + logName);
        if (logFile.exists()) {
            assertTrue(logFile.delete());
        }

        // GIVEN A RequestLog is configured to log to a file
        RequestLog requestLog = new NCSARequestLog(logFile.getAbsolutePath());

        // WHEN requests are made to some handlers and static resources
        easy.on(GET, "/whatever", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.send("ok");
            }
        }).on(POST, "/ignore", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.send("ok");
            }
        }).resourcesLocation(".").requestLog(requestLog).start();

        ContentResponse response1 = sendReqAndWait("GET", "http://localhost:8080/whatever");
        ContentResponse response2 = sendReqAndWait("POST", "http://localhost:8080/ignore");
        ContentResponse response3 = sendReqAndWait("GET", "http://localhost:8080/build.gradle");

        // THEN all requests are successful
        assertEquals(HttpStatus.OK_200, response1.getStatus());
        assertEquals(HttpStatus.OK_200, response2.getStatus());
        assertEquals(HttpStatus.OK_200, response3.getStatus());

        // AND the log file is created within a few seconds
        waitUntil(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return logFile.exists();
            }
        }, 2_000L);

        // AND the log file contains the expected contents
        List<String> lines = Files.readAllLines(logFile.toPath(), Charset.defaultCharset());

        assertThat(lines.size(), equalTo(3));
        assertThat(lines.get(0), containsString("/whatever"));
        assertThat(lines.get(1), containsString("/ignore"));
        assertThat(lines.get(2), containsString("/build.gradle"));
    }

}
