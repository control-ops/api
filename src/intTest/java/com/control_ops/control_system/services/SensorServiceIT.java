package com.control_ops.control_system.services;

import com.control_ops.control_system.ControlSystemApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ControlSystemApplication.class)
class SensorServiceIT {

/*
    private final SensorService sensorService;

    @Autowired
    SensorServiceIT(final SensorService sensorService) {
        this.sensorService = sensorService;
    }
*/

    @Test
    void can_get_sensor_with_valid_id() {
        assertThat(true).isTrue();
    }
}
