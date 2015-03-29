package com.athaydes.easyjetty.websocket.handler;

import com.athaydes.easyjetty.EasyJetty;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

/**
 * User provided WebSocket error handler.
 * Can be implemented with a Java 8 lambda.
 */
public interface WebSocketErrorHandler {

    public static WebSocketErrorHandler NO_OP = new NoOp();

    public static class ErrorExchange extends ConnectionStartedHandler.ConnectionExchange {
        public final Throwable error;

        public ErrorExchange(EasyJetty easyJetty, Session session, Throwable error) {
            super(easyJetty, session);
            this.error = error;
        }
    }

    static class NoOp implements WebSocketErrorHandler {
        @Override
        public void onError(ErrorExchange error) {
        }
    }

    /**
     * Handle WebSocket error.
     *
     * @param error the error exchange
     * @throws java.io.IOException if there's a problem in communication
     */
    public void onError(ErrorExchange error) throws IOException;

}
