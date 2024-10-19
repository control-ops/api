package com.control_ops.control_system.sensor;

import java.util.ArrayList;
import java.util.List;

public class MeasurementList implements SensorListener {
    private final List<Measurement> measurements = new ArrayList<>();

    @Override
    public void onMeasurement(final Measurement measurement) {
        measurements.add(measurement);
    }

    List<Measurement> getMeasurements() {
        return measurements;
    }
}
