package com.control_ops.control_system.instrument;

import java.time.ZonedDateTime;

public record Signal(
        String sensorId,
        double quantity,
        SignalUnit unit,
        ZonedDateTime dateTime) {
}
