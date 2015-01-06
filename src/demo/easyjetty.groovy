@Grab('com.athaydes.easy-jetty:easy-jetty:0.1')
import com.athaydes.easyjetty.EasyJetty
import javax.servlet.http.*;

easy = new EasyJetty()
easy.servlet("/hello", HelloServlet).start()

Thread.start {
    sleep 10_000
    easy.stop()
}

class HelloServlet extends HttpServlet {
    void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.outputStream.println("Hello")
    }
}
