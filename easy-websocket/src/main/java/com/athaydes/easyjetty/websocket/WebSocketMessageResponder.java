package com.athaydes.easyjetty.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

/**
 *
 */
public interface WebSocketMessageResponder {

    public static class WebSocketExchange extends ConnectionStarter.ConnectionExchange {
        public final String message;

        public WebSocketExchange(Session session, String message) {
            super(session);
            this.message = message;
        }
    }

    public abstract void respond(WebSocketExchange exchange)
            throws IOException;

}
