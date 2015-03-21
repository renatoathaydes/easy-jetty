package com.athaydes.easyjetty;


import com.athaydes.easyjetty.mapper.ObjectMapperGroup;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The ObjectSender can send responses mapped from any Object which has a
 * {@link com.athaydes.easyjetty.mapper.ObjectMapper} associated with its type.
 */
class ObjectSender {

    private static final ObjectMapperGroup DEFAULT_MAPPER_GROUP = new ObjectMapperGroup(false, true);

    private volatile ObjectMapperGroup mapperGroup = DEFAULT_MAPPER_GROUP;

    void setMapperGroup(ObjectMapperGroup mapperGroup) {
        this.mapperGroup = mapperGroup;
    }

    void send(Object object, HttpServletResponse response) throws IOException {
        response.getOutputStream().println(mapperGroup.map(object));
    }

    void clear() {
        this.mapperGroup = DEFAULT_MAPPER_GROUP;
    }

}
