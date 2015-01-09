package com.athaydes.easyjetty;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpExchange;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

}
