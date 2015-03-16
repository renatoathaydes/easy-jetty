package com.athaydes.easyjetty;

import com.athaydes.easyjetty.http.MethodArbiter.Method;
import com.athaydes.easyjetty.mapper.ObjectMapper;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.*;
import static com.athaydes.easyjetty.http.MethodArbiterFactory.*;
import static org.junit.Assert.*;

public class EasyJettyHandlersTest extends EasyJettyTest {

    @Test
    public void anyMethodHandlers() throws Exception {
        final List<String> handledMethods = new ArrayList<>();
        easy.on(anyMethod(), "/handleme", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                handledMethods.add(exchange.baseRequest.getMethod());
            }
        }).start();

        // WHEN requests are sent to the handler using all methods
        List<ContentResponse> responses = new ArrayList<>();
        for (Method method : Method.values()) {
            responses.add(sendReqAndWait(method.name(), "http://localhost:8080/handleme"));
        }

        // THEN the handler responds to all methods
        List<String> expectedMethods = new ArrayList<>();
        for (Method method : Method.values()) {
            ContentResponse response = responses.remove(0);
            assertEquals(HttpStatus.OK_200, response.getStatus());

            expectedMethods.add(method.name());
        }

        assertEquals(expectedMethods, handledMethods);
    }

    @Test
    public void singleMethodHandlers() throws Exception {
        easy.on(GET, "/example", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("Hello Example");
            }
        }).start();

        // WHEN a GET request is sent out
        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/example");

        // THEN the expected response is provided
        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertEquals("Hello Example", response.getContentAsString().trim());

        // WHEN a PUT/POST/DELETE request is sent out
        ContentResponse putEx = sendReqAndWait("PUT", "http://localhost:8080/example");
        ContentResponse postEx = sendReqAndWait("POST", "http://localhost:8080/example");
        ContentResponse delEx = sendReqAndWait("DELETE", "http://localhost:8080/example");

        // THEN the reponse is 405/404/405 respectively
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, putEx.getStatus());
        assertEquals(HttpStatus.NOT_FOUND_404, postEx.getStatus());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, delEx.getStatus());
    }

    @Test
    public void customMethodHandlers() throws Exception {
        easy.on(singleMethod("HELLO"), "/hi", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("Hello Example");
            }
        }).start();

        // WHEN a HELLO request is sent out
        ContentResponse response = sendReqAndWait("HELLO", "http://localhost:8080/hi");

        // THEN the expected response is provided
        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertEquals("Hello Example", response.getContentAsString().trim());

        // WHEN a PUT/POST/DELETE request is sent out
        ContentResponse putEx = sendReqAndWait("PUT", "http://localhost:8080/hi");
        ContentResponse postEx = sendReqAndWait("POST", "http://localhost:8080/hi");
        ContentResponse delEx = sendReqAndWait("DELETE", "http://localhost:8080/hi");

        // THEN the reponse is 405/404/405 respectively
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, putEx.getStatus());
        assertEquals(HttpStatus.NOT_FOUND_404, postEx.getStatus());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, delEx.getStatus());
    }

    @Test
    public void anyOfMethodHandlers() throws Exception {
        easy.on(anyOf(GET, OPTIONS), "/anyof", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("AnyOf");
            }
        }).start();

        // WHEN a GET/OPTIONS request is sent out
        ContentResponse patchEx = sendReqAndWait("GET", "http://localhost:8080/anyof");
        ContentResponse optionsEx = sendReqAndWait("OPTIONS", "http://localhost:8080/anyof");

        // THEN the expected response is provided
        assertEquals("AnyOf", patchEx.getContentAsString().trim());
        assertEquals("AnyOf", optionsEx.getContentAsString().trim());

        // WHEN a PUT/POST/DELETE request is sent out
        ContentResponse putEx = sendReqAndWait("PUT", "http://localhost:8080/anyof");
        ContentResponse postEx = sendReqAndWait("POST", "http://localhost:8080/anyof");
        ContentResponse delEx = sendReqAndWait("DELETE", "http://localhost:8080/anyof");

        // THEN the reponse is 405/404/405 respectively
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, putEx.getStatus());
        assertEquals(HttpStatus.NOT_FOUND_404, postEx.getStatus());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, delEx.getStatus());
    }

    @Test
    public void shouldConsiderMethodAndPath() throws Exception {
        easy.on(GET, "/path", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("GET path");
            }
        }).on(PUT, "/path", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("PUT path");
            }
        }).on(anyOf(POST, DELETE), "/path", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("POST or DELETE path");
            }
        }).start();

        // WHEN requests are sent out on the same path but different methods
        ContentResponse getEx = sendReqAndWait("GET", "http://localhost:8080/path");
        ContentResponse putEx = sendReqAndWait("PUT", "http://localhost:8080/path");
        ContentResponse postEx = sendReqAndWait("POST", "http://localhost:8080/path");
        ContentResponse deleteEx = sendReqAndWait("DELETE", "http://localhost:8080/path");

        // THEN the expected responses are provided
        assertEquals("GET path", getEx.getContentAsString().trim());
        assertEquals("PUT path", putEx.getContentAsString().trim());
        assertEquals("POST or DELETE path", postEx.getContentAsString().trim());
        assertEquals("POST or DELETE path", deleteEx.getContentAsString().trim());
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
                easy.on(GET, "/index" + data.i + "/" + data.p + data.j, new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.out.println("index" + data.i + " j" + data.j + data.p);
                    }
                }).on(GET, "/sub" + data.i + "/j" + data.j + "/" + data.p, new Responder() {
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
                    ContentResponse exchange1 = sendReqAndWait("GET", "http://localhost:8080/index" + data.i + "/" + data.p + data.j);
                    assertEquals(HttpStatus.OK_200, exchange1.getStatus());
                    assertEquals("index" + data.i + " j" + data.j + data.p, exchange1.getContentAsString().trim());

                    ContentResponse ex2 = sendReqAndWait("GET", "http://localhost:8080/sub" + data.i + "/j" + data.j + "/" + data.p);
                    assertEquals(HttpStatus.OK_200, ex2.getStatus());
                    assertEquals("sub" + data.i + " j" + data.j + data.p, ex2.getContentAsString().trim());
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
        easy.on(GET, "/:p1", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.send("Param " + exchange.params.get("p1"));
            }
        }).on(GET, "/hi/:name", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.send("Name " + exchange.params.get("name"));
            }
        }).start();

        // WHEN a GET request is sent out to each endpoint
        ContentResponse exchange1 = sendReqAndWait("GET", "http://localhost:8080/something");
        ContentResponse exchange2 = sendReqAndWait("GET", "http://localhost:8080/hi/john");

        // THEN the expected response is provided
        assertEquals("Param something", exchange1.getContentAsString().trim());
        assertEquals("Name john", exchange2.getContentAsString().trim());
    }

    @Test
    public void objectMappingTest() throws Exception {
        class BoolMapper implements ObjectMapper<Boolean> {
            @Override
            public String map(Boolean object) {
                return "Bool: " + object;
            }

            @Override
            public Class<Boolean> getMappedType() {
                return Boolean.class;
            }
        }

        class User {
            String name;
        }

        class UserMapper implements ObjectMapper<User> {
            @Override
            public String map(User object) {
                return "User: " + object.name;
            }

            @Override
            public Class<User> getMappedType() {
                return User.class;
            }
        }

        easy.addMapper(new BoolMapper()).addMapper(new UserMapper())
                .on(GET, "/user", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        User user = new User();
                        user.name = "Mark";
                        exchange.send(user);
                    }
                })
                .on(GET, "/bool", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.send(true);
                    }
                }).start();

        // WHEN a GET request is sent out to each endpoint
        ContentResponse exchange1 = sendReqAndWait("GET", "http://localhost:8080/user");
        ContentResponse exchange2 = sendReqAndWait("GET", "http://localhost:8080/bool");

        // THEN the expected response is provided
        assertEquals("User: Mark", exchange1.getContentAsString().trim());
        assertEquals("Bool: true", exchange2.getContentAsString().trim());
    }

    @Test
    public void removeSimplePath() throws Exception {
        // GIVEN a GET and a PUT handler for the same resource "hi"
        easy.on(GET, "/hi", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("Hi");
            }
        }).on(PUT, "/hi", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("Hi");
            }
        }).start();

        // WHEN the GET handler is removed
        boolean result = easy.remove(GET, "/hi");

        // THEN at least one handler is removed
        assertTrue(result);

        // AND requests are sent to each handler
        ContentResponse getEx = sendReqAndWait("GET", "http://localhost:8080/hi");
        ContentResponse putEx = sendReqAndWait("PUT", "http://localhost:8080/hi");

        // THEN the GET handler does not respond to requests
        assertEquals(HttpStatus.NOT_FOUND_404, getEx.getStatus());

        // AND the PUT handler still responds
        assertEquals(HttpStatus.OK_200, putEx.getStatus());
    }

    @Test
    public void removeAnyMethodHandler() throws Exception {
        // GIVEN a anyMethod handler for the resource "hi"
        easy.on(anyMethod(), "/hi", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("Hi");
            }
        }).start();

        // WHEN the GET handler is removed
        boolean result = easy.remove(GET, "/hi");

        // THEN at least one handler is removed
        assertTrue(result);

        // AND requests are sent using different methods
        ContentResponse getEx = sendReqAndWait("GET", "http://localhost:8080/hi");
        ContentResponse putEx = sendReqAndWait("PUT", "http://localhost:8080/hi");

        // THEN the no handler responds to any request
        assertEquals(HttpStatus.NOT_FOUND_404, getEx.getStatus());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, putEx.getStatus());
    }

    @Test
    public void removeNonExistingResource() throws Exception {
        // GIVEN a GET handler for the resource "hi"
        easy.on(GET, "/hi", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("Hi");
            }
        }).start();

        // WHEN the GET handler is removed for a non-existing resource
        boolean result = easy.remove(GET, "/blah");

        // THEN no handler is removed
        assertFalse(result);

        // AND the GET handler should still work
        ContentResponse getEx = sendReqAndWait("GET", "http://localhost:8080/hi");
        assertEquals(HttpStatus.OK_200, getEx.getStatus());
    }

}
