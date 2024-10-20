package com.control_ops.control_system.instrument;

public enum SignalUnit {
    CELSIUS(PhysicalProperty.TEMPERATURE),
    FAHRENHEIT(PhysicalProperty.TEMPERATURE),
    M3_PER_HOUR(PhysicalProperty.VOLUMETRIC_FLOW);

    SignalUnit(final PhysicalProperty physicalProperty) {
        this.physicalProperty = physicalProperty;
    }

    private final PhysicalProperty physicalProperty;

    PhysicalProperty getPhysicalProperty() {
        return physicalProperty;
    }
}
