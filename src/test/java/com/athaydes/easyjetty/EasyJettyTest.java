package com.athaydes.easyjetty;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.After;
import org.junit.Before;

public abstract class EasyJettyTest {

    final EasyJetty easy = new EasyJetty();
    final HttpClient client = new HttpClient();

    @Before
    public void setup() throws Exception {
        client.setConnectTimeout(1500L);
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

    public ContentResponse sendReqAndWait(String method, String url) throws Exception {
        return client.newRequest(url).method(method).send();
    }

}
