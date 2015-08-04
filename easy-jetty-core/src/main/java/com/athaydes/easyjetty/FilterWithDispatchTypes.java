package com.athaydes.easyjetty;

import javax.servlet.DispatcherType;

/**
 * A {@link Filter} with non-default dispatch types.
 */
public interface FilterWithDispatchTypes extends Filter {

    /**
     * @return the dispatch types for this Filter.
     * If an empty array is returned, the default DispatchType for Filter is used.
     */
    DispatcherType[] getDispatchTypes();

}
