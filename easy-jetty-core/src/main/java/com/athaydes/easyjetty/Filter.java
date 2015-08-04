package com.athaydes.easyjetty;

import org.eclipse.jetty.servlet.FilterHolder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A user-provided Filter that may respond to a Request.
 * <p/>
 * If the filter responds in any way (by writing to the OutputStream, sending an
 * Object or setting the response status), the request is deemed to have been
 * stopped by this Filter and no further processing occurs.
 * <p/>
 * Reading Request data has no effect on whether or not the Request is filtered.
 * <p/>
 * The Filter default DispatchType is REQUEST. If a different value is required,
 * use {@link FilterWithDispatchTypes} instead.
 */
public interface Filter {

    boolean allowFurther(FilterExchange filterExchange) throws Exception;

    class FilterExchange extends ExchangeBase {

        FilterExchange(ServletOutputStream out, HttpServletRequest request, HttpServletResponse response, ObjectSupport objectSupport) {
            super(out, request, response, objectSupport);
        }
    }

    class FilterAdapter {

        static FilterHolder asFilterHolder(final Filter filter,
                                           final ObjectSupport objectSupport) {
            return new FilterHolder(new javax.servlet.Filter() {
                @Override
                public void init(FilterConfig filterConfig) throws ServletException {
                    // nothing to do
                }

                @Override
                public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                        throws IOException, ServletException {
                    boolean allowFurther = true;
                    if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                        try {
                            allowFurther = filter.allowFurther(new FilterExchange(response.getOutputStream(),
                                    (HttpServletRequest) request, (HttpServletResponse) response,
                                    objectSupport));
                        } catch (Exception e) {
                            e.printStackTrace();
                            allowFurther = false;
                        }
                    }
                    if (!response.isCommitted() && allowFurther) {
                        chain.doFilter(request, response);
                    }
                }

                @Override
                public void destroy() {
                    // nothing to do
                }
            });
        }
    }

}
