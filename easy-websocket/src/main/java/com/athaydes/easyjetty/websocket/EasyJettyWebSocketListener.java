package com.athaydes.easyjetty.websocket;

import com.athaydes.easyjetty.websocket.WebSocketMessageResponder.WebSocketExchange;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import java.io.IOException;

/**
 *
 */
class EasyJettyWebSocketListener implements WebSocketListener {

    private final EasyJettyWebSocket.UserEndpoint userEndpoint;
    private volatile Session session;

    public EasyJettyWebSocketListener(EasyJettyWebSocket.UserEndpoint userEndpoint) {
        this.userEndpoint = userEndpoint;
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        throw new UnsupportedOperationException("onWebSocketBinary");
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        System.out.println("Closed connection: [" + statusCode + "] " + reason);
        this.session = null;
    }

    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        try {
            userEndpoint.connectionStarter.onConnect(new ConnectionStarter.ConnectionExchange(session));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketError(Throwable cause) {

    }

    @Override
    public void onWebSocketText(String message) {
        final Session currentSession = session;
        try {
            if (currentSession != null) {
                userEndpoint.responder.respond(new WebSocketExchange(currentSession, message));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
