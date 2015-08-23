package com.athaydes.easyjetty;

import com.athaydes.easyjetty.http.MethodArbiter;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.GET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class EasyJettyFilterTest extends EasyJettyTest {

    @Test
    public void shouldStopProcessingStaticResourceWhenFilterDoesNotAllow() throws Exception {
        easy.resourcesLocation("src/main/java/com/athaydes/easyjetty")
                .filterOn("/EasyJetty.java", new Filter() {
                    @Override
                    public boolean allowFurther(FilterExchange exchange) throws IOException {
                        exchange.out.println("Filter here!");
                        exchange.response.setStatus(HttpStatus.BAD_REQUEST_400);
                        return false;
                    }
                }).start();

        // WHEN a GET request is sent out
        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/EasyJetty.java");

        // THEN the expected response is provided
        assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
        assertEquals("Filter here!", response.getContentAsString().trim());
    }

    @Test
    public void shouldContinueProcessingStaticResourceWhenFilterAllows() throws Exception {
        final String responsePrefix = "XXXXXX";
        easy.resourcesLocation("src/main/java/com/athaydes/easyjetty")
                .filterOn("/EasyJetty.java", new Filter() {
                    @Override
                    public boolean allowFurther(FilterExchange exchange) throws IOException {
                        exchange.out.println(responsePrefix);
                        return true;
                    }
                }).start();

        // WHEN a GET request is sent out
        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/EasyJetty.java");

        // THEN the expected response is provided
        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertThat(response.getContentAsString(), CoreMatchers.startsWith(responsePrefix));
    }

    public static class HelloServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.getOutputStream().println("Hello");
        }
    }

    @Test
    public void shouldStopProcessingServletWhenFilterDoesNotAllow() throws Exception {
        easy.servlet("hello", HelloServlet.class)
                .filterOn("/hello", new Filter() {
                    @Override
                    public boolean allowFurther(FilterExchange exchange) throws IOException {
                        exchange.out.println("Filter here!");
                        exchange.response.setStatus(HttpStatus.BAD_REQUEST_400);
                        return false;
                    }
                }).start();

        // WHEN a GET request is sent out
        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/hello");

        // THEN the expected response is provided
        assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
        assertEquals("Filter here!", response.getContentAsString().trim());
    }

    @Test
    public void shouldContinueProcessingServletWhenFilterAllows() throws Exception {
        final String responsePrefix = "XXXXXX";
        easy.servlet("hello", HelloServlet.class)
                .filterOn("hello", new Filter() {
                    @Override
                    public boolean allowFurther(FilterExchange exchange) throws IOException {
                        exchange.out.println(responsePrefix);
                        return true;
                    }
                }).start();

        // WHEN a GET request is sent out
        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/hello");

        // THEN the expected response is provided
        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertThat(response.getContentAsString(), CoreMatchers.startsWith(responsePrefix));
    }

    @Test
    public void shouldStopProcessingHandlerWhenFilterDoesNotAllow() throws Exception {
        easy.on(GET, "/hello", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.send("Hello");
            }
        }).filterOn("/hello", new Filter() {
            @Override
            public boolean allowFurther(FilterExchange exchange) throws IOException {
                exchange.out.println("Filter here!");
                exchange.response.setStatus(HttpStatus.BAD_REQUEST_400);
                return false;
            }
        }).start();

        // WHEN a GET request is sent out
        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/hello");

        // THEN the expected response is provided
        assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
        assertEquals("Filter here!", response.getContentAsString().trim());
    }

    @Test
    public void shouldContinueProcessingHandlerWhenFilterAllows() throws Exception {
        final String responsePrefix = "XXXXXX";
        easy.on(GET, "/hello", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.send("Hello");
            }
        }).filterOn("/hello", new Filter() {
            @Override
            public boolean allowFurther(FilterExchange exchange) throws IOException {
                exchange.out.println(responsePrefix);
                return true;
            }
        }).start();

        // WHEN a GET request is sent out
        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/hello");

        // THEN the expected response is provided
        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertThat(response.getContentAsString(), CoreMatchers.startsWith(responsePrefix));
    }

}
