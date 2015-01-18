package com.athaydes.easyjetty.http;

/**
 * An arbiter to define whether a HTTP Method should be accepted or not.
 * <p/>
 * For example, to accept only GET requests, use {@link MethodArbiter.Method}#GET.
 * <p/>
 * To accept any request, use {@link com.athaydes.easyjetty.http.MethodArbiterFactory.AnyMethodArbiter}.
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


        private final MethodArbiter arbiter = MethodArbiterFactory.singleMethod(this.name());

        @Override
        public boolean accepts(String method) {
            return arbiter.accepts(method);
        }


    }


}

