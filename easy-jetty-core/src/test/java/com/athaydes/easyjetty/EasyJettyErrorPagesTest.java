package com.athaydes.easyjetty;

import org.boon.IO;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

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

}
