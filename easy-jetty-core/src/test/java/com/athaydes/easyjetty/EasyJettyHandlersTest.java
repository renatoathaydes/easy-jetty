package com.athaydes.easyjetty;

import com.athaydes.easyjetty.http.MethodArbiter.Method;
import com.athaydes.easyjetty.mapper.ObjectMapperGroup;
import com.athaydes.easyjetty.mapper.ObjectSerializer;
import org.boon.Maps;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.*;
import static com.athaydes.easyjetty.http.MethodArbiterFactory.*;
import static org.boon.Maps.map;
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
    public void shouldConsiderAcceptHeader() throws Exception {
        easy.on(GET, "/all", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("ALL");
            }
        }).on(GET, "/path", "application/json", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("JSON");
            }
        }).on(GET, "/path", "application/xml", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("XML");
            }
        }).on(anyOf(GET, PUT, POST), "/path", "image/jpeg", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("JPEG");
            }
        }).on(GET, "/path", "audio/mp3,audio/wmf", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("AUDIO");
            }
        }).start();

        // WHEN requests are sent out on the same path but different Accept header
        ContentResponse all1 = sendReqAndWait("GET", "http://localhost:8080/all", map("Accept", "application/json"));
        ContentResponse all2 = sendReqAndWait("GET", "http://localhost:8080/all", map("Accept", "text/*"));

        ContentResponse json = sendReqAndWait("GET", "http://localhost:8080/path", map("Accept", "application/json"));
        ContentResponse xml = sendReqAndWait("GET", "http://localhost:8080/path", map("Accept", "application/xml"));
        ContentResponse postJpeg = sendReqAndWait("POST", "http://localhost:8080/path", map("Accept", "image/jpeg"));
        ContentResponse getJpeg = sendReqAndWait("GET", "http://localhost:8080/path", map("Accept", "image/jpeg"));
        ContentResponse getAudio = sendReqAndWait("GET", "http://localhost:8080/path", map("Accept", "audio/*; q=0.2, audio/basic"));
        ContentResponse getGif = sendReqAndWait("GET", "http://localhost:8080/path", map("Accept", "image/gif"));

        // THEN the expected responses are provided
        assertEquals(HttpStatus.OK_200, all1.getStatus());
        assertEquals(HttpStatus.OK_200, all2.getStatus());
        assertEquals(HttpStatus.OK_200, json.getStatus());
        assertEquals(HttpStatus.OK_200, xml.getStatus());
        assertEquals(HttpStatus.OK_200, postJpeg.getStatus());
        assertEquals(HttpStatus.OK_200, getJpeg.getStatus());
        assertEquals(HttpStatus.OK_200, getAudio.getStatus());
        assertEquals(HttpStatus.NOT_FOUND_404, getGif.getStatus());
        assertEquals("ALL", all1.getContentAsString().trim());
        assertEquals("ALL", all2.getContentAsString().trim());
        assertEquals("JSON", json.getContentAsString().trim());
        assertEquals("XML", xml.getContentAsString().trim());
        assertEquals("JPEG", postJpeg.getContentAsString().trim());
        assertEquals("JPEG", getJpeg.getContentAsString().trim());
        assertEquals("AUDIO", getAudio.getContentAsString().trim());
    }

    @Test
    public void shouldProvideBestMatchAcceptHeader() throws Exception {
        // GIVEN a long Accept Header that uses quality of service parameters
        final String longAcceptHeader = "audio/mp3, audio/wmf; q=0.5, audio/xxx; q=0.2, " +
                "audio/zzz; q=0.75, audio/*; q=0.1";

        easy.on(GET, "/audio/1", "audio/wmf,audio/mp3", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println(exchange.acceptedContentType);
            }
        }).on(GET, "/audio/2", "audio/wmf", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println(exchange.acceptedContentType);
            }
        }).on(GET, "/audio/3", "audio/wmf, audio/zzz, audio/xxx", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println(exchange.acceptedContentType);
            }
        }).on(GET, "/audio/4", "audio/what,text/html,text/xml", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println(exchange.acceptedContentType);
            }
        }).start();

        // WHEN requests are sent out on different paths but with the same Accept header
        ContentResponse getAudio1 = sendReqAndWait("GET", "http://localhost:8080/audio/1", map("Accept", longAcceptHeader));
        ContentResponse getAudio2 = sendReqAndWait("GET", "http://localhost:8080/audio/2", map("Accept", longAcceptHeader));
        ContentResponse getAudio3 = sendReqAndWait("GET", "http://localhost:8080/audio/3", map("Accept", longAcceptHeader));
        ContentResponse getAudio4 = sendReqAndWait("GET", "http://localhost:8080/audio/4", map("Accept", longAcceptHeader));

        // THEN the expected responses are provided
        assertEquals(HttpStatus.OK_200, getAudio1.getStatus());
        assertEquals(HttpStatus.OK_200, getAudio2.getStatus());
        assertEquals(HttpStatus.OK_200, getAudio3.getStatus());
        assertEquals(HttpStatus.OK_200, getAudio4.getStatus());
        assertEquals("audio/mp3", getAudio1.getContentAsString().trim());
        assertEquals("audio/wmf", getAudio2.getContentAsString().trim());
        assertEquals("audio/zzz", getAudio3.getContentAsString().trim());
        assertEquals("audio/what", getAudio4.getContentAsString().trim());
    }

    @Test
    public void shouldSetAcceptHeaderAutomaticallyIfKnown() throws Exception {
        // GIVEN
        final String longAcceptHeader = "audio/mp3, audio/wmf; q=0.5, audio/xxx; q=0.2, " +
                "audio/zzz; q=0.75, audio/*; q=0.1";

        easy.on(GET, "/audio/1", "audio/wmf,audio/mp3", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println(exchange.acceptedContentType);
            }
        }).on(GET, "/audio/2", "audio/wmf", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println(exchange.acceptedContentType);
            }
        }).on(GET, "/audio/3", "audio/wmf, audio/zzz, audio/xxx", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println(exchange.acceptedContentType);
            }
        }).on(GET, "/audio/4", "audio/what,text/html+xml", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println(exchange.acceptedContentType);
            }
        }).start();

        // WHEN requests are sent out on different paths but with the same Accept header
        ContentResponse getAudio1 = sendReqAndWait("GET", "http://localhost:8080/audio/1", map("Accept", longAcceptHeader));
        ContentResponse getAudio2 = sendReqAndWait("GET", "http://localhost:8080/audio/2", map("Accept", longAcceptHeader));
        ContentResponse getAudio3 = sendReqAndWait("GET", "http://localhost:8080/audio/3", map("Accept", longAcceptHeader));
        ContentResponse getAudio4 = sendReqAndWait("GET", "http://localhost:8080/audio/4", map("Accept", longAcceptHeader));

        // THEN the expected Content-Type header is set
        assertEquals("audio/mp3", getAudio1.getHeaders().get("Content-Type"));
        assertEquals("audio/wmf", getAudio2.getHeaders().get("Content-Type"));
        assertEquals("audio/zzz", getAudio3.getHeaders().get("Content-Type"));
        assertEquals("audio/what", getAudio4.getHeaders().get("Content-Type"));
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
        class BoolMapper extends ObjectSerializer<Boolean> {
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

        class UserMapper extends ObjectSerializer<User> {
            @Override
            public String map(User object) {
                return "User: " + object.name;
            }

            @Override
            public Class<User> getMappedType() {
                return User.class;
            }
        }

        easy.withMapperGroup(new ObjectMapperGroup(true, true)
                .withMappers(new BoolMapper(), new UserMapper()))
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
    public void canReceiveFormAsMap() throws Exception {
        easy.on(POST, "/my-form", "application/x-www-form-urlencoded", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                Map body = exchange.receiveAs(Map.class);
                exchange.send(body);
            }
        }).start();

        // WHEN a form is POSTed to the server
        ContentResponse exchange1 = sendReqAndWait("POST", "http://localhost:8080/my-form",
                Maps.map(HttpHeader.CONTENT_TYPE.asString(), "application/x-www-form-urlencoded"),
                "key1=value1&k2=v2");

        // THEN the expected response is provided
        assertEquals(Maps.map("key1", "value1", "k2", "v2").toString(),
                exchange1.getContentAsString().trim());
    }

    @Test
    public void primitiveMapperTest() throws Exception {
        easy.on(POST, "/integer", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                int body = exchange.receiveAs(Integer.class);
                exchange.send(body);
            }
        }).on(POST, "/float", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                float body = exchange.receiveAs(Float.class);
                exchange.send(body);
            }
        }).on(POST, "/double", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                double body = exchange.receiveAs(Double.class);
                exchange.send(body);
            }
        }).on(POST, "/long", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                long body = exchange.receiveAs(Long.class);
                exchange.send(body);
            }
        }).on(POST, "/byte", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                byte body = exchange.receiveAs(Byte.class);
                exchange.send(body);
            }
        }).on(POST, "/char", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                char body = exchange.receiveAs(Character.class);
                exchange.send(body);
            }
        }).start();

        // WHEN a message with each primitive type is POSTed to the server
        ContentResponse intExchange = sendReqAndWait("POST", "http://localhost:8080/integer",
                Collections.<String, String>emptyMap(), "42");
        ContentResponse floatExchange = sendReqAndWait("POST", "http://localhost:8080/float",
                Collections.<String, String>emptyMap(), "42.4");
        ContentResponse doubleExchange = sendReqAndWait("POST", "http://localhost:8080/double",
                Collections.<String, String>emptyMap(), "0.42");
        ContentResponse longExchange = sendReqAndWait("POST", "http://localhost:8080/long",
                Collections.<String, String>emptyMap(), "43");
        ContentResponse byteExchange = sendReqAndWait("POST", "http://localhost:8080/byte",
                Collections.<String, String>emptyMap(), "44");
        ContentResponse charExchange = sendReqAndWait("POST", "http://localhost:8080/char",
                Collections.<String, String>emptyMap(), "c");

        // THEN the expected responses are provided
        assertEquals("42", intExchange.getContentAsString().trim());
        assertEquals("42.4", floatExchange.getContentAsString().trim());
        assertEquals("0.42", doubleExchange.getContentAsString().trim());
        assertEquals("43", longExchange.getContentAsString().trim());
        assertEquals("44", byteExchange.getContentAsString().trim());
        assertEquals("c", charExchange.getContentAsString().trim());
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
