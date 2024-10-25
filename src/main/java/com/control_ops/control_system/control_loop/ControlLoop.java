package com.control_ops.control_system.control_loop;

import com.control_ops.control_system.PeriodicExecutor;
import com.control_ops.control_system.instrument.actuator.Actuator;
import com.control_ops.control_system.instrument.sensor.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ControlLoop {

    private double setPoint;
    private ControlBehaviour controlBehaviour;

    private final Sensor controlledVariable;
    private final Actuator manipulatedVariable;
    private final PeriodicExecutor periodicExecutor;
    private static final ControlLoopRegistry controlLoopRegistry = new ControlLoopRegistry();

    private static final Logger logger = LoggerFactory.getLogger(ControlLoop.class);

    public ControlLoop(
            final Sensor controlledVariable,
            final Actuator manipulatedVariable,
            final double setPoint,
            final long updatePeriod,
            final TimeUnit updatePeriodUnit,
            final ControlBehaviour controlBehaviour) {
        controlLoopRegistry.registerControlLoop(this, controlledVariable, manipulatedVariable);

        this.controlledVariable = controlledVariable;
        this.manipulatedVariable = manipulatedVariable;
        this.setPoint = setPoint;
        this.periodicExecutor = new PeriodicExecutor("test", updatePeriod, updatePeriodUnit, this::updateManipulatedVariable);
        this.controlBehaviour = controlBehaviour;
    }

    public void startControlling() {
        periodicExecutor.start();
    }

    public void stopControlling() {
        periodicExecutor.stop();
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

    @Override
    public String toString() {
        return "control loop";
    }
}
