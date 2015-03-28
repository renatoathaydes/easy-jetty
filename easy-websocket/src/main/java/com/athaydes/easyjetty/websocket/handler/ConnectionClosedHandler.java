package com.athaydes.easyjetty.websocket.handler;

import org.eclipse.jetty.websocket.api.Session;

/**
 * User provided handler for WebSocket connection closing.
 * Can be implemented with a Java 8 lambda.
 */
public interface ConnectionClosedHandler {

    public static final ConnectionClosedHandler NO_OP = new NoOp();

    public static class CloseExchange extends ConnectionStartedHandler.ConnectionExchange {

        public final String reason;
        public final int statusCode;

        public CloseExchange(Session session, int statusCode, String reason) {
            super(session);
            this.reason = reason;
            this.statusCode = statusCode;
        }
    }

    static class NoOp implements ConnectionClosedHandler {
        @Override
        public void onClose(CloseExchange exchange) {
        }
    }

    /**
     * Handle the WebSocket connection closing.
     *
     * @param exchange the CloseExchange
     * @throws java.io.IOException if there's a problem in communication
     */
    public void onClose(CloseExchange exchange);

}
