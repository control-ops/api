package com.control_ops.control_system.control_loop;

import com.control_ops.control_system.PeriodicExecutorTest;
import com.control_ops.control_system.instrument.SignalUnit;
import com.control_ops.control_system.instrument.actuator.Actuator;
import com.control_ops.control_system.instrument.actuator.OutputList;
import com.control_ops.control_system.instrument.sensor.ConstantMeasurement;
import com.control_ops.control_system.instrument.sensor.Sensor;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.awaitility.Awaitility.await;

class ControlLoopTest {

    private final long sensorSamplingPeriod = 20L;
    private final long controlLoopUpdatePeriod = 40L;
    private final TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private final double gain = 1.0;
    private final OutputList outputList = new OutputList();
    private final Actuator actuator = new Actuator("actuator" + testCount, 0.0);
    private final Sensor sensor = new Sensor(
            "sensor" + testCount, sensorSamplingPeriod, timeUnit, SignalUnit.CELSIUS, new ConstantMeasurement(0.0));
    private final ControlBehaviour controlBehaviour = new ProportionalControl(gain);
    private final double setPoint = 2.0;

    static long testCount = 1;

    ControlLoop makeDefaultControlLoop() {
        return new ControlLoop(
                sensor,
                actuator,
                setPoint,
                controlLoopUpdatePeriod *2,
                timeUnit,
                controlBehaviour);
    }

    @BeforeEach
    void setUp() {
        actuator.addListener(outputList);
        sensor.startMeasuring();
        testCount++;
    }
    
    private void waitForActuatorAdjustments(final int numAdjustments, final long maxWaitDurationMs) {
        final int initialSize = outputList.getSignals().size();
        await().atMost(maxWaitDurationMs, timeUnit)
                .pollDelay(maxWaitDurationMs / 2, TimeUnit.MILLISECONDS)
                .until(() -> outputList.getSignals().size() - initialSize >= numAdjustments);
    }

    @Test
    void testStartControlling() {
        final ControlLoop controlLoop = makeDefaultControlLoop();
        assertThatExceptionOfType(ConditionTimeoutException.class).isThrownBy(
                () -> waitForActuatorAdjustments(1, 2*controlLoopUpdatePeriod));
        controlLoop.startControlling();
        waitForActuatorAdjustments(1, 2*controlLoopUpdatePeriod);
    }

    @Test
    void testStopControlling() {
        final ControlLoop controlLoop = makeDefaultControlLoop();
        assertThatExceptionOfType(ConditionTimeoutException.class).isThrownBy(
                () -> waitForActuatorAdjustments(1, 2*controlLoopUpdatePeriod));
        controlLoop.startControlling();
        waitForActuatorAdjustments(1, 2*controlLoopUpdatePeriod);
        controlLoop.stopControlling();
        assertThatExceptionOfType(ConditionTimeoutException.class).isThrownBy(
                () -> waitForActuatorAdjustments(1, 2*controlLoopUpdatePeriod));
    }

    @Test
    void testMultipleStartsAndStops() {
        final ControlLoop controlLoop = makeDefaultControlLoop();
        int previousSize = 0;
        controlLoop.startControlling();
        waitForActuatorAdjustments(1, 2*controlLoopUpdatePeriod);
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
        previousSize = outputList.getSignals().size();

        controlLoop.startControlling();
        waitForActuatorAdjustments(1, 2*controlLoopUpdatePeriod);
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
        previousSize = outputList.getSignals().size();

        controlLoop.startControlling();
        waitForActuatorAdjustments(1, 2*controlLoopUpdatePeriod);
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
        previousSize = outputList.getSignals().size();

        controlLoop.startControlling();
        waitForActuatorAdjustments(1, 2*controlLoopUpdatePeriod);
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
        previousSize = outputList.getSignals().size();

        controlLoop.startControlling();
        waitForActuatorAdjustments(1, 2*controlLoopUpdatePeriod);
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
        previousSize = outputList.getSignals().size();

        controlLoop.startControlling();
        waitForActuatorAdjustments(1, 2*controlLoopUpdatePeriod);
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
    }

    @Test
    void testAdjustSetPoint() {
        final ControlLoop controlLoop = makeDefaultControlLoop();
        controlLoop.updateSetPoint(2*setPoint);
        assertThat(controlLoop.getSetPoint()).isEqualTo(2*setPoint);
    }

    @Test
    void testSwitchControlBehaviour() {
        final ControlLoop controlLoop = makeDefaultControlLoop();
        controlLoop.startControlling();
        waitForActuatorAdjustments(1, 2*controlLoopUpdatePeriod);

        final double previousOutput = outputList.getSignals().getLast().quantity();

        controlLoop.switchControlBehaviour(new ProportionalControl(2*gain));
        waitForActuatorAdjustments(1, 2*controlLoopUpdatePeriod);
        final double newOutput = outputList.getSignals().getLast().quantity();

        // The new control behaviour has double the gain of the previous behaviour, so if the switch was successful the
        // actuator output should double
        assertThat(newOutput).isEqualTo(2*previousOutput);
    }


    @ParameterizedTest
    @CsvSource({
            "100, 50",
            "500, 15",
            "1000, 10"
    })
    void testUpdatePeriod(final long updatePeriodMs, final int numAdjustments) {

        final ControlLoop controlLoop = new ControlLoop(
                sensor,
                actuator,
                setPoint,
                updatePeriodMs,
                TimeUnit.MILLISECONDS,
                controlBehaviour);

        controlLoop.startControlling();
        waitForActuatorAdjustments(numAdjustments, (numAdjustments + 2)*updatePeriodMs);
        controlLoop.stopControlling();

        List<ZonedDateTime> updateTimes = new ArrayList<>(outputList.getSignals().size());
        outputList.getSignals().forEach(s -> updateTimes.add(s.dateTime()));

        PeriodicExecutorTest.assertExecutionPeriod(
                updateTimes, updatePeriodMs, TimeUnit.MILLISECONDS, 0.01);
    }
}
