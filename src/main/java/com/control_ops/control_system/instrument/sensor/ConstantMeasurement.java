package com.control_ops.control_system.instrument.sensor;

import com.control_ops.control_system.instrument.InstrumentId;
import com.control_ops.control_system.instrument.Signal;
import com.control_ops.control_system.instrument.SignalUnit;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ConstantMeasurement implements MeasurementBehaviour {
    private final double constant;

    public ConstantMeasurement(final double constant) {
        this.constant = constant;
    }

    @Override
    public Signal takeMeasurement(final InstrumentId instrumentId, final SignalUnit signalUnit, final ZoneId timeZone) {
        return new Signal(instrumentId, constant, signalUnit, ZonedDateTime.now(timeZone));
    }
}
