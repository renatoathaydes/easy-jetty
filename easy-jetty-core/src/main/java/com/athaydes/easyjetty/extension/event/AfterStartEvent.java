package com.athaydes.easyjetty.extension.event;

import com.athaydes.easyjetty.EasyJetty;

/**
 * Event created right after the EasyJetty Server has stopped.
 */
public class AfterStartEvent extends BaseEasyJettyEvent {

    public AfterStartEvent(EasyJetty easyJetty) {
        super(easyJetty);
    }

}
