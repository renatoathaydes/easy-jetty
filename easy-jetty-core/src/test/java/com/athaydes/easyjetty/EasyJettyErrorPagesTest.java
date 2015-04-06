package com.athaydes.easyjetty;

import org.boon.IO;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.GET;
import static org.junit.Assert.assertEquals;

public class EasyJettyErrorPagesTest extends EasyJettyTest {

    @Test
    public void simpleErrorPageByStatusCodeCanBeSet() throws Exception {
        easy.errorPage(404, "/html/page404.html")
                .resourcesLocation("src/test/resources/srv")
                .start();

        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/does/not/exist");
        assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
        assertEquals(IO.read(getClass().getResourceAsStream("/srv/html/page404.html")), response.getContentAsString());
    }

    @Test
    public void simpleErrorPageByStatusCodeRangesCanBeSet() throws Exception {
        easy.errorPage(400, 499, "/html/page404.html")
                .errorPage(500, 599, "/html/page500.html")
                .resourcesLocation("src/test/resources/srv")
                .on(GET, "ex", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        throw new RuntimeException();
                    }
                }).start();

        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/does/not/exist");
        assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
        assertEquals(IO.read(getClass().getResourceAsStream("/srv/html/page404.html")), response.getContentAsString());

        ContentResponse response2 = sendReqAndWait("GET", "http://localhost:8080/ex");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response2.getStatus());
        assertEquals(IO.read(getClass().getResourceAsStream("/srv/html/page500.html")), response2.getContentAsString());
    }

    @Test
    public void handlerErrorPageByStatusCodeCanBeSet() throws Exception {
        easy.errorPage(404, "error/page404")
                .on(GET, "error/page404", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        System.out.println("Hej, got into the 404 handler");
                        exchange.send("Hello 404!");
                        System.out.println("sent a 404 msg");
                    }
                }).start();

        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/does/not/exist");
        assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
        assertEquals("Hello 404!", response.getContentAsString().trim());
    }

    @Test
    public void handlerErrorPageByStatusCodeRangesCanBeSet() throws Exception {
        easy.errorPage(400, 499, "error/page404")
                .errorPage(500, 599, "error/page500")
                .on(GET, "error/page404", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.send("Hello 404!");
                    }
                })
                .on(GET, "error/page500", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.send("Hello 500!");
                    }
                })
                .on(GET, "throw", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        throw new RuntimeException();
                    }
                }).start();

        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/does/not/exist");
        ContentResponse response2 = sendReqAndWait("GET", "http://localhost:8080/throw");

        assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
        assertEquals("Hello 404!", response.getContentAsString().trim());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response2.getStatus());
        assertEquals("Hello 500!", response2.getContentAsString().trim());

    }

    @Test
    public void servletErrorPageByStatusCodeCanBeSet() throws Exception {
        class Servlet404 extends HttpServlet {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.getOutputStream().print("Servlet 404");
            }
        }
        easy.errorPage(404, "error/page404")
                .servlet("error/page404", new Servlet404()).start();

        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/does/not/exist");
        assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
        assertEquals("Servlet 404", response.getContentAsString());
    }

    @Test
    public void servletErrorPageByStatusCodeRangesCanBeSet() throws Exception {
        class Servlet404 extends HttpServlet {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.getOutputStream().print("Servlet 404");
            }
        }
        class Servlet500 extends HttpServlet {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.getOutputStream().print("Servlet 500");
            }
        }
        class ServletWithError extends HttpServlet {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                throw new RuntimeException();
            }
        }
        easy.errorPage(400, 499, "error/page404")
                .errorPage(500, 599, "error/page500")
                .servlet("throw", new ServletWithError())
                .servlet("error/page404", new Servlet404())
                .servlet("error/page500", new Servlet500()).start();

        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/does/not/exist");
        ContentResponse response2 = sendReqAndWait("GET", "http://localhost:8080/throw");

        assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
        assertEquals("Servlet 404", response.getContentAsString());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response2.getStatus());
        assertEquals("Servlet 500", response2.getContentAsString());
    }

}
