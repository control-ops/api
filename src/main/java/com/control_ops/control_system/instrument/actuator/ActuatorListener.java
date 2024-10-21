package com.control_ops.control_system.instrument.actuator;

import com.control_ops.control_system.instrument.Signal;

public interface ActuatorListener {
    void onAdjustment(final Signal signal);
}
