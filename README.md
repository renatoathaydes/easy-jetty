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
        new EasyJetty().on(GET, "/hello", new Response() {
                @Override
                public void respond(Exchange exchange) throws IOException {
                    exchange.out.println("Hello World!");
                }
        }).start();
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

You can also start the server in a Groovy script:

```groovy
@Grab('com.athaydes.easy-jetty:easy-jetty:0.1')
import com.athaydes.easyjetty.EasyJetty

import static com.athaydes.easyjetty.http.MethodArbiter.Method.GET

new EasyJetty().on(GET, "/hello",
        { e -> e.out.println 'Hello World!' }).start()
```

Run with (supposing you called the file `easyjetty.groovy`):

```
groovy easyjetty.groovy
```

This will automatically download all dependencies, compile and start the server.

Test with:

```
curl localhost:8080/hello
```
