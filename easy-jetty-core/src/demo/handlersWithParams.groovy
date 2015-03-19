@Grab('com.athaydes.easy-jetty:easy-jetty:0.1')
import com.athaydes.easyjetty.EasyJetty

import static com.athaydes.easyjetty.http.MethodArbiter.Method.GET

def easy = new EasyJetty()
        .on(GET, "/hello", { e -> e.out.println 'Hello World!' })
        .on(GET, "/hello/:name", { e -> e.out.println "Hello ${e.params['name']}" })
        .on(GET, "/ola", { e -> e.out.println 'Ola Mundo!' })
        .on(GET, "/ola/:nombre", { e -> e.out.println "Ola ${e.params['nombre']}" })
        .on(GET, "/:any/:thing", { e -> e.out.println "You set ${e.params['any']} ${e.params['thing']}" })
        .start()

println "Hit any key to stop the server!"
System.in.read()

easy.stop()
