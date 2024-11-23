package com.control_ops.control_system.repositories;

import com.control_ops.control_system.models.SensorModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<SensorModel, Long> {
}
