package com.athaydes.easyjetty;

import com.athaydes.easyjetty.http.MethodArbiter;
import org.eclipse.jetty.server.Handler;

interface EasyJettyHandler extends Handler {

    MethodArbiter getMethodArbiter();

}
