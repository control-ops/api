package com.control_ops.control_system.sensor;

import com.control_ops.control_system.instrument.Signal;
import com.control_ops.control_system.instrument.SignalUnit;

import java.time.ZoneId;

public interface MeasurementBehaviour {
    Signal takeMeasurement(
            final String sensorId,
            final SignalUnit signalUnit,
            final ZoneId timeZone);
}
