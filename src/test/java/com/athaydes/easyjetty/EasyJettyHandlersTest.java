package com.athaydes.easyjetty;

import com.athaydes.easyjetty.http.MethodArbiter.Method;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpExchange;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.anyMethod;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
    public void resourcesLocationIsRespected() throws Exception {
        easy.resourcesLocation("src/").start();
        ContentExchange exchange = sendReqAndWait("GET", "http://localhost:8080/");
        assertEquals(HttpExchange.STATUS_COMPLETED, exchange.waitForDone());
        assertThat(exchange.getResponseContent(), containsString("test"));
    }

    @Test
    public void simpleContextPathIsRespected() throws Exception {
        easy.resourcesLocation("src/").contextPath("/ctx").start();
        ContentExchange exchange = sendReqAndWait("GET", "http://localhost:8080/ctx/");
        assertEquals(HttpExchange.STATUS_COMPLETED, exchange.waitForDone());
        assertThat(exchange.getResponseContent(), containsString("test"));
    }

}
