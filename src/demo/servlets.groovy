@Grab('com.athaydes.easy-jetty:easy-jetty:0.1')
import com.athaydes.easyjetty.EasyJetty

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

easy = new EasyJetty()
        .servlet('/hello', HelloServlet)
        .servlet('/bye', ByeServlet)
        .start()

println "Hit any key to stop the server!"
System.in.read()

easy.stop()

class HelloServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.getOutputStream().println("Hello")
    }
}

class ByeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.getOutputStream().println("Bye")
    }
}