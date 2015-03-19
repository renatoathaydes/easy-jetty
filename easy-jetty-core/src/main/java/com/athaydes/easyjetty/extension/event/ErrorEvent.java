package com.athaydes.easyjetty.extension.event;

import com.athaydes.easyjetty.EasyJetty;

/**
 * Event that occurs when an error happens in EasyJetty.
 */
public class ErrorEvent extends BaseEasyJettyEvent {

    private final String message;
    private final Throwable throwable;

    public ErrorEvent(EasyJetty easyJetty, String message) {
        this(easyJetty, message, null);
    }


    public ErrorEvent(EasyJetty easyJetty, Throwable throwable) {
        this(easyJetty, throwable.getMessage(), throwable);
    }

    public ErrorEvent(EasyJetty easyJetty, String message, Throwable throwable) {
        super(easyJetty);
        this.message = message;
        this.throwable = throwable;
    }

    /**
     * @return the error message. May be empty.
     */
    public String getMessage() {
        return message == null ? "" : message;
    }

    /**
     * @return the Throwable if any, or null if none.
     */
    public Throwable getThrowable() {
        return throwable;
    }

}
