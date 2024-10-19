package com.control_ops.control_system.sensor;

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
