package com.control_ops.control_system.control_loop;

import com.control_ops.control_system.instrument.actuator.Actuator;
import com.control_ops.control_system.instrument.sensor.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ControlLoop {

    private double setPoint;
    private ControlBehaviour controlBehaviour;
    private boolean isControlling = false;

    private final Sensor controlledVariable;
    private final Actuator manipulatedVariable;
    private final long samplingPeriod;
    private final TimeUnit samplingPeriodUnit;
    private ScheduledExecutorService scheduler;

    private static final Logger logger = LoggerFactory.getLogger(ControlLoop.class);

    ControlLoop(
            final Sensor controlledVariable,
            final Actuator manipulatedVariable,
            final double setPoint,
            final long samplingPeriod,
            final TimeUnit samplingPeriodUnit,
            final ControlBehaviour controlBehaviour) {
        this.controlledVariable = controlledVariable;
        this.manipulatedVariable = manipulatedVariable;
        this.setPoint = setPoint;
        this.samplingPeriod = samplingPeriod;
        this.samplingPeriodUnit = samplingPeriodUnit;
        this.controlBehaviour = controlBehaviour;
    }

    public void startControlling() {
        if (!isControlling) {
            this.scheduler = Executors.newScheduledThreadPool(1);
            this.scheduler.scheduleAtFixedRate(
                    this::updateManipulatedVariable,
                    0L,
                    samplingPeriod,
                    samplingPeriodUnit);
            isControlling = true;
            logger.info("{} control loop started", controlledVariable.getInstrumentID());
        } else {
            logger.warn("{} control loop is already controlling", controlledVariable.getInstrumentID());
        }
    }

    public void stopControlling() {
        if (isControlling) {
            this.scheduler.shutdown();
            isControlling = false;
            logger.info("{} control loop stopped", controlledVariable.getInstrumentID());
        } else {
            logger.warn("Cannot stop {} control loop; it is already in manual mode", controlledVariable.getInstrumentID());
        }
    }

    public void updateSetPoint(final double newSetPoint) {
        final double oldSetPoint = setPoint;
        setPoint = newSetPoint;
        logger.info("Set point updated from {} tp {} on control loop {}",
                oldSetPoint,
                newSetPoint,
                controlledVariable.getInstrumentID());
    }

    public void switchControlBehaviour(final ControlBehaviour newControlBehaviour) {
        final ControlBehaviour oldControlBehaviour = this.controlBehaviour;
        this.controlBehaviour = newControlBehaviour;
        logger.info("Control behaviour switched from {} tp {} on control loop {}",
                oldControlBehaviour,
                newControlBehaviour,
                controlledVariable.getInstrumentID());
    }

    public double getSetPoint() {
        return setPoint;
    }

    private synchronized void updateManipulatedVariable() {
        final double newActuatorOutput = controlBehaviour.calculateActuatorValue(
                setPoint,
                controlledVariable.getCurrentSignal().quantity()
        );
        manipulatedVariable.adjustSignal(newActuatorOutput);
    }
}
