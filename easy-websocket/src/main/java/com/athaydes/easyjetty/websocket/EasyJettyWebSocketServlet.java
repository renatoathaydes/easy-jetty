package com.athaydes.easyjetty.websocket;

import com.athaydes.easyjetty.EasyJetty;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

class EasyJettyWebSocketServlet extends WebSocketServlet {

    private final EasyJetty easyJetty;
    private final UserEndpoint userEndpoint;

    EasyJettyWebSocketServlet(EasyJetty easyJetty, UserEndpoint userEndpoint) {
        this.easyJetty = easyJetty;
        this.userEndpoint = userEndpoint;
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.setCreator(new WebSocketServerFactory() {
            @Override
            public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
                return new EasyJettyWebSocketListener(easyJetty, userEndpoint);
            }
        });
    }


}
