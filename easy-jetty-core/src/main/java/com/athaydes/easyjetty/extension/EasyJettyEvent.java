package com.athaydes.easyjetty.extension;

import com.athaydes.easyjetty.EasyJetty;

/**
 * Base type of all EasyJetty events.
 */
public interface EasyJettyEvent {

    long getTimestamp();

    EasyJetty getEasyJetty();

}
