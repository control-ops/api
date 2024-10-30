package com.control_ops.control_system.control_loop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProportionalControl implements ControlBehaviour {
    final double gain;
    static final Logger logger = LoggerFactory.getLogger(ProportionalControl.class);

    ProportionalControl(double gain) {
        // A proportional controller with a gain of zero is useless and should never be instantiated
        if (gain == 0.0) {
            final String failureMessage = "Cannot instantiate a ProportionalControl object with a zero gain.";
            logger.error(failureMessage);
            throw new IllegalArgumentException(failureMessage);
        }
        this.gain = gain;
    }

    @Override
    public double calculateActuatorOutput(
            final double setPoint,
            final double controlledVariableValue) {
        return gain * (setPoint - controlledVariableValue);
    }
}
