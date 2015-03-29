package com.athaydes.easyjetty.extension.event;

import com.athaydes.easyjetty.EasyJetty;

/**
 * Event created just before the EasyJetty Server starts.
 */
public class BeforeStartEvent extends BaseEasyJettyEvent {

    public BeforeStartEvent(EasyJetty easyJetty) {
        super(easyJetty);
    }

}
