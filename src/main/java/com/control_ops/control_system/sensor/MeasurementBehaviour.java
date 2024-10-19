package com.control_ops.control_system.sensor;

import java.time.ZoneId;

public interface MeasurementBehaviour {
    Signal takeMeasurement(
            final String sensorId,
            final SignalUnit signalUnit,
            final ZoneId timeZone);
}
