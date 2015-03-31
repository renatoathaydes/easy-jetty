package com.athaydes.easyjetty.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An ObjectMapper group which can use any of the ObjectMappers added to it to map an
 * Object.
 */
public class ObjectMapperGroup {

    private final Map<Class<?>, ObjectMapper<?>> mapperByType = new HashMap<>();
    private final boolean exactTypeOnly;
    private volatile boolean lenient = true;
    private volatile String nullString = "<null>";

    /**
     * Creates a lenient ObjectMappperGroup.
     *
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
     * @param object
     * @return
     */
    public String map(Object object) {
        if (object == null) {
            return nullString;
        }
        // try to find a mapper by the exact type
        ObjectMapper mapper = findMapper(object);
        if (mapper == null) {
            if (lenient) {
                return object.toString();
            }
            throw new RuntimeException("Cannot map Object of type '" + object.getClass() + "' to a String");
        }
        return mapper.map(object);
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
