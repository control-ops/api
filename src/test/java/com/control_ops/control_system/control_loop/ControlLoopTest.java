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

    private Actuator actuator;
    private Sensor sensor;

    private final long controlLoopUpdatePeriod = 40L;
    private final TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private final double gain = 1.0;
    private final OutputList outputList = new OutputList();
    private final ControlBehaviour controlBehaviour = new ProportionalControl(gain);
    private final double setPoint = 2.0;

    static int sensorCount = 0;
    static int actuatorCount = 0;
    static int controlLoopCount = 0;

    Sensor makeSensor() {
        sensorCount++;
        return new Sensor(
                sensorCount,
                20L,
                timeUnit,
                SignalUnit.CELSIUS,
                new ConstantMeasurement(0.0));
    }

    Actuator makeActuator() {
        actuatorCount++;
        return new Actuator(actuatorCount, 0.0);
    }

    ControlLoop makeDefaultControlLoop() {
        controlLoopCount++;
        return new ControlLoop(
                controlLoopCount,
                sensor,
                actuator,
                setPoint,
                controlLoopUpdatePeriod *2,
                timeUnit,
                controlBehaviour);
    }

    @BeforeEach
    void setUp() {
        actuator = makeActuator();
        sensor = makeSensor();
        actuator.addListener(outputList);
        sensor.startMeasuring();
    }

    private void waitForActuatorAdjustments(final int numAdjustments, final long periodMs) {
        final long maxWaitDurationMs = Math.max(500, 3*numAdjustments*periodMs);
        final int initialSize = outputList.getSignals().size();
        await().atMost(maxWaitDurationMs, timeUnit)
                .pollDelay(0L, TimeUnit.MILLISECONDS)
                .until(() -> outputList.getSignals().size() - initialSize >= numAdjustments);
    }

    @Test
    void testStartControlling() {
        final ControlLoop controlLoop = makeDefaultControlLoop();
        assertThatExceptionOfType(ConditionTimeoutException.class).isThrownBy(
                () -> waitForActuatorAdjustments(1, controlLoopUpdatePeriod));
        controlLoop.startControlling();
        waitForActuatorAdjustments(1, controlLoopUpdatePeriod);
    }

    @Test
    void testStopControlling() {
        final ControlLoop controlLoop = makeDefaultControlLoop();
        assertThatExceptionOfType(ConditionTimeoutException.class).isThrownBy(
                () -> waitForActuatorAdjustments(1, controlLoopUpdatePeriod));
        controlLoop.startControlling();
        waitForActuatorAdjustments(1, controlLoopUpdatePeriod);
        controlLoop.stopControlling();
        assertThatExceptionOfType(ConditionTimeoutException.class).isThrownBy(
                () -> waitForActuatorAdjustments(1, controlLoopUpdatePeriod));
    }

    @Test
    void testMultipleStartsAndStops() {
        final ControlLoop controlLoop = makeDefaultControlLoop();
        int previousSize = 0;
        controlLoop.startControlling();
        waitForActuatorAdjustments(1, controlLoopUpdatePeriod);
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
        previousSize = outputList.getSignals().size();

        controlLoop.startControlling();
        waitForActuatorAdjustments(1, controlLoopUpdatePeriod);
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
        previousSize = outputList.getSignals().size();

        controlLoop.startControlling();
        waitForActuatorAdjustments(1, controlLoopUpdatePeriod);
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
        previousSize = outputList.getSignals().size();

        controlLoop.startControlling();
        waitForActuatorAdjustments(1, controlLoopUpdatePeriod);
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
        previousSize = outputList.getSignals().size();

        controlLoop.startControlling();
        waitForActuatorAdjustments(1, controlLoopUpdatePeriod);
        assertThat(outputList.getSignals()).hasSizeGreaterThan(previousSize);
        controlLoop.stopControlling();
        previousSize = outputList.getSignals().size();

        controlLoop.startControlling();
        waitForActuatorAdjustments(1, controlLoopUpdatePeriod);
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
        waitForActuatorAdjustments(1, controlLoopUpdatePeriod);

        final double previousOutput = outputList.getSignals().getLast().quantity();

        controlLoop.switchControlBehaviour(new ProportionalControl(2*gain));
        waitForActuatorAdjustments(1, controlLoopUpdatePeriod);
        final double newOutput = outputList.getSignals().getLast().quantity();

        // The new control behaviour has double the gain of the previous behaviour, so if the switch was successful the
        // actuator output should double
        assertThat(newOutput).isEqualTo(2*previousOutput);
    }

    @ParameterizedTest
    @CsvSource({
            "100, 50",
            "500, 10",
            "1000, 5"
    })
    void testUpdateSequence(final long updatePeriodMs, final int numUpdates) {
        final ControlLoop controlLoop = new ControlLoop(
                ++controlLoopCount,
                sensor,
                actuator,
                setPoint,
                updatePeriodMs,
                TimeUnit.MILLISECONDS,
                controlBehaviour);

        controlLoop.startControlling();
        waitForActuatorAdjustments(numUpdates, updatePeriodMs);
        controlLoop.stopControlling();

        List<ZonedDateTime> updateTimes = new ArrayList<>(outputList.getSignals().size());
        outputList.getSignals().forEach(s -> updateTimes.add(s.dateTime()));

        PeriodicExecutorTest.assertExecutionSequence(updateTimes);
    }

    @ParameterizedTest
    @CsvSource({
            "100, 50",
            "500, 15",
            "1000, 10"
    })
    void testUpdatePeriod(final long updatePeriodMs, final int numUpdates) {

        final ControlLoop controlLoop = new ControlLoop(
                ++controlLoopCount,
                sensor,
                actuator,
                setPoint,
                updatePeriodMs,
                TimeUnit.MILLISECONDS,
                controlBehaviour);

        controlLoop.startControlling();
        waitForActuatorAdjustments(numUpdates, updatePeriodMs);
        controlLoop.stopControlling();

        List<ZonedDateTime> updateTimes = new ArrayList<>(outputList.getSignals().size());
        outputList.getSignals().forEach(s -> updateTimes.add(s.dateTime()));

        PeriodicExecutorTest.assertExecutionPeriod(
                updateTimes, updatePeriodMs, TimeUnit.MILLISECONDS, 0.01);
    }

    /**
     * Tests that the same Sensor cannot be used to instantiate multiple control loops.
     */
    @Test
    void testInstantiationDuplicatedSensor() {
        makeDefaultControlLoop();
        final Actuator newActuator = makeActuator();
        assertThatExceptionOfType(ControlLoopRegistry.RegistrationDuplicationException.class)
                .isThrownBy(() -> new ControlLoop(
                        ++controlLoopCount,
                        sensor,
                        newActuator,
                        1.0,
                        10L,
                        TimeUnit.MILLISECONDS,
                        controlBehaviour));

    }

    /**
     * Tests that the same Actuator cannot be used to instantiate multiple control loops.
     */
    @Test
    void testInstantiationDuplicatedActuator() {
        makeDefaultControlLoop();
        final Sensor newSensor = makeSensor();
        assertThatExceptionOfType(ControlLoopRegistry.RegistrationDuplicationException.class)
                .isThrownBy(() -> new ControlLoop(
                        ++controlLoopCount,
                        newSensor,
                        actuator,
                        1.0,
                        10L,
                        TimeUnit.MILLISECONDS,
                        controlBehaviour));
    }

}
