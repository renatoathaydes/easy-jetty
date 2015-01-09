package com.athaydes.easyjetty;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.junit.After;
import org.junit.Before;

public abstract class EasyJettyTest {

    final EasyJetty easy = new EasyJetty();
    final HttpClient client = new HttpClient();

    @Before
    public void setup() throws Exception {
        client.setTimeout(1500L);
        client.start();
    }

    @After
    public void cleanup() {
        try {
            easy.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ContentExchange sendReqAndWait(String method, String url) throws Exception {
        ContentExchange exchange = new ContentExchange(false);
        exchange.setURL(url);

        exchange.setMethod(method);
        client.send(exchange);
        exchange.waitForDone();

        return exchange;
    }

}
