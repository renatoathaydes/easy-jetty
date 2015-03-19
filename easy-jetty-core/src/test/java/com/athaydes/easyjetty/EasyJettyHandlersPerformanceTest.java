package com.athaydes.easyjetty;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.GET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EasyJettyHandlersPerformanceTest extends EasyJettyTest {

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


}
