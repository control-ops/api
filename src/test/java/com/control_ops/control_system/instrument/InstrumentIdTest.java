package com.control_ops.control_system.instrument;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class InstrumentIdTest {
    @Test
    void testUniqueIds() {
        final String duplicatedId = "InstrumentIdTest::duplicatedId";
        new InstrumentId(duplicatedId);
        assertThatExceptionOfType(InstrumentId.IdAlreadyExistsException.class).isThrownBy(() ->
                new InstrumentId(duplicatedId));
    }
}
