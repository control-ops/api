package com.control_ops.control_system.services;

import com.control_ops.control_system.models.SensorModel;
import com.control_ops.control_system.repositories.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SensorService {
    final SensorRepository sensorRepository;

    @Autowired
    public SensorService(final SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    public SensorModel getSensorById(final long id) {
        return sensorRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Sensor with ID " + id + " not found"));
    }

    public SensorModel updateSamplingPeriod(final long id, final long newSamplingPeriod) {
        final SensorModel sensorModel = getSensorById(id);
        sensorModel.setSamplingPeriod(newSamplingPeriod);
        return sensorRepository.save(sensorModel);
    }
}
