package com.athaydes.easyjetty.websocket;

import com.athaydes.easyjetty.EasyJetty;
import org.junit.Test;

/**
 *
 */
public class EasyWebSocketTest {

    private final EasyJetty jetty;

    public EasyWebSocketTest(EasyJetty jetty) {
        this.jetty = jetty;
    }

    @Test
    public void easy() {
        jetty.getServer();
    }

}
