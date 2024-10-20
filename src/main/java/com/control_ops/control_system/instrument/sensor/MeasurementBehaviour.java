package com.control_ops.control_system.instrument.sensor;

import com.control_ops.control_system.instrument.InstrumentId;
import com.control_ops.control_system.instrument.Signal;
import com.control_ops.control_system.instrument.SignalUnit;

import java.time.ZoneId;

public interface MeasurementBehaviour {
    Signal takeMeasurement(
            final InstrumentId instrumentId,
            final SignalUnit signalUnit,
            final ZoneId timeZone);
}
