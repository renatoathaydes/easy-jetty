package sample;

import com.athaydes.easyjetty.EasyJetty;
import com.athaydes.easyjetty.Responder;
import com.athaydes.easyjetty.mapper.ObjectMapperGroup;
import com.athaydes.easyjetty.mapper.ObjectSerializer;
import org.boon.json.JsonSerializer;
import org.boon.json.JsonSerializerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.*;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.boon.Maps.map;

/**
 * An example showing how to use ObjectMappers.
 */
public class HandlersWithObjectMappers {

    static final Map<String, String> db = new HashMap<>();

    static {
        db.put("id-1", "John");
        db.put("id-2", "Mark");
        db.put("id-3", "Erik");
        db.put("id-4", "Mary");
        db.put("id-5", "Jennifer");
    }

    static int idCount = db.size();

    static class BoonMapper extends ObjectSerializer<Object> {

        private final JsonSerializer serializer = new JsonSerializerFactory().create();

        @Override
        public String map(Object object) {
            return serializer.serialize(object).toString();
        }

        @Override
        public Class<Object> getMappedType() {
            return Object.class;
        }
    }

    public static void main(String[] args) {
        new EasyJetty().defaultContentType("text/json;charset=utf-8")
                .withMapperGroup(new ObjectMapperGroup(true, true)
                        .withMappers(new BoonMapper()))
                .on(GET, "/", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.response.setContentType("text/plain;charset=utf-8");
                        exchange.send("Try resources /people and /people/id-1");
                    }
                })
                .on(GET, "/people", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.send(db.values());
                    }
                })
                .on(GET, "/people/:id", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        String name = db.get(exchange.params.get("id"));
                        if (name == null) {
                            exchange.response.setStatus(SC_NOT_FOUND);
                        } else {
                            exchange.send(name);
                        }
                    }
                })
                .on(DELETE, "/people/:id", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        String name = db.remove(exchange.params.get("id"));
                        if (name == null) {
                            exchange.response.setStatus(SC_NOT_FOUND);
                        }
                    }
                })
                .on(PUT, "/people/:name", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.response.setContentType("text/plain;charset=utf-8");
                        String name = exchange.params.get("name");
                        int id = ++idCount;
                        db.put("id-" + id, name);
                        exchange.send(map("name", name, "id", id));
                    }
                })
                .start();
    }

}
