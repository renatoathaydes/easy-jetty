package com.athaydes.easyjetty.websocket.handler;

import com.athaydes.easyjetty.EasyJetty;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

/**
 * User provided WebSocket Message handler.
 * Can be implemented with a Java 8 lambda.
 */
public interface TextMessageHandler {

    public static final TextMessageHandler NO_OP = new NoOp();

    public static class MessageExchange extends ConnectionStartedHandler.ConnectionExchange {
        public final String message;

        public MessageExchange(EasyJetty easyJetty, Session session, String message) {
            super(easyJetty, session);
            this.message = message;
        }
    }

    static class NoOp implements TextMessageHandler {
        @Override
        public void respond(MessageExchange exchange) throws IOException {
        }
    }

    /**
     * Handle a WebSocket message.
     *
     * @param exchange the message exchange
     * @throws java.io.IOException if there's a problem in communication
     */
    public abstract void respond(MessageExchange exchange)
            throws IOException;

}
