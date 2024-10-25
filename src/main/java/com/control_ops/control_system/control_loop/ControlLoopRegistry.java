package com.control_ops.control_system.control_loop;

import com.control_ops.control_system.instrument.actuator.Actuator;
import com.control_ops.control_system.instrument.sensor.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ControlLoopRegistry {
    private final Map<Sensor, ControlLoop> registeredSensors = new HashMap<>();
    private final Map<Actuator, ControlLoop> registeredActuators = new HashMap<>();
    private final Set<ControlLoop> registeredControlLoops = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(ControlLoopRegistry.class);

    public void registerControlLoop(
            final ControlLoop controlLoop,
            final Sensor sensor,
            final Actuator actuator) {
        if (registeredSensors.containsKey(sensor)) {
            logRegistrationError(sensor);
            throw new RegistrationDuplicationException(sensor);
        } else if (registeredActuators.containsKey(actuator)) {
            logRegistrationError(actuator);
            throw new RegistrationDuplicationException(actuator);
        } else if (registeredControlLoops.contains(controlLoop)) {
            logRegistrationError(controlLoop);
            throw new RegistrationDuplicationException(controlLoop);
        }
        registeredSensors.put(sensor, controlLoop);
        registeredActuators.put(actuator, controlLoop);
        registeredControlLoops.add(controlLoop);
    }

    static class RegistrationDuplicationException extends RuntimeException {
        private RegistrationDuplicationException(final Object object) {
            super(object + " has already been registered");
        }
    }

    private void logRegistrationError(final Object object) {
        logger.error("{} has already been registered; cannot complete registration", object);
    }

}
