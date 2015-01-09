package sample;

import com.athaydes.easyjetty.EasyJetty;
import com.athaydes.easyjetty.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.GET;

/**
 * Easy-Jetty Sample code.
 */
public class Sample {

    public static void main(String[] args) {
        new EasyJetty()
                .servlet("/hello", HelloServlet.class)
                .on(GET, "/bye", new Response() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.out.println("Bye bye!");
                    }
                }).start();
    }

    public static class HelloServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.getOutputStream().println("Hello");
        }
    }

}
