package com.control_ops.control_system.instrument.actuator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ActuatorTest {
    @Test
    void testAddListener() {
        final OutputList outputList = new OutputList();
        final Actuator actuator = new Actuator(1, 0.0);
        actuator.adjustSignal(50.0);
        assertThat(outputList.getSignals()).isEmpty();

        actuator.addListener(outputList);
        actuator.adjustSignal(25.0);

        assertThat(outputList.getSignals()).hasSize(1);
        assertThat(outputList.getSignals().getFirst().quantity()).isEqualTo(25.0);
    }

    @Test
    void testRemoveListener() {
        final OutputList outputList = new OutputList();
        final Actuator actuator = new Actuator(2, 0.0);
        actuator.addListener(outputList);
        actuator.adjustSignal(25.0);
        actuator.removeListener(outputList);
        actuator.adjustSignal(50.0);

        assertThat(outputList.getSignals()).hasSize(1);
        assertThat(outputList.getSignals().getFirst().quantity()).isEqualTo(25.0);
    }

    @Test
    void testAdjustSignal() {
        final Actuator actuator = new Actuator(3, 0.0);
        actuator.adjustSignal(25.0);
        assertThat(actuator.getSignalValue()).isEqualTo(25.0);
        actuator.adjustSignal(50.0);
        assertThat(actuator.getSignalValue()).isEqualTo(50.0);
    }

}
