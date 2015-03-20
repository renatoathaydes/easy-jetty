package com.athaydes.easyjetty.extension.event;

import com.athaydes.easyjetty.EasyJetty;
import com.athaydes.easyjetty.extension.EasyJettyExtension;

/**
 * Event triggered when an Extension is added to EasyJetty.
 * All Extensions, including the one that has been added, will receive this Event.
 */
public class ExtensionAddedEvent extends BaseEasyJettyEvent {

    private final EasyJettyExtension extension;

    public ExtensionAddedEvent(EasyJetty easyJetty, EasyJettyExtension extension) {
        super(easyJetty);
        this.extension = extension;
    }

    public EasyJettyExtension getExtension() {
        return extension;
    }
}
