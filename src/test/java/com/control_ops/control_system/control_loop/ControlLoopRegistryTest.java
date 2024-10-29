package com.control_ops.control_system.control_loop;

import com.control_ops.control_system.instrument.SignalUnit;
import com.control_ops.control_system.instrument.actuator.Actuator;
import com.control_ops.control_system.instrument.sensor.RandomMeasurement;
import com.control_ops.control_system.instrument.sensor.Sensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ControlLoopRegistryTest {
    private static long sensorCount = 0;
    private static long actuatorCount = 0;

    private Sensor sensor1;
    private Actuator actuator1;
    private ControlLoop controlLoop1;

    private final ControlLoopRegistry registry = new ControlLoopRegistry();

    private static Sensor makeSensor() {
        sensorCount++;
        return new Sensor(
                "ControlLoopRegistryTest::sensor" + sensorCount,
                100L,
                TimeUnit.MILLISECONDS,
                SignalUnit.CELSIUS,
                new RandomMeasurement());
    }

    private static Actuator makeActuator() {
        actuatorCount++;
        return new Actuator(
                "ControlLoopRegistryTest::actuator" + actuatorCount,
                0.0);
    }

    private static ControlLoop makeControlLoop(final Sensor sensor, final Actuator actuator) {
        return new ControlLoop(sensor, actuator, 1.0, 200, TimeUnit.MILLISECONDS, new ProportionalControl(1.0));
    }

    @BeforeEach
    void setUp() {
        sensorCount++;
        sensor1 = makeSensor();
        actuator1 = makeActuator();
        controlLoop1 = makeControlLoop(sensor1, actuator1);
    }

    /**
     * Tests that registering the same sensor to two different control loops throws an exception.
     */
    @Test
    void testSensorDuplication() {
        registry.registerControlLoop(controlLoop1, sensor1, actuator1);

        final Sensor sensor2 = makeSensor();
        final Actuator actuator2 = makeActuator();
        final ControlLoop controlLoop2 = makeControlLoop(sensor2, actuator2);

        assertThatExceptionOfType(ControlLoopRegistry.RegistrationDuplicationException.class)
                .isThrownBy(() -> registry.registerControlLoop(controlLoop2, sensor1, actuator2));

    }

    /**
     * Tests that registering the same actuator to two different control loops throws an exception.
     */
    @Test
    void testActuatorDuplication() {
        registry.registerControlLoop(controlLoop1, sensor1, actuator1);

        final Sensor sensor2 = makeSensor();
        final Actuator actuator2 = makeActuator();
        final ControlLoop controlLoop2 = makeControlLoop(sensor2, actuator2);

        assertThatExceptionOfType(ControlLoopRegistry.RegistrationDuplicationException.class)
                .isThrownBy(() -> registry.registerControlLoop(controlLoop2, sensor2, actuator1));

    }

    /**
     * Tests that registering the same control loop twice throws an exception.
     */
    @Test
    void testControlLoopDuplication() {
        registry.registerControlLoop(controlLoop1, sensor1, actuator1);

        final Sensor sensor2 = makeSensor();
        final Actuator actuator2 = makeActuator();

        assertThatExceptionOfType(ControlLoopRegistry.RegistrationDuplicationException.class)
                .isThrownBy(() -> registry.registerControlLoop(controlLoop1, sensor2, actuator2));
    }
}
