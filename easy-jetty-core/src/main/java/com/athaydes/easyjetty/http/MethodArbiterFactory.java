package com.athaydes.easyjetty.http;

import com.athaydes.easyjetty.http.MethodArbiter.Method;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A Factory for creating MethodArbiter instances.
 */
public class MethodArbiterFactory {

    private static final Map<String, MethodArbiter> arbiterByName = new HashMap<>();
    private static AnyMethodArbiter anyMethodInstance;

    private static class SingleMethodArbiter implements MethodArbiter {

        final String accepted;

        public SingleMethodArbiter(String accepted) {
            this.accepted = accepted;
        }

        @Override
        public boolean accepts(String method) {
            return accepted.equals(method);
        }
    }

    private static class AnyMethodArbiter implements MethodArbiter {

        @Override
        public boolean accepts(String method) {
            return true;
        }
    }

    private static class AnyOfMethodsArbiter implements MethodArbiter {

        private final Set<String> acceptedMethods;

        private AnyOfMethodsArbiter(Method accepted, Method... othersAccepted) {
            acceptedMethods = new HashSet<>(othersAccepted.length + 1);
            acceptedMethods.add(accepted.name());
            for (Method other : othersAccepted) {
                acceptedMethods.add(other.name());
            }
        }

        @Override
        public boolean accepts(String method) {
            return acceptedMethods.contains(method);
        }

    }

    /**
     * Creates and returns a {@link com.athaydes.easyjetty.http.MethodArbiter}
     * that accepts any HTTP method.
     *
     * @return MethodArbiter
     */
    public static MethodArbiter anyMethod() {
        if (anyMethodInstance == null) {
            anyMethodInstance = new AnyMethodArbiter();
        }
        return anyMethodInstance;
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
     * that accepts a single HTTP method.
     *
     * @param accepted the accepted HTTP method
     * @return MethodArbiter
     */
    public static MethodArbiter singleMethod(String accepted) {
        Objects.requireNonNull(accepted);
        MethodArbiter arbiter = arbiterByName.get(accepted);
        if (arbiter == null) {
            arbiter = new SingleMethodArbiter(accepted);
            arbiterByName.put(accepted, arbiter);
        }
        return arbiter;
    }

}


