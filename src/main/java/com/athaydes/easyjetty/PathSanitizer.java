package com.athaydes.easyjetty;

class PathSanitizer {

    static String sanitize(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path.trim();
    }

    static HandlerPath handlerPath(String path) {
        return handlerPath(path.split("/"));
    }

    static HandlerPath handlerPath(String... paths) {
        // we may want to cache HandlerPaths for most-used paths
        return new HandlerPath(paths);
    }

    static boolean isParam(String path) {
        return path.startsWith(":");
    }
}
