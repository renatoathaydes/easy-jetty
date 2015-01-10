package com.athaydes.easyjetty;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class EasyJettyBasicTest extends EasyJettyTest {


    @Test
    public void serverStarts() {
        easy.start();
        assertTrue(easy.isRunning());
        easy.stop();
    }

    @Test
    public void canSelectPort() throws Exception {
        easy.port(8088).start();
        ContentExchange exchange1 = sendReqAndWait("GET", "http://localhost:8088/");
        assertEquals(HttpExchange.STATUS_COMPLETED, exchange1.waitForDone());

        easy.stop();

        easy.port(8085).start();
        ContentExchange exchange2 = sendReqAndWait("GET", "http://localhost:8085/");
        assertEquals(HttpExchange.STATUS_COMPLETED, exchange2.waitForDone());
    }

    @Test(expected = IllegalStateException.class)
    public void cannotChangePortWhileServerIsRunning() {
        easy.port(8085).start().port(8088);
    }

    @Test
    public void resourcesLocationIsRespected() throws Exception {
        easy.resourcesLocation("src/").start();
        ContentExchange exchange = sendReqAndWait("GET", "http://localhost:8080/");
        assertEquals(HttpExchange.STATUS_COMPLETED, exchange.waitForDone());
        assertThat(exchange.getResponseContent(), containsString("test"));
    }

    @Test
    public void simpleContextPathIsRespected() throws Exception {
        easy.resourcesLocation("src/").contextPath("/ctx").start();
        ContentExchange exchange = sendReqAndWait("GET", "http://localhost:8080/ctx/");
        assertEquals(HttpExchange.STATUS_COMPLETED, exchange.waitForDone());
        assertThat(exchange.getResponseContent(), containsString("test"));
    }

    @Test
    public void directoryListingOptionIsRespected() throws Exception {
        easy.resourcesLocation("src/").disableDirectoryListing().start();
        ContentExchange exchange = sendReqAndWait("GET", "http://localhost:8080/");
        assertEquals(HttpExchange.STATUS_COMPLETED, exchange.waitForDone());
        assertEquals(HttpStatus.FORBIDDEN_403, exchange.getResponseStatus());
    }

}
