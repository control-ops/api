package com.control_ops.control_system.instrument.sensor;

import com.control_ops.control_system.instrument.Signal;

import java.util.ArrayList;
import java.util.List;

public class MeasurementList implements SensorListener {
    private final List<Signal> signals = new ArrayList<>();

    @Override
    public void onMeasurement(final Signal signal) {
        signals.add(signal);
    }

    List<Signal> getMeasurements() {
        return signals;
    }
}
