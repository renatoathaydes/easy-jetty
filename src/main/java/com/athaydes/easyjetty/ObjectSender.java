package com.athaydes.easyjetty;


import com.athaydes.easyjetty.mapper.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The ObjectSender can send responses mapped from any Object which has a
 * {@link com.athaydes.easyjetty.mapper.ObjectMapper} associated with its type.
 */
class ObjectSender {

    private final Map<Class<?>, ObjectMapper<?>> mapperByType = new HashMap<>();

    void addMapper(ObjectMapper mapper) {
        mapperByType.put(mapper.getMappedType(), mapper);
    }

    void send(Object object, HttpServletResponse response) throws IOException {
        if (object == null) {
            object = "null";
        }
        response.getOutputStream().println(getResult(object));
    }

    private String getResult(Object object) {
        // try to find a mapper by the exact type
        ObjectMapper mapper = mapperByType.get(object.getClass());
        if (mapper == null) {
            // if nothing is found, try to find a mappers for the Object's super-type
            for (Map.Entry<Class<?>, ObjectMapper<?>> entry : mapperByType.entrySet()) {
                if (entry.getKey().isAssignableFrom(object.getClass())) {
                    mapper = entry.getValue();
                    break;
                }
            }
        }
        String result;
        if (mapper == null) {
            result = object.toString();
        } else {
            result = mapper.map(object);
        }
        return result;
    }

}
