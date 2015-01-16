package sample;

import com.athaydes.easyjetty.EasyJetty;
import com.athaydes.easyjetty.Response;
import com.athaydes.easyjetty.mapper.ObjectMapper;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.*;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

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

    static class JsonNameMapper implements ObjectMapper<String> {
        @Override
        public String map(String object) {
            return "{ 'name': '" + object + "' }";
        }

        @Override
        public Class<String> getMappedType() {
            return String.class;
        }
    }

    static class JsonNameCollectionMapper implements ObjectMapper<Collection> {
        @Override
        public String map(Collection object) {
            String result = "{\n 'names': [ ";
            int i = 0;
            for (Object item : object) {
                i++;
                result += "'" + item + "'" + (i == object.size() ? "" : ", ");
            }
            return result + " ]\n}";
        }

        @Override
        public Class<Collection> getMappedType() {
            return Collection.class;
        }
    }

    public static void main(String[] args) {
        new EasyJetty().defaultContentType("text/json;charset=utf-8")
                .addMapper(new JsonNameMapper())
                .addMapper(new JsonNameCollectionMapper())
                .on(GET, "/", new Response() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.response.setContentType("text/plain;charset=utf-8");
                        exchange.send("Try resources /people and /people/id-1");
                    }
                })
                .on(GET, "/people", new Response() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.send(db.values());
                    }
                })
                .on(GET, "/people/:id", new Response() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.send(db.get(exchange.params.get("id")));
                    }
                })
                .on(DELETE, "/people/:id", new Response() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        String name = db.remove(exchange.params.get("id"));
                        if (name == null) {
                            exchange.response.setStatus(SC_NOT_FOUND);
                        }
                    }
                })
                .on(PUT, "/people/:name", new Response() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.response.setContentType("text/plain;charset=utf-8");
                        db.put("id-" + (++idCount), exchange.params.get("name"));
                        exchange.send(true);
                    }
                })
                .start();
    }

}
