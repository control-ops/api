package com.control_ops.control_system.sensor;

import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sensor {
    private boolean isMeasuring = false;
    private final long samplingPeriod;
    private final TimeUnit samplingPeriodUnit;
    private final MeasurementUnit measurementUnit;
    private final SecureRandom random = new SecureRandom();
    private final ScheduledExecutorService scheduler;
    private final List<SensorListener> sensorListeners = new ArrayList<>();

    /**
     * Initializes a new sensor object.
     * @param samplingPeriod How often the sensor should record a new measurement, in milliseconds
     * @param measurementUnit The measurement unit of data gathered by the sensor
     */
    public Sensor(final long samplingPeriod, final TimeUnit samplingPeriodUnit, final MeasurementUnit measurementUnit) {
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
                random.nextDouble(),
                this.measurementUnit,
                ZonedDateTime.now(ZoneId.of("UTC")));
        for (final SensorListener listener : this.sensorListeners) {
            listener.onMeasurement(newMeasurement);
        }
    }
}
