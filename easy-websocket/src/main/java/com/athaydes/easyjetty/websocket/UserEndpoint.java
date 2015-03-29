package com.athaydes.easyjetty.websocket;

import com.athaydes.easyjetty.websocket.handler.*;

class UserEndpoint {

    final String path;
    final ConnectionStartedHandler connectionStarter;
    final TextMessageHandler responder;
    final BinaryMessageHandler binaryResponder;
    final WebSocketErrorHandler errorHandler;
    final ConnectionClosedHandler connectionCloser;

    UserEndpoint(String path,
                 ConnectionStartedHandler connectionStarter,
                 TextMessageHandler responder,
                 BinaryMessageHandler binaryResponder,
                 WebSocketErrorHandler errorHandler,
                 ConnectionClosedHandler connectionCloser) {
        this.path = path;
        this.connectionStarter = connectionStarter;
        this.responder = responder;
        this.binaryResponder = binaryResponder;
        this.errorHandler = errorHandler;
        this.connectionCloser = connectionCloser;
    }

}
