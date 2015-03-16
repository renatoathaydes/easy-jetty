# EasyJetty

> STATUS: ready for experimental use

EasyJetty makes it trivial to embed [Jetty](http://www.eclipse.org/jetty/),
a fully functional web server and Servlet container, into your application.

Although EasyJetty provides lots of shortcuts to make the creation of a web server application
based on Jetty much simpler, you still have the full power of Jetty available where you need it.

See the [documentation](https://github.com/renatoathaydes/easy-jetty/wiki) for details.

```java
import com.athaydes.easyjetty.*;
import java.io.IOException;
import static com.athaydes.easyjetty.http.MethodArbiter.Method.GET;

public class Sample {

    public static void main(String[] args) {
        new EasyJetty().on(GET, "/hello", new Responder() {
                @Override
                public void respond(Exchange exchange) throws IOException {
                    exchange.out.println("Hello World!");
                }
        }).start();
    }

}
```

Run as a simple Java application.

Test with:

```
curl localhost:8080/hello
```

You should see the `Hello World!` message from the server.


If you want things to be easier to start and a little less verbose, you can use a [Groovy](http://beta.groovy-lang.org/docs/latest/html/documentation/)
script instead:

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


Read the [documentation](https://github.com/renatoathaydes/easy-jetty/wiki) to see what else EasyJetty has to offer.

## Code samples

* [Java samples](src/test/java/sample)
* [Groovy samples](src/demo)

