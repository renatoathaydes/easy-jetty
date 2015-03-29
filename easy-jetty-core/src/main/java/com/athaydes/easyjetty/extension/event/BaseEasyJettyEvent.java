package com.athaydes.easyjetty.extension.event;

import com.athaydes.easyjetty.EasyJetty;
import com.athaydes.easyjetty.extension.EasyJettyEvent;

/**
 *
 */
public abstract class BaseEasyJettyEvent implements EasyJettyEvent {

    private final long timestamp;
    private final EasyJetty easyJetty;

    public BaseEasyJettyEvent(EasyJetty easyJetty) {
        this.timestamp = System.currentTimeMillis();
        this.easyJetty = easyJetty;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public EasyJetty getEasyJetty() {
        return easyJetty;
    }

}
