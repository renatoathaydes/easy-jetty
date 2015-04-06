package sample;

import com.athaydes.easyjetty.EasyJetty;
import com.athaydes.easyjetty.Responder;

import java.io.IOException;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.GET;

public class ErrorHandlerSample {

    public static void main(String[] args) {
        new EasyJetty()
                .errorPage(400, 499, "errors/page400")
                .errorPage(500, 599, "errors/page500")
                .on(GET, "/hello", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.out.println("Hello!");
                    }
                })
                .on(GET, "error", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        throw new RuntimeException();
                    }
                })
                .on(GET, "errors/page400", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.send("Page not found");
                    }
                })
                .on(GET, "errors/page500", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.send("Internal Error");
                    }
                })
                .start();
    }

}
