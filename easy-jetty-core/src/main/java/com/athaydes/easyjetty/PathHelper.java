package com.athaydes.easyjetty;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PathHelper {

    public static String sanitize(String path) {
        path = path.trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    static HandlerPath handlerPath(String path) {
        String[] pathParts = sanitize(path).split("/");
        if (pathParts.length == 0) {
            return HandlerPath.empty();
        }
        return handlerPath(pathParts);
    }

    static HandlerPath handlerPath(String... paths) {
        // we may want to cache HandlerPaths for most-used paths
        return new HandlerPath(paths);
    }

    public static boolean isParam(String path) {
        return path.startsWith(":");
    }

    public static Map<String, String> matchParams(Map<Integer, String> paramsByIndex, String requestPath) {
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
