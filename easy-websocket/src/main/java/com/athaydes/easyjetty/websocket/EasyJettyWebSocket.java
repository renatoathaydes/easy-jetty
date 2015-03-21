package com.athaydes.easyjetty.websocket;

import com.athaydes.easyjetty.EasyJetty;
import com.athaydes.easyjetty.extension.EasyJettyEvent;
import com.athaydes.easyjetty.extension.EasyJettyExtension;
import com.athaydes.easyjetty.extension.event.BeforeStartEvent;
import com.athaydes.easyjetty.extension.event.BeforeStopEvent;
import com.athaydes.easyjetty.extension.event.ExtensionAddedEvent;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * WebSocket extension for EasyJetty.
 */
public class EasyJettyWebSocket implements EasyJettyExtension {

    static class UserEndpoint {
        final String path;
        final ConnectionStarter connectionStarter;
        final WebSocketMessageResponder responder;

        public UserEndpoint(String path, ConnectionStarter connectionStarter, WebSocketMessageResponder responder) {
            this.path = path;
            this.connectionStarter = connectionStarter;
            this.responder = responder;
        }
    }

    private final List<UserEndpoint> endpoints = new ArrayList<>();
    private final ConnectionStarter noOpConnectionStarter = new ConnectionStarter.Default();

    @Override
    public void handleEvent(EasyJettyEvent event) {
        if (event instanceof ExtensionAddedEvent) {
            handleAddedEvent((ExtensionAddedEvent) event);
        } else if (event instanceof BeforeStartEvent) {
            config(event.getEasyJetty());
            start();
        } else if (event instanceof BeforeStopEvent) {
            stop();
        }
    }

    private void handleAddedEvent(ExtensionAddedEvent event) {
        if (event.getExtension() == this) {
            EasyJetty jetty = event.getEasyJetty();
            if (jetty.isRunning()) {
                jetty.stop(false);
                config(jetty);
                start();
                jetty.start();
            }
        }
    }

    private void start() {

    }

    private void stop() {

    }

    private void config(EasyJetty easyJetty) {
        for (UserEndpoint endpoint : endpoints) {
            easyJetty.getServletHandler().addServlet(
                    new ServletHolder(new EasyJettyWebSocketServlet(endpoint)),
                    endpoint.path);
        }
    }

    public EasyJettyWebSocket on(String path, WebSocketMessageResponder responder) {
        endpoints.add(new UserEndpoint(path, noOpConnectionStarter, responder));
        return this;
    }

    public EasyJettyWebSocket on(String path, ConnectionStarter connectionStarter, WebSocketMessageResponder responder) {
        endpoints.add(new UserEndpoint(path, connectionStarter, responder));
        return this;
    }

}
