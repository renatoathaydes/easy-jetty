package com.athaydes.easyjetty.websocket.handler;

import com.athaydes.easyjetty.EasyJetty;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

/**
 * User provided handler for binary messages.
 * Can be implemented with a Java 8 lambda.
 */
public interface BinaryMessageHandler {

    public static final BinaryMessageHandler NO_OP = new NoOp();

    public static class BinaryMessageExchange extends ConnectionStartedHandler.ConnectionExchange {

        /**
         * message as a raw bytes array
         */
        public final byte[] payload;

        /**
         * the offset in the array where the data starts
         */
        public final int offset;

        /**
         * the length of the received data in the array
         */
        public final int len;

        public BinaryMessageExchange(EasyJetty easyJetty, Session session, byte[] payload, int offset, int len) {
            super(easyJetty, session);
            this.payload = payload;
            this.offset = offset;
            this.len = len;
        }
    }

    static class NoOp implements BinaryMessageHandler {
        @Override
        public void handleBinaryMessage(BinaryMessageExchange exchange) {
        }
    }

    /**
     * Handle a WebSocket binary message.
     *
     * @param exchange the binary message exchange
     * @throws java.io.IOException if there's a problem in communication
     */
    public void handleBinaryMessage(BinaryMessageExchange exchange)
            throws IOException;

}
