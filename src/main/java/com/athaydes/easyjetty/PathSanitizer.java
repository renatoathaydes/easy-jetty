package com.athaydes.easyjetty;

class PathSanitizer {

    static String sanitize(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path.trim();
    }

}
