package com.athaydes.easyjetty.mapper;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An ObjectMapper group which can use any of the ObjectMappers added to it to map an
 * Object.
 */
public class ObjectMapperGroup {

    static final String PAYLOAD_TOO_BIG = "Request payload is too big";

    private static final ObjectMapper<String> stringMapper = new ObjectMapper<String>() {
        @Override
        public String map(String object) {
            return object.toString();
        }

        @Override
        public String unmap(String objectAsString) {
            return objectAsString;
        }

        @Override
        public Class<? extends String> getMappedType() {
            return String.class;
        }
    };
    private final Map<Class<?>, ObjectMapper<?>> mapperByType = new HashMap<>();
    private final boolean exactTypeOnly;
    private volatile boolean lenient = true;

    private volatile String nullString = "<null>";

    /**
     * Creates a lenient ObjectMappperGroup.
     * <p/>
     * This is equivalent to calling <code>new ObjectMapperGroup(false, true)</code>.
     */
    public ObjectMapperGroup() {
        this(false, true);
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

    public ObjectMapperGroup withMappers(ObjectMapper... mappers) {
        for (ObjectMapper mapper : mappers) {
            mapperByType.put(mapper.getMappedType(), mapper);
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
     * Maps the given Object to a String using the appropriate ObjectMapper.
     * Errors are handled according to the "lenient" and "exactTypeOnly" parameters.
     *
     * @param object to map
     * @return object as String
     */
    public String map(Object object) {
        if (object == null) {
            return nullString;
        }
        // try to find a mapper by the exact type
        ObjectMapper mapper = findMapper(object);
        if (mapper == null) {
            if (lenient) {
                return stringMapper.map(object.toString());
            }
            throw new RuntimeException("Cannot map Object of type '" + object.getClass() + "' to a String");
        }
        return mapper.map(object);
    }

    public <T> T unmap(HttpServletRequest request, Class<T> type, int maxContentLength) {
        ObjectMapper<?> mapper = mapperByType.get(type);
        if (lenient && mapper == null && type.equals(String.class)) {
            mapper = stringMapper;
        } else if (mapper == null) {
            throw new RuntimeException("No mapper found for type " + type.getName());
        }
        if (request.getContentLength() > maxContentLength) {
            throw new IllegalArgumentException(PAYLOAD_TOO_BIG);
        }
        try {
            StringBuilder sb = readFrom(request.getReader(), maxContentLength);
            return type.cast(mapper.unmap(sb.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private StringBuilder readFrom(BufferedReader reader, int maxContentLength) throws IOException {
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
        return sb;
    }

    private ObjectMapper findMapper(Object object) {
        ObjectMapper mapper = mapperByType.get(object.getClass());
        if (mapper == null && !exactTypeOnly) {
            // if nothing is found, try to find a mapper that can handle an instanceof the type
            for (Map.Entry<Class<?>, ObjectMapper<?>> entry : mapperByType.entrySet()) {
                if (entry.getKey().isAssignableFrom(object.getClass())) {
                    mapper = entry.getValue();
                    break;
                }
            }
        }
        return mapper;
    }

    public void clear() {
        mapperByType.clear();
    }

}
