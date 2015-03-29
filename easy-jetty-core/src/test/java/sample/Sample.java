package sample;

import com.athaydes.easyjetty.EasyJetty;
import com.athaydes.easyjetty.Responder;

import java.io.IOException;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.GET;

/**
 * Simple handler sample.
 */
public class Sample {

    public static void main(String[] args) {
        new EasyJetty().on(GET, "/bye", new Responder() {
            @Override
            public void respond(Exchange exchange) throws IOException {
                exchange.out.println("Bye bye!");
            }
        }).start();
    }

}
