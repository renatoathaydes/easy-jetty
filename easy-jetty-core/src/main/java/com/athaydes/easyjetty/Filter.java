package com.athaydes.easyjetty;

import com.athaydes.easyjetty.http.MethodArbiter;
import com.athaydes.easyjetty.http.MethodArbiterFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * A user-provided Filter that may respond to a Request and decide whether or not
 * to allow further processing.
 */
public interface Filter {

    /**
     * Decide whether or not to allow this request to proceed further through the request chain.
     *
     * @param filterExchange exchange
     * @return true to allow further processing, false otherwise
     * @throws Exception
     */
    boolean allowFurther(FilterExchange filterExchange) throws Exception;

    class FilterExchange extends ExchangeBase {

        FilterExchange(ServletOutputStream out, HttpServletRequest request,
                       HttpServletResponse response,
                       Map<String, String> params,
                       ObjectSupport objectSupport) {
            super(out, request, response, params, objectSupport);
        }
    }

    class FilterAdapter {

        private static class FilterHandler extends AbstractHandler
                implements EasyJettyHandler {

            private static MethodArbiter arbiter = null;

            private final Filter filter;
            private final Map<Integer, String> parametersByIndex;
            private final ObjectSupport objectSupport;

            public FilterHandler(Filter filter,
                                 Map<Integer, String> parametersByIndex,
                                 ObjectSupport objectSupport) {
                this.filter = filter;
                this.parametersByIndex = parametersByIndex;
                this.objectSupport = objectSupport;
            }

            @Override
            public MethodArbiter getMethodArbiter() {
                if (arbiter == null) {
                    MethodArbiter.Method[] all = MethodArbiter.Method.values();
                    MethodArbiter.Method last = all[all.length - 1];
                    arbiter = MethodArbiterFactory.anyOf(
                            last, Arrays.copyOf(all, all.length - 1));
                }
                return arbiter;
            }

            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException {
                try {
                    Map<String, String> params = PathHelper.matchParams(
                            parametersByIndex, baseRequest.getPathInfo());
                    boolean allow = filter.allowFurther(
                            new FilterExchange(response.getOutputStream(), request,
                                    response, params, objectSupport));
                    if (!allow) {
                        baseRequest.setHandled(true);
                    }
                } catch (Exception e) {
                    throw new ServletException("Problem filtering request", e);
                }
            }
        }

        static EasyJettyHandler filterAsHandler(
                Filter filter, Map<Integer, String> parametersByIndex,
                ObjectSupport objectSupport) {
            return new FilterHandler(filter, parametersByIndex, objectSupport);
        }

    }

}
