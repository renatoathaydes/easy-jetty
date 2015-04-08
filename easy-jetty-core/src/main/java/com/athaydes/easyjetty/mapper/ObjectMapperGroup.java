package com.athaydes.easyjetty.mapper;

import com.athaydes.easyjetty.external.MIMEParse;
import org.eclipse.jetty.http.HttpHeader;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

import static com.athaydes.easyjetty.mapper.ObjectMapper.ACCEPT_EVERYTHING;

/**
 * An ObjectMapper group which can use any of the ObjectMappers added to it to map/unmap an
 * Object/String.
 */
public class ObjectMapperGroup {

    static final String PAYLOAD_TOO_BIG = "Request payload is too big";

    private static final ObjectMapper<Object> toStringMapper = new ObjectSerializer<Object>() {
        @Override
        public String map(Object object) {
            return object.toString();
        }

        @Override
        public Object unmap(String objectAsString) {
            return objectAsString;
        }

        @Override
        public Class<? extends Object> getMappedType() {
            return Object.class;
        }
    };
    private final Map<Class<?>, List<ObjectMapper<?>>> mapperByType = new HashMap<>();
    private final boolean exactTypeOnly;
    private volatile boolean lenient = true;

    private volatile String nullString = "<null>";

    /**
     * Creates a lenient ObjectMappperGroup.
     * <p/>
     * This is equivalent to calling <code>new ObjectMapperGroup(true, true)</code>.
     */
    public ObjectMapperGroup() {
        this(true, true);
    }

    /**
     * Creates an ObjectMapperGroup.
     *
     * @param exactTypeOnly if true, only Objects whose exact Class has a corresponding Mapper will be mapped.
     *                      <br/>
     *                      if false, will also try using Mappers for super-types of the Object (runs slower).
     * @param lenient       if true, Objects of unknown types will be mapped to their
     *                      toString() value.
     *                      <br/>
     *                      if false, an Exception will be thrown when trying to map Objects of unknown types.
     */
    public ObjectMapperGroup(boolean exactTypeOnly, boolean lenient) {
        this.exactTypeOnly = exactTypeOnly;
        this.lenient = lenient;
    }

    /**
     * Add the given ObjectMappers to this group.
     *
     * @param mappers to be added
     * @return this
     */
    public ObjectMapperGroup withMappers(ObjectMapper... mappers) {
        for (ObjectMapper mapper : mappers) {
            List<ObjectMapper<?>> existingMappers = mapperByType.get(mapper.getMappedType());
            if (existingMappers == null) {
                existingMappers = new ArrayList<>(1);
                mapperByType.put(mapper.getMappedType(), existingMappers);
            }
            existingMappers.add(mapper);
        }
        return this;
    }

    /**
     * Sets the String mapped to the null value.
     * The default value is "&lt;null&gt;"
     *
     * @param nullString a non-null String.
     */
    public ObjectMapperGroup withNullString(String nullString) {
        Objects.requireNonNull(nullString);
        this.nullString = nullString;
        return this;
    }

    /**
     * Maps the given Object to a String using the first appropriate ObjectMapper encountered.
     * Errors are handled according to the "lenient" and "exactTypeOnly" parameters.
     *
     * @param object to map
     * @return object as String
     */
    public String map(Object object) {
        return map(object, ACCEPT_EVERYTHING);
    }

    /**
     * Maps the given Object to a String using the appropriate ObjectMapper.
     * Errors are handled according to the "lenient" and "exactTypeOnly" parameters.
     *
     * @param object      to map
     * @param contentType content-type expected (eg. JSON, XML)
     * @return object as String with the given contentType
     */
    public String map(Object object, String contentType) {
        if (object == null) {
            return nullString;
        }
        ObjectMapper mapper = findMapperFor(contentType, object.getClass());
        if (mapper == null) {
            throw new RuntimeException("Cannot map Object of type '" + object.getClass() + "' to a String");
        }
        return mapper.map(object);
    }

    /**
     * Attempts to unmarshall an Object of type T from the request content.
     *
     * @param request          whose content should be unmarshalled
     * @param type             of the returned Object
     * @param maxContentLength maximum allowed content length in bytes
     * @param <T>              type of the returned Object
     * @return Object of type T
     * @throws java.lang.RuntimeException         if no mapper is found and this group is not lenient
     * @throws java.io.IOException                if a problem occurs while reading the request content
     * @throws java.lang.IllegalArgumentException if the request content length is larger than maxContentLength
     */
    public <T> T unmap(HttpServletRequest request, Class<T> type, int maxContentLength)
            throws IOException {
        ObjectMapper<T> mapper = findMapperFor(request.getHeader(HttpHeader.ACCEPT.asString()), type);
        if (request.getContentLength() > maxContentLength) {
            throw new IllegalArgumentException(PAYLOAD_TOO_BIG);
        }
        try {
            String content = readFrom(request.getReader(), maxContentLength);
            return type.cast(mapper.unmap(content));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readFrom(BufferedReader reader, int maxContentLength) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] charBuffer = new char[128];
        int totalBytes = 0;
        int bytesRead;
        while ((bytesRead = reader.read(charBuffer)) > 0) {
            totalBytes += bytesRead;
            if (totalBytes > maxContentLength) {
                throw new IllegalArgumentException(PAYLOAD_TOO_BIG);
            }
            sb.append(charBuffer, 0, bytesRead);
        }
        return sb.toString();
    }

    private <T> ObjectMapper<T> findMapperFor(String acceptedContentType, Class<T> type) {
        ObjectMapper<?> result = null;
        List<ObjectMapper<?>> mappers = findMappersByType(type);

        if (lenient && mappers == null) {
            result = toStringMapper;
        } else if (mappers == null) {
            throw new RuntimeException("No mapper found for type " + type.getName());
        } else if (acceptedContentType == null || acceptedContentType.equals(ACCEPT_EVERYTHING)) {
            result = mappers.get(0);
        } else for (ObjectMapper<?> mapper : mappers) {
            if (MIMEParse.isAccepted(acceptedContentType, mapper.getContentType())) {
                result = mapper;
                break;
            }
        }
        if (result == null) {
            throw new RuntimeException("Found " + mappers.size() + " mapper(s) for the type " + type.getName() +
                    ", but no mapper can handle content-type " + acceptedContentType);
        }
        return (ObjectMapper<T>) result;
    }

    private List<ObjectMapper<?>> findMappersByType(Class<?> type) {
        List<ObjectMapper<?>> mappers = mapperByType.get(type);
        if (mappers == null && !exactTypeOnly) {
            // if nothing is found, try to find a mapper that can handle an instanceof the type
            for (Map.Entry<Class<?>, List<ObjectMapper<?>>> entry : mapperByType.entrySet()) {
                if (entry.getKey().isAssignableFrom(type)) {
                    mappers = entry.getValue();
                    break;
                }
            }
        }
        return mappers;
    }

    /**
     * Remove all ObjectMappers from this group.
     */
    public void clear() {
        mapperByType.clear();
    }

}
