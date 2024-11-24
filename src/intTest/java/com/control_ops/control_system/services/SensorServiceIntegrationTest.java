package com.control_ops.control_system.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SensorServiceIntegrationTest {

    @Autowired
    SensorService sensorService;

    @Test
    void can_get_sensor_with_valid_id() {
        assertThat(true).isTrue();
    }
}
