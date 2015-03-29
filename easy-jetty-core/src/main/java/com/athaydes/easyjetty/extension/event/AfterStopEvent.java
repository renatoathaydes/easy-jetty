package com.athaydes.easyjetty.extension.event;

import com.athaydes.easyjetty.EasyJetty;

/**
 * Event created right after the EasyJetty Server stops.
 */
public class AfterStopEvent extends BaseEasyJettyEvent {

    public AfterStopEvent(EasyJetty easyJetty) {
        super(easyJetty);
    }

}
