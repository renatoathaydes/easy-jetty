package sample;

import com.athaydes.easyjetty.EasyJetty;
import com.athaydes.easyjetty.Responder;
import com.athaydes.easyjetty.SSLConfig;
import com.athaydes.easyjetty.http.MethodArbiter;

import java.io.IOException;

import static com.athaydes.easyjetty.EasyJettyBasicTest.*;

/**
 * This sample shows how you can enable HTTPS-only.
 * Notice that the default port when using HTTPS is 8443.
 * The HTTP port, 8080, will not have any connectors and therefore will not respond to any request.
 * To keep HTTP traffic, instead of using sslOnly(), use ssl().
 */
public class UsingSSL {

    public static void main(String[] args) {
        new EasyJetty()
                .sslOnly(new SSLConfig("ssl/renatokeystore", KEYPASS, MANAGER_PASS))
                .resourcesLocation("src/")
                .on(MethodArbiter.Method.GET, "/", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.send("You are secure now!");
                    }
                }).start();
    }

}
