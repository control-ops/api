package com.control_ops.control_system.control_loop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ProportionalControlTest {

    @Test
    void testInstantiation() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new ProportionalControl(0.0));
    }

    @ParameterizedTest
    @CsvSource({
            // Gain changes
            "1.0, 1.0, 0.0",
            "2.0, 1.0, 0.0",
            "-1.0, 1.0, 0.0",

            // Set point changes
            "1.0, 1.0, 1.0",
            "1.0, -1.0, 1.0",
            "1.0, 0.0, 1.0",

            // Controlled variable changes
            "1.0, 1.0, 1.0",
            "1.0, 1.0, 3.0",
            "1.0, 1.0, -1.0",
    })
    void testCalculateActuatorOutput(
            final double gain,
            final double setPoint,
            final double controlledVariableValue) {
        final double expectedOutput = gain * (setPoint - controlledVariableValue);
        final ProportionalControl proportionalControl = new ProportionalControl(gain);
        assertThat(proportionalControl.calculateActuatorOutput(setPoint, controlledVariableValue)).isEqualTo(expectedOutput);
    }
}
