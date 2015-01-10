package com.athaydes.easyjetty;

import com.athaydes.easyjetty.http.MethodArbiter.Method;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.*;
import static org.junit.Assert.assertEquals;

public class EasyJettyHandlersTest extends EasyJettyTest {

    @Test
    public void anyMethodHandlers() throws Exception {
        final List<String> handledMethods = new ArrayList<>();
        easy.on(anyMethod(), "/handleme", new Response() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                handledMethods.add(exchange.baseRequest.getMethod());
            }
        }).start();

        // WHEN requests are sent to the handler using all methods
        List<ContentExchange> exchanges = new ArrayList<>();
        for (Method method : Method.values()) {
            exchanges.add(sendReqAndWait(method.name(), "http://localhost:8080/handleme"));
        }

        // THEN the handler responds to all methods
        List<String> expectedMethods = new ArrayList<>();
        for (Method method : Method.values()) {
            ContentExchange exchange = exchanges.remove(0);
            assertEquals(HttpExchange.STATUS_COMPLETED, exchange.waitForDone());

            if (method != Method.CONNECT) // not handled by Jetty
                expectedMethods.add(method.name());
        }

        assertEquals(expectedMethods, handledMethods);
    }

    @Test
    public void singleMethodHandlers() throws Exception {
        easy.on(GET, "/example", new Response() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("Hello Example");
            }
        }).start();

        // WHEN a GET request is sent out
        ContentExchange exchange = sendReqAndWait("GET", "http://localhost:8080/example");

        // THEN the expected response is provided
        assertEquals(HttpExchange.STATUS_COMPLETED, exchange.waitForDone());
        assertEquals("Hello Example", exchange.getResponseContent().trim());

        // WHEN a PUT/POST/DELETE request is sent out
        ContentExchange putEx = sendReqAndWait("PUT", "http://localhost:8080/example");
        ContentExchange postEx = sendReqAndWait("POST", "http://localhost:8080/example");
        ContentExchange delEx = sendReqAndWait("DELETE", "http://localhost:8080/example");

        // THEN the reponse is 405/404/405 respectively
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, putEx.getResponseStatus());
        assertEquals(HttpStatus.NOT_FOUND_404, postEx.getResponseStatus());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, delEx.getResponseStatus());
    }

    @Test
    public void anyOfMethodHandlers() throws Exception {
        easy.on(anyOf(PATCH, OPTIONS), "/anyof", new Response() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("AnyOf");
            }
        }).start();

        // WHEN a PATCH/OPTIONS request is sent out
        ContentExchange patchEx = sendReqAndWait("PATCH", "http://localhost:8080/anyof");
        ContentExchange optionsEx = sendReqAndWait("OPTIONS", "http://localhost:8080/anyof");

        // THEN the expected response is provided
        assertEquals("AnyOf", patchEx.getResponseContent().trim());
        assertEquals("AnyOf", optionsEx.getResponseContent().trim());

        // WHEN a PUT/POST/DELETE request is sent out
        ContentExchange putEx = sendReqAndWait("PUT", "http://localhost:8080/anyof");
        ContentExchange postEx = sendReqAndWait("POST", "http://localhost:8080/anyof");
        ContentExchange delEx = sendReqAndWait("DELETE", "http://localhost:8080/anyof");

        // THEN the reponse is 405/404/405 respectively
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, putEx.getResponseStatus());
        assertEquals(HttpStatus.NOT_FOUND_404, postEx.getResponseStatus());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, delEx.getResponseStatus());
    }

}
