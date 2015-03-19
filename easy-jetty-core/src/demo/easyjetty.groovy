@Grab('com.athaydes.easy-jetty:easy-jetty:0.1')
import com.athaydes.easyjetty.EasyJetty
import static com.athaydes.easyjetty.http.MethodArbiter.Method.GET

easy = new EasyJetty().on(GET, "/hello",
        { e -> e.out.println 'Hello World!' }).start()

println "Hit any key to stop the server!"
System.in.read()

easy.stop()
