package com.control_ops.control_system.sensor;

import java.time.ZonedDateTime;

public record Measurement (
        String sensorId,
        double quantity,
        MeasurementUnit unit,
        ZonedDateTime dateTime) {
}
