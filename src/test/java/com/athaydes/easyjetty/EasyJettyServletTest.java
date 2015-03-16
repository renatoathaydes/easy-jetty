package com.athaydes.easyjetty;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class EasyJettyServletTest extends EasyJettyTest {

    @Test
    public void servletGetsRequests() throws Exception {
        easy.servlet("/hello", HelloServlet.class).start();

        // WHEN a request is sent to the servlet
        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/hello");

        // THEN the servlet responds
        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertEquals("Hello", response.getContentAsString().trim());
    }

    public static class HelloServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/plain");
            resp.getOutputStream().println("Hello");
        }
    }

}
