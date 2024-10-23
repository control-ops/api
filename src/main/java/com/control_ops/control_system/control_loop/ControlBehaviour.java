package com.control_ops.control_system.control_loop;

public interface ControlBehaviour {
    double calculateActuatorValue(
            final double currentSetPoint,
            final double currentControlledVariable);
}
