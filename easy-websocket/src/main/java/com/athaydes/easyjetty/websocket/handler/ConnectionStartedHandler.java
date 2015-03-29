package com.athaydes.easyjetty.websocket.handler;

import com.athaydes.easyjetty.EasyJetty;
import com.athaydes.easyjetty.mapper.ObjectMapperGroup;
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
        private final EasyJetty easyJetty;

        public ConnectionExchange(EasyJetty easyJetty, Session session) {
            this.easyJetty = easyJetty;
            this.session = session;
        }

        public void send(Object object) throws IOException {
            ObjectMapperGroup omGroup = easyJetty.getObjectMapperGroup();
            session.getRemote().sendString(omGroup.map(object));
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
