package com.control_ops.control_system.instrument.actuator;

import com.control_ops.control_system.instrument.InstrumentId;
import com.control_ops.control_system.instrument.Signal;
import com.control_ops.control_system.instrument.SignalUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Actuator {
    private static final Logger logger = LoggerFactory.getLogger(Actuator.class);
    private final InstrumentId instrumentId;
    private final List<ActuatorListener> actuatorListeners;
    private double signalValue;

    Actuator(final String instrumentId, final double initialSignalValue) {
        this.instrumentId = new InstrumentId(instrumentId);
        this.signalValue = initialSignalValue;
        this.actuatorListeners = new ArrayList<>();
        logger.info("A new Actuator was created.\tInstrument ID: {}\tInitial signal value: {}", instrumentId, initialSignalValue);
    }

    public void addListener(final ActuatorListener actuatorListener) {
        if (this.actuatorListeners.contains(actuatorListener)) {
            logger.warn("Cannot add the provided ActuatorListener; it is already subscribed to {}", instrumentId);
            return;
        }
        this.actuatorListeners.add(actuatorListener);
        logger.info("The provided ActuatorListener was added to {}", instrumentId);
    }

    public void removeListener(final ActuatorListener actuatorListener) {
        if (!this.actuatorListeners.contains(actuatorListener)) {
            logger.warn("Cannot remove the provided ActuatorListener; it is not subscribed to {}", instrumentId);
            return;
        }
        this.actuatorListeners.remove(actuatorListener);
        logger.info("The provided ActuatorListener was removed from {}", instrumentId);
    }

    public void adjustSignal(final double newSignalValue) {
        logger.info("Adjusting signal of {}.\tNew signal value: {}", instrumentId, newSignalValue);
        signalValue = newSignalValue;
        final Signal newSignal = new Signal(
                instrumentId,
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
}
