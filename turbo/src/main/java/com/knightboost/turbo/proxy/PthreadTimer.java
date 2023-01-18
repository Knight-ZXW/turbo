package com.knightboost.turbo.proxy;

import java.util.Timer;

public class PthreadTimer extends Timer {
    public PthreadTimer() {
    }

    public PthreadTimer(String name) {
        super(name);
    }

    public PthreadTimer(String name, boolean isDaemon) {
        super(name, isDaemon);
    }

    public PthreadTimer(boolean isDaemon) {
        super(isDaemon);
    }
}
