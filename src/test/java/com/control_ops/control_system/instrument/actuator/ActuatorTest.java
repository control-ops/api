package com.control_ops.control_system.instrument.actuator;

import com.control_ops.control_system.instrument.InstrumentId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ActuatorTest {
    @Test
    void testInitialization() {
        new Actuator("duplicatedId", 0d);
        assertThatExceptionOfType(InstrumentId.IdAlreadyExistsException.class).isThrownBy(() ->
                new Actuator("duplicatedId", 0d));
    }

    @Test
    void testAddListener() {
        final OutputList outputList = new OutputList();
        final Actuator actuator = new Actuator("ActuatorTest::actuator1", 0.0);
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
        final Actuator actuator = new Actuator("ActuatorTest::actuator2", 0.0);
        actuator.addListener(outputList);
        actuator.adjustSignal(25.0);
        actuator.removeListener(outputList);
        actuator.adjustSignal(50.0);

        assertThat(outputList.getSignals()).hasSize(1);
        assertThat(outputList.getSignals().getFirst().quantity()).isEqualTo(25.0);
    }

    @Test
    void testAdjustSignal() {
        final Actuator actuator = new Actuator("ActuatorTest::actuator3", 0.0);
        actuator.adjustSignal(25.0);
        assertThat(actuator.getSignalValue()).isEqualTo(25.0);
        actuator.adjustSignal(50.0);
        assertThat(actuator.getSignalValue()).isEqualTo(50.0);
    }

}
