package com.athaydes.easyjetty;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class EasyJettyBasicTest extends EasyJettyTest {


    @Test
    public void serverStarts() {
        easy.start();
        assertTrue(easy.isRunning());
        easy.stop();
    }

    @Test
    public void canSelectPort() throws Exception {
        easy.resourcesLocation("src/").port(8088).start();
        ContentResponse response1 = sendReqAndWait("GET", "http://localhost:8088/");
        assertEquals(HttpStatus.OK_200, response1.getStatus());

        easy.stop();

        easy.resourcesLocation("src/").port(8085).start();
        ContentResponse response2 = sendReqAndWait("GET", "http://localhost:8085/");
        assertEquals(HttpStatus.OK_200, response2.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void cannotChangePortWhileServerIsRunning() {
        easy.port(8085).start().port(8088);
    }

    @Test
    public void resourcesLocationIsRespected() throws Exception {
        easy.resourcesLocation("src/").start();
        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/");
        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertThat(response.getContentAsString(), containsString("test"));
    }

    @Test
    public void simpleContextPathIsRespected() throws Exception {
        easy.resourcesLocation("src/").contextPath("/ctx").start();
        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/ctx/");
        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertThat(response.getContentAsString(), containsString("test"));
    }

    @Test
    public void directoryListingOptionIsRespected() throws Exception {
        easy.resourcesLocation("src/").disableDirectoryListing().start();
        ContentResponse response = sendReqAndWait("GET", "http://localhost:8080/");
        assertEquals(HttpStatus.FORBIDDEN_403, response.getStatus());
    }

}
