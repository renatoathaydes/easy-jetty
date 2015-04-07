package com.athaydes.easyjetty;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class EasyJettyTest {

    final EasyJetty easy = new EasyJetty();
    final HttpClient client;

    public EasyJettyTest() {
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(EasyJettyBasicTest.CACERTS);
        sslContextFactory.setKeyStorePassword(EasyJettyBasicTest.KEYPASS);
        sslContextFactory.setKeyManagerPassword(EasyJettyBasicTest.MANAGER_PASS);
        client = new HttpClient(sslContextFactory);
    }

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
        return sendReqAndWait(method, url, headers, null);
    }

    public ContentResponse sendReqAndWait(String method, String url, Map<String, String> headers,
                                          String payload) throws Exception {
        Request request = client.newRequest(url).method(method);
        if (payload != null) {
            // this needs to be set for Jetty's Servlet to even check the payload size!
            request.header(HttpHeader.CONTENT_TYPE, MimeTypes.Type.FORM_ENCODED.asString());
            request.content(new StringContentProvider(payload));
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        return request.send();
    }

    public static String randomPayloadOfSize(int size) {
        List<Character> chars = new ArrayList<>(Chars.unicodeChars());
        Collections.shuffle(chars);
        StringBuffer buffer = new StringBuffer(size);
        for (int i = 0; i < size; i++) {
            int index = i % chars.size();
            buffer.append(chars.get(index).charValue());
        }
        return buffer.toString();
    }

    static class Chars {

        static List<Character> unicodeChars() {
            char minChar = 32;
            char maxChar = 126;
            List<Character> result = new ArrayList<>(1 + maxChar - minChar);
            for (char c = minChar; c <= maxChar; c++) {
                result.add(c);
            }
            return result;
        }
    }

}
