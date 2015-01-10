package com.athaydes.easyjetty.http;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
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
     * HTTP Method (as specified in RFC 2068).
     *
     * @see <a href="http://www.ietf.org/rfc/rfc2068.txt">http://www.ietf.org/rfc/rfc2068.txt</a>
     */
    public static enum Method implements MethodArbiter {
        DELETE, GET, HEAD, OPTIONS, POST, PUT, TRACE;

        private final MethodArbiter arbiter = new SingleMethodArbiter(this.name());

        @Override
        public boolean accepts(String method) {
            return arbiter.accepts(method);
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
         * @param accepted       first accepted method
         * @param othersAccepted other accepted methods
         * @return MethodArbiter
         */
        public static MethodArbiter anyOf(Method accepted, Method... othersAccepted) {
            return new AnyOfMethodsArbiter(accepted, othersAccepted);
        }

        /**
         * Creates and returns a {@link com.athaydes.easyjetty.http.MethodArbiter}
         * that accepts a custom HTTP method.
         *
         * @return MethodArbiter
         */
        public static MethodArbiter customMethod(String accepted) {
            return new SingleMethodArbiter(accepted);
        }

    }

    static class SingleMethodArbiter implements MethodArbiter {

        private final String accepted;

        SingleMethodArbiter(String accepted) {
            Objects.requireNonNull(accepted);
            this.accepted = accepted;
        }

        @Override
        public boolean accepts(String method) {
            return accepted.equals(method);
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
