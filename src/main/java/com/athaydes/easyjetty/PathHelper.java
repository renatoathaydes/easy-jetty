package com.athaydes.easyjetty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class PathHelper {

    static String sanitize(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path.trim();
    }

    static HandlerPath handlerPath(String path) {
        return handlerPath(sanitize(path).split("/"));
    }

    static HandlerPath handlerPath(String... paths) {
        // we may want to cache HandlerPaths for most-used paths
        return new HandlerPath(paths);
    }

    static boolean isParam(String path) {
        return path.startsWith(":");
    }

    static Map<String, String> matchParams(Map<Integer, String> paramsByIndex, String requestPath) {
        if (paramsByIndex.isEmpty()) {
            return Collections.emptyMap();
        }
        String[] pathParts = requestPath.split("/");
        Map<String, String> result = new HashMap<>(2);
        for (Map.Entry<Integer, String> entry : paramsByIndex.entrySet()) {
            result.put(entry.getValue().substring(1), pathParts[entry.getKey()]);
        }
        return Collections.unmodifiableMap(result);
    }
}
