package com.athaydes.easyjetty.websocket;

import com.athaydes.easyjetty.EasyJetty;
import com.athaydes.easyjetty.extension.EasyJettyEvent;
import com.athaydes.easyjetty.extension.EasyJettyExtension;
import com.athaydes.easyjetty.extension.event.BeforeStartEvent;
import com.athaydes.easyjetty.extension.event.ExtensionAddedEvent;
import com.athaydes.easyjetty.websocket.handler.*;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * WebSocket extension for EasyJetty.
 */
public class EasyJettyWebSocket implements EasyJettyExtension {

    private final List<UserEndpoint> endpoints = new ArrayList<>();

    @Override
    public void handleEvent(EasyJettyEvent event) {
        if (event instanceof ExtensionAddedEvent) {
            handleAddedEvent((ExtensionAddedEvent) event);
        } else if (event instanceof BeforeStartEvent) {
            config(event.getEasyJetty());
        }
    }

    private void handleAddedEvent(ExtensionAddedEvent event) {
        if (event.getExtension() == this) {
            EasyJetty jetty = event.getEasyJetty();
            if (jetty.isRunning()) {
                jetty.stop(false);
                config(jetty);
                jetty.start();
            }
        }
    }

    private void config(EasyJetty easyJetty) {
        for (UserEndpoint endpoint : endpoints) {
            easyJetty.getServletHandler().addServlet(
                    new ServletHolder(new EasyJettyWebSocketServlet(easyJetty, endpoint)),
                    endpoint.path);
        }
    }

    public EasyJettyWebSocket onText(String path, TextMessageHandler responder) {
        endpoints.add(new UserEndpoint(path, ConnectionStartedHandler.NO_OP, responder,
                BinaryMessageHandler.NO_OP, WebSocketErrorHandler.NO_OP,
                ConnectionClosedHandler.NO_OP));
        return this;
    }

    public EasyJettyWebSocket onBinary(String path, BinaryMessageHandler responder) {
        endpoints.add(new UserEndpoint(path, ConnectionStartedHandler.NO_OP, TextMessageHandler.NO_OP,
                responder, WebSocketErrorHandler.NO_OP, ConnectionClosedHandler.NO_OP));
        return this;
    }

    public EasyJettyWebSocket onText(String path, ConnectionStartedHandler connectionStarter,
                                     TextMessageHandler responder) {
        endpoints.add(new UserEndpoint(path, connectionStarter, responder, BinaryMessageHandler.NO_OP,
                WebSocketErrorHandler.NO_OP, ConnectionClosedHandler.NO_OP));
        return this;
    }

    public EasyJettyWebSocket onBinary(String path, ConnectionStartedHandler connectionStarter,
                                       BinaryMessageHandler responder) {
        endpoints.add(new UserEndpoint(path, connectionStarter, TextMessageHandler.NO_OP, responder,
                WebSocketErrorHandler.NO_OP, ConnectionClosedHandler.NO_OP));
        return this;
    }

    public EasyJettyWebSocket onText(String path, ConnectionStartedHandler connectionStarter,
                                     TextMessageHandler responder, WebSocketErrorHandler errorHandler) {
        endpoints.add(new UserEndpoint(path, connectionStarter, responder, BinaryMessageHandler.NO_OP,
                errorHandler, ConnectionClosedHandler.NO_OP));
        return this;
    }

    public EasyJettyWebSocket onBinary(String path, ConnectionStartedHandler connectionStarter,
                                       BinaryMessageHandler responder, WebSocketErrorHandler errorHandler) {
        endpoints.add(new UserEndpoint(path, connectionStarter, TextMessageHandler.NO_OP, responder,
                errorHandler, ConnectionClosedHandler.NO_OP));
        return this;
    }

    public EasyJettyWebSocket onText(String path, ConnectionStartedHandler connectionStarter,
                                     TextMessageHandler responder, WebSocketErrorHandler errorHandler,
                                     ConnectionClosedHandler connectionCloser) {
        endpoints.add(new UserEndpoint(path, connectionStarter, responder, BinaryMessageHandler.NO_OP,
                errorHandler, connectionCloser));
        return this;
    }

    public EasyJettyWebSocket onBinary(String path, ConnectionStartedHandler connectionStarter,
                                       BinaryMessageHandler responder, WebSocketErrorHandler errorHandler,
                                       ConnectionClosedHandler connectionCloser) {
        endpoints.add(new UserEndpoint(path, connectionStarter, TextMessageHandler.NO_OP, responder,
                errorHandler, connectionCloser));
        return this;
    }

}
