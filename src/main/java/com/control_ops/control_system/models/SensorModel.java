package com.control_ops.control_system.models;

import com.control_ops.control_system.instrument.SignalUnit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sensors")
public class SensorModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private SignalUnit signalUnit;

    @Column(name = "sampling_period", nullable = false)
    private long samplingPeriod;

    @Enumerated(EnumType.STRING)
    private TimeUnit samplingPeriodUnit;
}
