package com.athaydes.easyjetty;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.After;
import org.junit.Before;

import java.util.Collections;
import java.util.Map;

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
        return sendReqAndWait(method, url, Collections.<String, String>emptyMap());
    }

    public ContentResponse sendReqAndWait(String method, String url, Map<String, String> headers) throws Exception {
        Request request = client.newRequest(url).method(method);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        return request.send();
    }

}
