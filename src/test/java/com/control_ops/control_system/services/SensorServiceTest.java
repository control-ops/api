package com.control_ops.control_system.services;

import com.control_ops.control_system.instrument.SignalUnit;
import com.control_ops.control_system.models.SensorModel;
import com.control_ops.control_system.repositories.SensorRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

class SensorServiceTest {
    private SensorService sensorService;

    private final long sensorId = 1L;
    private final long samplingPeriod = 10L;
    private final TimeUnit samplingPeriodUnit = TimeUnit.SECONDS;
    private final SignalUnit signalUnit = SignalUnit.CELSIUS;
    private final SensorModel sensor = new SensorModel(sensorId, signalUnit, samplingPeriod, samplingPeriodUnit);
    private AutoCloseable mocks;

    @Mock
    SensorRepository sensorRepository;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        sensorService = new SensorService(sensorRepository);
        when(sensorRepository.findById(sensorId)).thenReturn(Optional.of(sensor));
        when(sensorRepository.save(sensor)).thenReturn(sensor);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void valid_id_should_return_associated_sensor() {
        final SensorModel retrievedSensor = sensorService.getSensorById(sensorId);
        assertThat(retrievedSensor.getId()).isEqualTo(sensorId);
        assertThat(retrievedSensor.getSamplingPeriod()).isEqualTo(samplingPeriod);
        assertThat(retrievedSensor.getSamplingPeriodUnit()).isEqualTo(samplingPeriodUnit);
        assertThat(retrievedSensor.getSignalUnit()).isEqualTo(signalUnit);
    }

    @Test
    void invalid_id_should_throw_exception() {
        final long invalidSensorId = 1L;
        when(sensorRepository.findById(invalidSensorId)).thenReturn(Optional.empty());
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> sensorService.getSensorById(invalidSensorId));
    }

    @Test
    void updating_sampling_period_for_valid_id_should_return_new_sensor() {
        final long newSamplingPeriod = 2*samplingPeriod + 1;
        final SensorModel updatedSensor = sensorService.updateSamplingPeriod(sensorId, newSamplingPeriod);
        assertThat(updatedSensor.getId()).isEqualTo(sensorId);
        assertThat(updatedSensor.getSamplingPeriod()).isEqualTo(newSamplingPeriod);
        assertThat(updatedSensor.getSamplingPeriodUnit()).isEqualTo(samplingPeriodUnit);
        assertThat(updatedSensor.getSignalUnit()).isEqualTo(signalUnit);
    }

    @Test
    void updating_sampling_period_for_invalid_id_should_throw_exception() {
        final long invalidSensorId = sensorId + 1;
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> sensorService.updateSamplingPeriod(invalidSensorId, 100L));
    }


}
