package com.athaydes.easyjetty;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EasyJettyTest {

    final EasyJetty easy = new EasyJetty();
    final HttpClient client = new HttpClient();
    static final CountDownLatch latch = new CountDownLatch(1);

    @Before
    public void setup() throws Exception {
        client.setTimeout(1500L);
        client.start();
    }

    @After
    public void cleanup() {
        try {
            easy.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        ContentExchange exchange = new ContentExchange(false);
        exchange.setURL("http://localhost:8080/hello");

        client.send(exchange);

        // THEN the servlet responds
        assertEquals(HttpExchange.STATUS_COMPLETED, exchange.waitForDone());
        assertEquals("Hello", exchange.getResponseContent().trim());
        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    public static class HelloServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/plain");
            resp.getOutputStream().println("Hello");
            latch.countDown();
        }
    }

}
