package sample;

import com.athaydes.easyjetty.EasyJetty;
import com.athaydes.easyjetty.Response;
import org.boon.IO;

import java.io.IOException;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.GET;
import static com.athaydes.easyjetty.http.MethodArbiter.Method.POST;

/**
 *
 */
public class AddingRoutesAtRuntime {

    public static void main(String[] args) {
        final EasyJetty jetty = new EasyJetty();

        jetty.on(POST, "/add/:path", new Response() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                final String response = IO.read(exchange.request.getInputStream(), "utf-8");
                System.out.println("Response will be: " + response);
                jetty.on(GET, exchange.params.get("path"), new Response() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.out.println(response);
                    }
                });
            }
        }).start();
    }

}
