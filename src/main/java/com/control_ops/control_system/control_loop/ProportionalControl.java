package com.control_ops.control_system.control_loop;

public class ProportionalControl implements ControlBehaviour {
    double gain;

    ProportionalControl(double gain) {
        this.gain = gain;
    }

    @Override
    public double calculateActuatorValue(
            final double currentSetPoint,
            final double currentControlledVariable) {
        return gain * (currentSetPoint - currentControlledVariable);
    }
}
