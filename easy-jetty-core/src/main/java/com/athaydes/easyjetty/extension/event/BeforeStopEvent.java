package com.athaydes.easyjetty.extension.event;

import com.athaydes.easyjetty.EasyJetty;

/**
 * Event created just before the EasyJetty server stops.
 */
public class BeforeStopEvent extends BaseEasyJettyEvent {

    public BeforeStopEvent(EasyJetty easyJetty) {
        super(easyJetty);
    }

}
