package com.control_ops.control_system.sensor;

import java.time.ZoneId;

public interface MeasurementBehaviour {
    Measurement takeMeasurement(
            final String sensorId,
            final MeasurementUnit measurementUnit,
            final ZoneId timeZone);
}
