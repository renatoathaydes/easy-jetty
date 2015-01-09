package com.athaydes.easyjetty.http;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

        /**
         * Creates and returns a {@link com.athaydes.easyjetty.http.MethodArbiter}
         * that accepts any HTTP method.
         *
         * @return MethodArbiter
         */
        public static MethodArbiter anyMethod() {
            return new AnyMethodArbiter();
        }

        /**
         * Creates and returns a {@link com.athaydes.easyjetty.http.MethodArbiter}
         * that accepts any HTTP method.
         *
         * @return MethodArbiter
         */
        public static MethodArbiter anyOf(Method accepted, Method... othersAccepted) {
            return new AnyOfMethodsArbiter(accepted, othersAccepted);
        }

    }

    static class AnyMethodArbiter implements MethodArbiter {
        @Override
        public boolean accepts(String method) {
            return true;
        }
    }

    static class AnyOfMethodsArbiter implements MethodArbiter {

        private final Set<Method> acceptedMethods;

        private AnyOfMethodsArbiter(Method accepted, Method... othersAccepted) {
            acceptedMethods = new HashSet<>(othersAccepted.length + 1);
            acceptedMethods.add(accepted);
            acceptedMethods.addAll(Arrays.asList(othersAccepted));
        }

        @Override
        public boolean accepts(String method) {
            try {
                return acceptedMethods.contains(Method.valueOf(method));
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

    }

}
