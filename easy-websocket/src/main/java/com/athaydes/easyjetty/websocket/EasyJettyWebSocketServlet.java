package com.athaydes.easyjetty.websocket;

import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 *
 */
public class EasyJettyWebSocketServlet extends WebSocketServlet {

    private final EasyJettyWebSocket.UserEndpoint userEndpoint;

    public EasyJettyWebSocketServlet(EasyJettyWebSocket.UserEndpoint userEndpoint) {
        this.userEndpoint = userEndpoint;
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.setCreator(new WebSocketServerFactory() {
            @Override
            public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
                return new EasyJettyWebSocketListener(userEndpoint);
            }
        });
    }



}
