package sample;

import com.athaydes.easyjetty.EasyJetty;
import com.athaydes.easyjetty.Responder;
import com.athaydes.easyjetty.mapper.CollectionMapper;
import com.athaydes.easyjetty.mapper.ObjectMapperGroup;
import com.athaydes.easyjetty.mapper.ObjectSerializer;
import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;

import java.io.IOException;
import java.util.*;

import static com.athaydes.easyjetty.http.MethodArbiter.Method.*;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.boon.Maps.map;

/**
 * An example showing how to use ObjectMappers.
 */
public class HandlersWithObjectMappers {

    static int ids = 0;

    static class Person {
        int id;
        String name;
        int age;

        public Person(String name, int age) {
            this.id = ids++;
            this.name = name;
            this.age = age;
        }

        public Person(Person person) {
            this.id = ids++;
            this.name = person.name;
            this.age = person.age;
        }
    }

    static final Map<Integer, Person> db = new HashMap<>();

    static {
        for (Person person : Arrays.asList(
                new Person("John", 20), new Person("Mary", 26),
                new Person("Jack", 45), new Person("Adam", 53))) {
            db.put(person.id, person);
        }
    }

    static class PersonMapper extends ObjectSerializer<Person> {

        ObjectMapper mapper = JsonFactory.create();

        @Override
        public String map(Person object) {
            return mapper.toJson(object);
        }

        @Override
        public Person unmap(String objectAsString) {
            return mapper.fromJson(objectAsString, getMappedType());
        }

        @Override
        public Class<? extends Person> getMappedType() {
            return Person.class;
        }

        @Override
        public String getContentType() {
            return "application/json";
        }

    }

    static class BoonObjectMapper extends ObjectSerializer<Object> {

        ObjectMapper mapper = JsonFactory.create();

        @Override
        public String map(Object object) {
            return mapper.toJson(object);
        }

        @Override
        public Object unmap(String objectAsString) {
            return mapper.fromJson(objectAsString);
        }

        @Override
        public Class<Object> getMappedType() {
            return Object.class;
        }

        @Override
        public String getContentType() {
            return "application/json";
        }
    }

    static class JsonCollectionMapper extends CollectionMapper {

        @Override
        public <T> Collection<T> unmapAll(String objectAsString, Class<T> type) {
            return JsonFactory.fromJsonArray(objectAsString, type);
        }

        @Override
        public Collection unmap(String objectAsString) {
            return unmapAll(objectAsString, Object.class);
        }

        @Override
        public String map(Collection object) {
            return JsonFactory.toJson(object);
        }

        @Override
        public String getContentType() {
            return "application/json";
        }
    }

    public static void main(String[] args) {
        new EasyJetty().defaultContentType("text/json;charset=utf-8")
                .defaultAccept("application/json")
                .errorPage(404, "not-found")
                .withMapperGroup(new ObjectMapperGroup()
                        .withCollectionMappers(new JsonCollectionMapper())
                        .withMappers(new PersonMapper(), new BoonObjectMapper()))
                .on(GET, "not-found", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.send("Nothing here");
                    }
                })
                .on(GET, "/", "text/plain", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        exchange.response.setContentType("text/plain;charset=utf-8");
                        exchange.send("Try resources /people and /people/1");
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
                        Person person = db.get(Integer.valueOf(exchange.params.get("id")));
                        exchange.send(person); // EasyJetty automatically sends a 404 response if trying to send null
                    }
                })
                .on(DELETE, "/people/:id", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        Person person = db.remove(Integer.valueOf(exchange.params.get("id")));
                        if (person == null) {
                            exchange.response.setStatus(SC_NOT_FOUND);
                        }
                    }
                })
                .on(POST, "/person", "application/json", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        // use copy-constructor so the person ID is generated
                        Person person = new Person(exchange.receiveAs(Person.class));
                        db.put(person.id, person);
                        exchange.send(map("id", person.id));
                    }
                })
                .on(POST, "/people", "application/json", new Responder() {
                    @Override
                    public void respond(Exchange exchange) throws IOException {
                        // use copy-constructor so the person ID is generated
                        List<Person> people = exchange.receiveAll(Person.class);
                        List<Integer> ids = new ArrayList<>(people.size());
                        for (Person p : people) {
                            Person person = new Person(p);
                            ids.add(person.id);
                            db.put(person.id, person);
                        }
                        exchange.send(map("ids", ids));
                    }
                })
                .start();
    }

}
