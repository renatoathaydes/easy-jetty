# EasyJetty

EasyJetty makes it trivial to embed [Jetty](http://www.eclipse.org/jetty/),
a fully functional web server and Servlet container, into your application.

```java
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import com.athaydes.easyjetty.EasyJetty;

public class Sample {

    public static void main(String[] args) {
        new EasyJetty().servlet("/hello", HelloServlet.class).start();
    }

    public static class HelloServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.getOutputStream().println("Hello");
        }
    }

}
```

You can run the above Java class as any Java application.

Test it with:

```
curl localhost:8080/hello
```

You should see the "Hello" message from the server.


## Starting the server with a Groovy script

You can also start the server in a Groovy script (called `easyjetty.groovy` for example):

```groovy
@Grab('com.athaydes.easy-jetty:easy-jetty:0.1')
import com.athaydes.easyjetty.EasyJetty
import javax.servlet.http.*

new EasyJetty().servlet("/hello", HelloServlet).start()

class HelloServlet extends HttpServlet {
    void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.outputStream.println("Hello")
    }
}
```

Run with:

```
groovy easyjetty.groovy
```

This will automatically download all dependencies, compile and start the server.
