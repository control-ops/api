package com.control_ops.control_system.instrument.actuator;

import com.control_ops.control_system.instrument.Signal;
import com.control_ops.control_system.instrument.SignalUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Actuator {
    private double signalValue;
    private final int id;
    private final List<ActuatorListener> actuatorListeners;
    private static final Logger logger = LoggerFactory.getLogger(Actuator.class);

    public Actuator(final int id, final double initialSignalValue) {
        this.id = id;
        this.signalValue = initialSignalValue;
        this.actuatorListeners = new ArrayList<>();
        logger.info("A new Actuator was created.\tInstrument ID: {}\tInitial signal value: {}", id, initialSignalValue);
    }

    public void addListener(final ActuatorListener actuatorListener) {
        if (this.actuatorListeners.contains(actuatorListener)) {
            logger.warn("Cannot add the provided ActuatorListener; it is already subscribed to {}", id);
            return;
        }
        this.actuatorListeners.add(actuatorListener);
        logger.info("The provided ActuatorListener was added to {}", id);
    }

    public void removeListener(final ActuatorListener actuatorListener) {
        if (!this.actuatorListeners.contains(actuatorListener)) {
            logger.warn("Cannot remove the provided ActuatorListener; it is not subscribed to {}", id);
            return;
        }
        this.actuatorListeners.remove(actuatorListener);
        logger.info("The provided ActuatorListener was removed from {}", id);
    }

    public void adjustSignal(final double newSignalValue) {
        logger.info("Adjusting signal of {}.\tNew signal value: {}", id, newSignalValue);
        signalValue = newSignalValue;
        final Signal newSignal = new Signal(
                signalValue,
                SignalUnit.PERCENTAGE,
                ZonedDateTime.now(ZoneId.of("UTC")));
        for (final ActuatorListener actuatorListener : this.actuatorListeners) {
            actuatorListener.onAdjustment(newSignal);
        }
    }

    public double getSignalValue() {
        return signalValue;
    }

    @Override
    public String toString() {
        return "Actuator" + id;
    }
}
