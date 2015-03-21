package com.athaydes.easyjetty.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

/**
 *
 */
public interface ConnectionStarter {

    public static class ConnectionExchange {
        public final Session session;

        public ConnectionExchange(Session session) {
            this.session = session;
        }

        public void send(Object object) throws IOException {
            session.getRemote().sendString(object.toString());
        }
    }

    static final class Default implements ConnectionStarter {
        @Override
        public void onConnect(ConnectionExchange exchange) {
        }
    }

    public void onConnect(ConnectionExchange exchange)
            throws IOException;

}
