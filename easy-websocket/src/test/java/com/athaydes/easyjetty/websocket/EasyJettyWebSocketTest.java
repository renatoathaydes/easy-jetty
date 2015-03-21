package com.athaydes.easyjetty.websocket;

import com.athaydes.easyjetty.EasyJetty;
import org.junit.Test;

import java.io.IOException;

/**
 *
 */
public class EasyJettyWebSocketTest {

    private EasyJetty jetty = new EasyJetty();


    @Test
    public void easy() throws Exception {
        jetty.withExtension(new EasyJettyWebSocket()
                .on("/chat", new ConnectionStarter() {
                    @Override
                    public void onConnect(ConnectionExchange exchange) throws IOException {
                        exchange.send("Welcome!");
                    }
                }, new WebSocketMessageResponder() {
                    @Override
                    public void respond(WebSocketExchange exchange) throws IOException {
                        exchange.send("hello " + exchange.message);
                    }
                })).start();

        // FIXME currently just manually testing
        Thread.sleep(60_000L);
        jetty.stop();
    }

}
