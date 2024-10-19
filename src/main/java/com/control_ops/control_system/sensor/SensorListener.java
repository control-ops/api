package com.control_ops.control_system.sensor;

import com.control_ops.control_system.instrument.Signal;

public interface SensorListener {
    void onMeasurement(final Signal signal);
}
