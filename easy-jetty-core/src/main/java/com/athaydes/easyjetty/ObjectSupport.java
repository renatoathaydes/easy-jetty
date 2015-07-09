package com.athaydes.easyjetty;


import com.athaydes.easyjetty.mapper.ObjectMapperGroup;
import org.eclipse.jetty.http.HttpHeader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

import static com.athaydes.easyjetty.mapper.ObjectMapper.ACCEPT_EVERYTHING;

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
        String contentType = response.getHeader(HttpHeader.CONTENT_TYPE.asString());
        String data = mapperGroup.map(object, contentType != null ? contentType : ACCEPT_EVERYTHING);
        response.getOutputStream().println(data);
    }

    <T> T receive(HttpServletRequest request, Class<T> type) throws IOException {
        return mapperGroup.unmap(request, type, easyJetty.getMaxFormSize());
    }

    <T> Collection<T> receiveAll(HttpServletRequest request, Class<T> type) throws IOException {
        return mapperGroup.unmapAll(request, type, easyJetty.getMaxFormSize());
    }

    void clear() {
        this.mapperGroup = DEFAULT_MAPPER_GROUP;
    }

    ObjectMapperGroup getObjectMapperGroup() {
        return mapperGroup;
    }

}
