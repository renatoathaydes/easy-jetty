package com.athaydes.easyjetty;

import com.athaydes.easyjetty.http.MethodArbiter.Method;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.GET;
import static com.athaydes.easyjetty.http.MethodArbiter.Method.OPTIONS;
import static com.athaydes.easyjetty.http.MethodArbiter.Method.anyMethod;
import static com.athaydes.easyjetty.http.MethodArbiter.Method.anyOf;
import static com.athaydes.easyjetty.http.MethodArbiter.Method.customMethod;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
    public void customMethodHandlers() throws Exception {
        easy.on(customMethod("HELLO"), "/hi", new Response() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("Hello Example");
            }
        }).start();

        // WHEN a GET request is sent out
        ContentExchange exchange = sendReqAndWait("HELLO", "http://localhost:8080/hi");

        // THEN the expected response is provided
        assertEquals(HttpExchange.STATUS_COMPLETED, exchange.waitForDone());
        assertEquals("Hello Example", exchange.getResponseContent().trim());

        // WHEN a PUT/POST/DELETE request is sent out
        ContentExchange putEx = sendReqAndWait("PUT", "http://localhost:8080/hi");
        ContentExchange postEx = sendReqAndWait("POST", "http://localhost:8080/hi");
        ContentExchange delEx = sendReqAndWait("DELETE", "http://localhost:8080/hi");

        // THEN the reponse is 405/404/405 respectively
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, putEx.getResponseStatus());
        assertEquals(HttpStatus.NOT_FOUND_404, postEx.getResponseStatus());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, delEx.getResponseStatus());
    }

    @Test
    public void anyOfMethodHandlers() throws Exception {
        easy.on(anyOf(GET, OPTIONS), "/anyof", new Response() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("AnyOf");
            }
        }).start();

        // WHEN a PATCH/OPTIONS request is sent out
        ContentExchange patchEx = sendReqAndWait("GET", "http://localhost:8080/anyof");
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

    @Test
    public void largeNumberOfHandlersTest() throws Exception {
        final List<String> subPaths = Arrays.asList("hej", "hello", "ola");
        final int maxIs = 100;
        final int maxJs = 10;

        class Data {
            int i, j;
            String p;

            Data(int i, int j, String p) {
                this.i = i;
                this.j = j;
                this.p = p;
            }
        }

        abstract class ToRun {
            abstract boolean run(Data data);
        }

        class Runner {
            void start(ToRun toRun) {
                mainLoop:
                for (int i = 0; i < maxIs; i++) {
                    for (int j = 0; j < maxJs; j++) {
                        for (final String p : subPaths) {
                            boolean shouldContinue = toRun.run(new Data(i, j, p));
                            if (!shouldContinue) {
                                break mainLoop;
                            }
                        }
                    }
                }
            }
        }

        // GIVEN a large number of paths with corresponding handlers
        new Runner().start(new ToRun() {
            @Override
            boolean run(final Data data) {
                easy.on(GET, "/index" + data.i + "/" + data.p + data.j, new Response() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.out.println("index" + data.i + " j" + data.j + data.p);
                    }
                }).on(GET, "/sub" + data.i + "/j" + data.j + "/" + data.p, new Response() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.out.println("sub" + data.i + " j" + data.j + data.p);
                    }
                });
                return true;
            }
        });

        easy.start();

        // WHEN a GET request is sent out to each one of the handlers
        final AtomicReference<Throwable> error = new AtomicReference<>();
        new Runner().start(new ToRun() {
            @Override
            boolean run(final Data data) {
                try {
                    ContentExchange exchange1 = sendReqAndWait("GET", "http://localhost:8080/index" + data.i + "/" + data.p + data.j);
                    assertEquals(HttpStatus.OK_200, exchange1.getResponseStatus());
                    assertEquals("index" + data.i + " j" + data.j + data.p, exchange1.getResponseContent().trim());

                    ContentExchange ex2 = sendReqAndWait("GET", "http://localhost:8080/sub" + data.i + "/j" + data.j + "/" + data.p);
                    assertEquals(HttpStatus.OK_200, ex2.getResponseStatus());
                    assertEquals("sub" + data.i + " j" + data.j + data.p, ex2.getResponseContent().trim());
                    return true;
                } catch (Throwable t) {
                    error.set(t);
                    return false;
                }
            }
        });

        // THEN no error is thrown, all requests completed and passed all assertions
        assertNull(error.get());
    }

    @Test
    public void parametersTest() throws Exception {
        easy.on(GET, "/:p1", new Response() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.send("Param " + exchange.params.get("p1"));
            }
        }).on(GET, "/hi/:name", new Response() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.send("Name " + exchange.params.get("name"));
            }
        }).start();

        // WHEN a GET request is sent out to each endpoint
        ContentExchange exchange1 = sendReqAndWait("GET", "http://localhost:8080/something");
        ContentExchange exchange2 = sendReqAndWait("GET", "http://localhost:8080/hi/john");

        // THEN the expected response is provided
        assertEquals("Param something", exchange1.getResponseContent().trim());
        assertEquals("Name john", exchange2.getResponseContent().trim());
    }

}
