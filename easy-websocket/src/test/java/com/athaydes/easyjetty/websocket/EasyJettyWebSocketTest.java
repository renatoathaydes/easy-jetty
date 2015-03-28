package com.athaydes.easyjetty.websocket;

import com.athaydes.easyjetty.EasyJetty;
import com.athaydes.easyjetty.websocket.handler.ConnectionClosedHandler;
import com.athaydes.easyjetty.websocket.handler.ConnectionStartedHandler;
import com.athaydes.easyjetty.websocket.handler.WebSocketErrorHandler;
import com.athaydes.easyjetty.websocket.handler.TextMessageHandler;
import com.google.code.tempusfugit.temporal.Condition;
import com.google.code.tempusfugit.temporal.Duration;
import com.google.code.tempusfugit.temporal.Timeout;
import com.google.code.tempusfugit.temporal.WaitFor;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class EasyJettyWebSocketTest {

    private EasyJetty jetty = new EasyJetty();

    @After
    public void cleanup() {
        jetty.stop();
    }

    @Test
    public void sendAndReceiveMessagesAndHandleErrors() throws Exception {
        final List<String> errors = new ArrayList<>();
        final List<String> serverMessages = new ArrayList<>();
        final AtomicBoolean done = new AtomicBoolean(false);

        jetty.withExtension(new EasyJettyWebSocket()
                .onText("/chat/*", new ConnectionStartedHandler() {
                    @Override
                    public void onConnect(ConnectionExchange exchange) throws IOException {
                        exchange.send("Welcome!");
                    }
                }, new TextMessageHandler() {
                    @Override
                    public void respond(MessageExchange exchange) throws IOException {
                        serverMessages.add(exchange.message);
                        if (exchange.message.equals("Thanks")) {
                            throw new RuntimeException("PROBLEM!!");
                        }
                    }
                }, new WebSocketErrorHandler() {
                    @Override
                    public void onError(ErrorExchange error) throws IOException {
                        errors.add("Some error!! " + error.error);
                        error.send("Just handled some error");
                    }
                }, new ConnectionClosedHandler() {
                    @Override
                    public void onClose(CloseExchange exchange) {
                        System.out.println("Closed the connection");
                    }
                })).start();

        final List<String> clientMessages = new ArrayList<>();

        final WebSocketClient client = getWebSocketClient("ws://localhost:8080/chat/hello",
                new WebSocketAdapter() {
                    @Override
                    public void onWebSocketConnect(Session sess) {
                        try {
                            super.onWebSocketConnect(sess);
                            sess.getRemote().sendString("Hi");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onWebSocketText(String message) {
                        clientMessages.add(message);
                        if (message.equals("Welcome!")) {
                            try {
                                this.getSession().getRemote().sendString("Thanks");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            done.set(true);
                        }
                    }
                });

        WaitFor.waitOrTimeout(new Condition() {
            @Override
            public boolean isSatisfied() {
                return done.get();
            }
        }, Timeout.timeout(Duration.seconds(2)));

        client.stop();

        assertThat(serverMessages, is(asList("Hi", "Thanks")));
        assertThat(clientMessages, is(asList("Welcome!", "Just handled some error")));
        assertThat(errors, is(asList("Some error!! java.lang.RuntimeException: PROBLEM!!")));
    }

    private static WebSocketClient getWebSocketClient(String destUri, WebSocketAdapter adapter)
            throws Exception {
        final WebSocketClient client = new WebSocketClient();
        client.start();
        client.connect(adapter, new URI(destUri));
        return client;
    }

}