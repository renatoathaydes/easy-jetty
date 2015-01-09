package com.athaydes.easyjetty;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpExchange;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EasyJettyServletTest extends EasyJettyTest {

    @Test
    public void serverStarts() {
        easy.start();
        assertTrue(easy.isRunning());
        easy.stop();
    }

    @Test
    public void servletGetsRequests() throws Exception {
        easy.servlet("/hello", HelloServlet.class).start();

        // WHEN a request is sent to the servlet
        ContentExchange exchange = sendReqAndWait("GET", "http://localhost:8080/hello");

        // THEN the servlet responds
        assertEquals(HttpExchange.STATUS_COMPLETED, exchange.waitForDone());
        assertEquals("Hello", exchange.getResponseContent().trim());
    }

    public static class HelloServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/plain");
            resp.getOutputStream().println("Hello");
        }
    }

}
