package com.control_ops.control_system.instrument.sensor;

import com.control_ops.control_system.instrument.InstrumentId;
import com.control_ops.control_system.instrument.Signal;
import com.control_ops.control_system.instrument.SignalUnit;

import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SampledMeasurement implements MeasurementBehaviour {
    private final SecureRandom random = new SecureRandom();

    @Override
    public Signal takeMeasurement(final InstrumentId instrumentId, final SignalUnit signalUnit, final ZoneId timeZone) {
        final double quantity = random.nextDouble();
        return new Signal(instrumentId, quantity, signalUnit, ZonedDateTime.now(timeZone));
    }
}
