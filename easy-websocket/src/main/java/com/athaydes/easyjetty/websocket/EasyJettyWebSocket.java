package com.athaydes.easyjetty.websocket;

import com.athaydes.easyjetty.EasyJetty;
import com.athaydes.easyjetty.extension.EasyJettyEvent;
import com.athaydes.easyjetty.extension.EasyJettyExtension;
import com.athaydes.easyjetty.extension.event.BeforeStartEvent;
import com.athaydes.easyjetty.extension.event.BeforeStopEvent;
import com.athaydes.easyjetty.extension.event.ExtensionAddedEvent;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

/**
 * WebSocket extension for EasyJetty.
 */
public class EasyJettyWebSocket implements EasyJettyExtension {

    private Class<? extends Endpoint> userEndpoint;
    private String usertPath;

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
        try {
            ServerContainer container = WebSocketServerContainerInitializer.configureContext(easyJetty.getServletHandler());
            ServerEndpointConfig endpointConfig = ServerEndpointConfig.Builder.create(userEndpoint, usertPath).build();
            container.addEndpoint(endpointConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public EasyJettyWebSocket on(String path, Class<? extends Endpoint> endpoint) {
        this.userEndpoint = endpoint;
        this.usertPath = path;
        return this;
    }

}
