package com.control_ops.control_system.control_loop;

import com.control_ops.control_system.instrument.SignalUnit;
import com.control_ops.control_system.instrument.actuator.Actuator;
import com.control_ops.control_system.instrument.actuator.OutputList;
import com.control_ops.control_system.instrument.sensor.ConstantMeasurement;
import com.control_ops.control_system.instrument.sensor.Sensor;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.awaitility.Awaitility.await;

class ControlLoopTest {

    private final long sensorSamplingPeriod = 20L;
    private final long controllerSamplingPeriod = 40L;
    private final TimeUnit samplingPeriodUnit = TimeUnit.MILLISECONDS;
    private final double gain = 1.0;
    private final OutputList outputList = new OutputList();
    private final Actuator actuator = new Actuator("actuator" + testCount, 0.0);
    private final Sensor sensor = new Sensor(
            "sensor" + testCount, sensorSamplingPeriod, samplingPeriodUnit, SignalUnit.CELSIUS, new ConstantMeasurement(0.0));
    private final ControlBehaviour controlBehaviour = new ProportionalControl(gain);
    private final double setPoint = 2.0;
    private final ControlLoop controlLoop = new ControlLoop(
            sensor,
            actuator,
            setPoint,
            controllerSamplingPeriod*2,
            samplingPeriodUnit,
            controlBehaviour);

    static long testCount = 1;
    @BeforeEach
    void setUp() {
        actuator.addListener(outputList);
        sensor.startMeasuring();
        testCount++;
    }

    private void waitForActuatorAdjustments() {
        final long initialSize = outputList.getSignals().size();
        await().atMost(
                5*controllerSamplingPeriod,
                samplingPeriodUnit).until(() -> outputList.getSignals().size() > initialSize);
    }

    @Test
    void testStartControlling() {
        assertThatExceptionOfType(ConditionTimeoutException.class).isThrownBy(
                this::waitForActuatorAdjustments);
        controlLoop.startControlling();
        waitForActuatorAdjustments();
    }

    @Test
    void testStopControlling() {
        assertThatExceptionOfType(ConditionTimeoutException.class).isThrownBy(
                this::waitForActuatorAdjustments);
        controlLoop.startControlling();
        waitForActuatorAdjustments();
        controlLoop.stopControlling();
        assertThatExceptionOfType(ConditionTimeoutException.class).isThrownBy(
                this::waitForActuatorAdjustments);
    }

    @Test
    void testMultipleStartsAndStops() {
        int previousSize = 0;
        controlLoop.startControlling();
        waitForActuatorAdjustments();
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
        previousSize = outputList.getSignals().size();

        controlLoop.startControlling();
        waitForActuatorAdjustments();
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
        previousSize = outputList.getSignals().size();

        controlLoop.startControlling();
        waitForActuatorAdjustments();
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
        previousSize = outputList.getSignals().size();

        controlLoop.startControlling();
        waitForActuatorAdjustments();
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
        previousSize = outputList.getSignals().size();

        controlLoop.startControlling();
        waitForActuatorAdjustments();
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
        previousSize = outputList.getSignals().size();

        controlLoop.startControlling();
        waitForActuatorAdjustments();
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
    }

    @Test
    void testAdjustSetPoint() {
        controlLoop.updateSetPoint(2*setPoint);
        assertThat(controlLoop.getSetPoint()).isEqualTo(2*setPoint);
    }

}
