package com.athaydes.easyjetty;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class EasyJettyFilterTest extends EasyJettyTest {

    @Test
    public void shouldStopProcessingWhenFilterDoesNotAllow() throws Exception {
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

}
