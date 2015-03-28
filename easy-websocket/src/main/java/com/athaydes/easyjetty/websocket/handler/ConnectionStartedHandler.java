package com.athaydes.easyjetty.websocket.handler;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

/**
 * User provided connection starter.
 * Can be implemented with a Java 8 lambda.
 */
public interface ConnectionStartedHandler {

    static final ConnectionStartedHandler NO_OP = new NoOp();

    public static class ConnectionExchange {
        public final Session session;

        public ConnectionExchange(Session session) {
            this.session = session;
        }

        public void send(Object object) throws IOException {
            session.getRemote().sendString(object.toString());
        }
    }

    static final class NoOp implements ConnectionStartedHandler {
        @Override
        public void onConnect(ConnectionExchange exchange) {
        }
    }

    /**
     * Handle a WebSocket connection start.
     *
     * @param exchange the connection exchange
     * @throws java.io.IOException if there's a problem in communication
     */
    public void onConnect(ConnectionExchange exchange)
            throws IOException;

}
