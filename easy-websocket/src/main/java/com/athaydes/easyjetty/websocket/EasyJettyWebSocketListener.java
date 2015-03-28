package com.athaydes.easyjetty.websocket;

import com.athaydes.easyjetty.websocket.handler.BinaryMessageHandler.BinaryMessageExchange;
import com.athaydes.easyjetty.websocket.handler.ConnectionClosedHandler.CloseExchange;
import com.athaydes.easyjetty.websocket.handler.ConnectionStartedHandler.ConnectionExchange;
import com.athaydes.easyjetty.websocket.handler.WebSocketErrorHandler.ErrorExchange;
import com.athaydes.easyjetty.websocket.handler.TextMessageHandler.MessageExchange;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import java.io.IOException;

class EasyJettyWebSocketListener implements WebSocketListener {

    private final UserEndpoint userEndpoint;
    private volatile Session session;

    public EasyJettyWebSocketListener(UserEndpoint userEndpoint) {
        this.userEndpoint = userEndpoint;
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        final Session currentSession = session;
        try {
            if (currentSession != null) {
                userEndpoint.binaryResponder.handleBinaryMessage(
                        new BinaryMessageExchange(session, payload, offset, len));
            }
        } catch (IOException e) {
            onWebSocketError(e);
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        userEndpoint.connectionCloser.onClose(new CloseExchange(session, statusCode, reason));
        this.session = null;
    }

    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        try {
            userEndpoint.connectionStarter.onConnect(new ConnectionExchange(session));
        } catch (IOException e) {
            onWebSocketError(e);
        }
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        try {
            userEndpoint.errorHandler.onError(new ErrorExchange(session, cause));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketText(String message) {
        final Session currentSession = session;
        try {
            if (currentSession != null) {
                userEndpoint.responder.respond(new MessageExchange(currentSession, message));
            }
        } catch (IOException e) {
            onWebSocketError(e);
        }
    }

}
