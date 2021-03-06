package com.athaydes.easyjetty.mapper;

import java.util.Collection;

public abstract class CollectionMapper implements ObjectMapper<Collection> {

    protected ObjectMapperGroup mapperGroup;

    /**
     * Sets the mapperGroup which can be used to serialize individual items of a Collection.
     * This will be called when a CollectionMapper is added to an ObjectMapperGroup.
     *
     * @param mapperGroup mapperGroup
     */
    public void setMapperGroup(ObjectMapperGroup mapperGroup) {
        this.mapperGroup = mapperGroup;
    }

    @Override
    public Class<? extends Collection> getMappedType() {
        return Collection.class;
    }

    @Override
    public <S extends Collection> S unmap(String objectAsString, Class<S> type) {
        Collection result = unmapAll(objectAsString, Object.class);
        if (type.isAssignableFrom(result.getClass())) {
            return type.cast(result);
        } else {
            throw new ClassCastException("Cannot cast object to " + type.getName());
        }
    }

    public abstract <T> Collection<T> unmapAll(String objectAsString, Class<T> type);
}
