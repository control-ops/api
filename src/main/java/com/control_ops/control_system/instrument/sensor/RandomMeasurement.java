package com.control_ops.control_system.instrument.sensor;

import com.control_ops.control_system.instrument.Signal;
import com.control_ops.control_system.instrument.SignalUnit;

import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class RandomMeasurement implements MeasurementBehaviour {
    private final SecureRandom random = new SecureRandom();

    @Override
    public Signal takeMeasurement(final SignalUnit signalUnit, final ZoneId timeZone) {
        final double quantity = random.nextDouble();
        return new Signal(quantity, signalUnit, ZonedDateTime.now(timeZone));
    }
}
