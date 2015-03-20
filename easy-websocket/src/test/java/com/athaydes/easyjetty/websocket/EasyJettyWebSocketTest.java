package com.athaydes.easyjetty.websocket;

import com.athaydes.easyjetty.EasyJetty;
import org.junit.Test;

import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;

/**
 *
 */
public class EasyJettyWebSocketTest {

    private EasyJetty jetty = new EasyJetty();

    public static class MyEndpoint extends Endpoint {
        @Override
        public void onOpen(Session session, EndpointConfig config) {
            try {
                session.getBasicRemote().sendText("Hello there!!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void easy() throws Exception {
        jetty.withExtension(new EasyJettyWebSocket()
                .on("/chat", MyEndpoint.class))
                .start();

        // FIXME currently just manually testing
        Thread.sleep(60_000L);
        jetty.stop();
    }

}
