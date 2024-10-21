package com.control_ops.control_system.instrument.actuator;

import com.control_ops.control_system.instrument.Signal;

import java.util.ArrayList;
import java.util.List;

public class OutputList implements ActuatorListener {
    private final List<Signal> signals = new ArrayList<>();

    @Override
    public void onAdjustment(final Signal signal) {
        signals.add(signal);
    }

    List<Signal> getSignals() {
        return signals;
    }
}
