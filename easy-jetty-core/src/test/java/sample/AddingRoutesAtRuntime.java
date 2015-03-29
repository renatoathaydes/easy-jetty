package sample;

import com.athaydes.easyjetty.EasyJetty;
import com.athaydes.easyjetty.Responder;
import org.boon.IO;

import java.io.IOException;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.DELETE;
import static com.athaydes.easyjetty.http.MethodArbiter.Method.GET;
import static com.athaydes.easyjetty.http.MethodArbiter.Method.POST;
import static org.boon.Maps.map;

/**
 * This sample shows how it is easy to add/remove handlers at runtime.
 */
public class AddingRoutesAtRuntime {

    public static void main(String[] args) {
        final EasyJetty jetty = new EasyJetty();

        jetty.on(POST, "/paths/:path", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                final String path = exchange.params.get("path");
                final String response = IO.read(exchange.request.getInputStream(), "utf-8");
                System.out.println("Adding path " + path + ", Response will be: " + response);
                jetty.on(GET, path, new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.out.println(response);
                    }
                });
            }
        }).on(DELETE, "paths/:path", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                final String path = exchange.params.get("path");
                System.out.println("Removing path " + path);
                exchange.send(map("success", jetty.remove(GET, path)));
            }
        }).start();
    }

}
