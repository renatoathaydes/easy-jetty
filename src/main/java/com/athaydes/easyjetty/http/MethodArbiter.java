package com.athaydes.easyjetty.http;

/**
 * An arbiter to define whether a HTTP Method should be accepted or not.
 * <p/>
 * For example, to accept only GET requests, use {@link MethodArbiter.Method}#GET.
 * <p/>
 * To accept any request, use {@link com.athaydes.easyjetty.http.MethodArbiter.AnyMethodArbiter}.
 */
public interface MethodArbiter {

    /**
     * Returns true if the given method should be accepted.
     *
     * @param method method that may be accepted
     * @return true if the method is accepted
     */
    public boolean accepts(String method);

    /**
     * HTTP Method (RFC 2616).
     */
    public static enum Method implements MethodArbiter {
        CONNECT, DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT, TRACE;

        @Override
        public boolean accepts(String method) {
            return name().equals(method);
        }

    }

    /**
     * An AcceptMethod that accepts any Method.
     */
    public static class AnyMethodArbiter implements MethodArbiter {
        @Override
        public boolean accepts(String method) {
            return true;
        }
    }

}
