package com.athaydes.easyjetty;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.athaydes.easyjetty.PathHelper.handlerPath;
import static com.athaydes.easyjetty.PathHelper.isParam;

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

    public Map<Integer, String> getParametersByIndex() {
        Map<Integer, String> result = new HashMap<>(2);
        for (int i = 0; i < paths.length; i++) {
            if (PathHelper.isParam(paths[i])) {
                result.put(i, paths[i]);
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HandlerPath that = (HandlerPath) o;

        return Arrays.equals(ignoreParamNames(this.paths), ignoreParamNames(that.paths));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(ignoreParamNames(paths));
    }

    @Override
    public String toString() {
        return "HandlerPath{" +
                "paths=" + Arrays.toString(paths) +
                '}';
    }

    private String[] ignoreParamNames(String[] paths) {
        String[] result = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            if (isParam(paths[i])) {
                result[i] = ":";
            } else {
                result[i] = paths[i];
            }
        }
        return result;
    }
}
