package com.athaydes.easyjetty;


import java.util.Arrays;
import java.util.Objects;

import static com.athaydes.easyjetty.PathSanitizer.handlerPath;

class HandlerPath {

    private final String[] paths;

    HandlerPath(String... paths) {
        for (String path : paths) {
            Objects.requireNonNull(path, "All path components must be non-null");
        }
        this.paths = paths;
    }

    public int size() {
        return paths.length;
    }

    public String head() {
        return paths[0];
    }

    public HandlerPath tail() {
        return handlerPath(Arrays.copyOfRange(paths, 1, paths.length));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HandlerPath that = (HandlerPath) o;

        return Arrays.equals(paths, that.paths);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(paths);
    }

    @Override
    public String toString() {
        return "HandlerPath{" +
                "paths=" + Arrays.toString(paths) +
                '}';
    }
}
