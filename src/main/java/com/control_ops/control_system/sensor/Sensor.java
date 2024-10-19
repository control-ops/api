package com.control_ops.control_system.sensor;

import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sensor {
    private boolean isMeasuring = false;
    private final String sensorId;
    private final long samplingPeriod;
    private final TimeUnit samplingPeriodUnit;
    private final MeasurementUnit measurementUnit;
    private final SecureRandom random = new SecureRandom();
    private final ScheduledExecutorService scheduler;
    private final List<SensorListener> sensorListeners = new ArrayList<>();
    private static final Set<String> sensorIds = new HashSet<>();

    /**
     * Initializes a new sensor object.
     * @param sensorId A unique string identifying the sensor
     * @param samplingPeriod How often the sensor should record a new measurement
     * @param samplingPeriodUnit The time units in which the sampling period is denominated (e.g. milliseconds)
     * @param measurementUnit The measurement unit of data gathered by the sensor
     */
    public Sensor(
            final String sensorId,
            final long samplingPeriod,
            final TimeUnit samplingPeriodUnit,
            final MeasurementUnit measurementUnit) {
        if (!sensorIds.add(sensorId)) {
            throw new IllegalArgumentException("A sensor with ID " + sensorId + " already exists.");
        }
        this.sensorId = sensorId;
        this.samplingPeriod = samplingPeriod;
        this.samplingPeriodUnit = samplingPeriodUnit;
        this.measurementUnit = measurementUnit;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void startMeasuring() {
        if (!isMeasuring) {
            this.scheduler.scheduleAtFixedRate(this::takeMeasurement, 0L, this.samplingPeriod, this.samplingPeriodUnit);
            this.isMeasuring = true;
        }
    }

    public void stopMeasuring() {
        if (isMeasuring) {
            this.scheduler.shutdown();
            this.isMeasuring = false;
        }
    }

    public void addListener(final SensorListener sensorListener) {
        this.sensorListeners.add(sensorListener);
    }

    public void removeListener(final SensorListener sensorListener) {
        this.sensorListeners.remove(sensorListener);
    }

    private synchronized void takeMeasurement() {
        final Measurement newMeasurement = new Measurement(
                this.sensorId,
                random.nextDouble(),
                this.measurementUnit,
                ZonedDateTime.now(ZoneId.of("UTC")));
        for (final SensorListener listener : this.sensorListeners) {
            listener.onMeasurement(newMeasurement);
        }
    }
}
