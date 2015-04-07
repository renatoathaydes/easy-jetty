package com.athaydes.easyjetty;


import com.athaydes.easyjetty.mapper.ObjectMapperGroup;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

class ObjectSupport {

    private static final ObjectMapperGroup DEFAULT_MAPPER_GROUP = new ObjectMapperGroup(false, true);

    private volatile ObjectMapperGroup mapperGroup = DEFAULT_MAPPER_GROUP;

    private final EasyJetty easyJetty;

    public ObjectSupport(EasyJetty easyJetty) {
        this.easyJetty = easyJetty;
    }

    void setMapperGroup(ObjectMapperGroup mapperGroup) {
        this.mapperGroup = mapperGroup;
    }

    void send(Object object, HttpServletResponse response) throws IOException {
        response.getOutputStream().println(mapperGroup.map(object));
    }

    <T> T receive(HttpServletRequest request, Class<T> type) throws IOException {
        return mapperGroup.unmap(request, type, easyJetty.getMaxFormSize());
    }

    void clear() {
        this.mapperGroup = DEFAULT_MAPPER_GROUP;
    }

    ObjectMapperGroup getObjectMapperGroup() {
        return mapperGroup;
    }

}
