package com.control_ops.control_system.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sensor {
    private static final Logger logger = LoggerFactory.getLogger(Sensor.class);
    private boolean isMeasuring = false;
    private final MeasurementBehaviour measurementBehaviour;

    private final String sensorId;
    private final long samplingPeriod;
    private final TimeUnit samplingPeriodUnit;
    private final SignalUnit signalUnit;
    private final ScheduledExecutorService scheduler;
    private final List<SensorListener> sensorListeners = new ArrayList<>();
    private static final Set<String> sensorIds = new HashSet<>();

    static class SensorAlreadyExistsException extends RuntimeException {
        public SensorAlreadyExistsException(String sensorId) {
            super("Sensor IDs must be unique; a sensor with ID " + sensorId + " already exists");
        }
    }

    /**
     * Initializes a new sensor object.
     * @param sensorId A unique string identifying the sensor
     * @param samplingPeriod How often the sensor should record a new measurement
     * @param samplingPeriodUnit The time units in which the sampling period is denominated (e.g. milliseconds)
     * @param signalUnit The measurement unit of data gathered by the sensor
     * @param measurementBehaviour Describes how measurements should be taken
     */
    public Sensor(
            final String sensorId,
            final long samplingPeriod,
            final TimeUnit samplingPeriodUnit,
            final SignalUnit signalUnit,
            final MeasurementBehaviour measurementBehaviour) {
        if (sensorIds.contains(sensorId)) {
            throw new SensorAlreadyExistsException(sensorId);
        }
        sensorIds.add(sensorId);
        this.sensorId = sensorId;
        this.samplingPeriod = samplingPeriod;
        this.samplingPeriodUnit = samplingPeriodUnit;
        this.signalUnit = signalUnit;
        this.measurementBehaviour = measurementBehaviour;
        this.scheduler = Executors.newScheduledThreadPool(1);
        logger.info("A new sensor was created: Sensor ID: {}\tTotal sensors: {}", sensorId, sensorIds.size());
    }

    public void startMeasuring() {
        if (isMeasuring) {
            logger.warn("Measurement is already enabled for {}", sensorId);
            return;
        }
        this.scheduler.scheduleAtFixedRate(this::takeMeasurement, 0L, this.samplingPeriod, this.samplingPeriodUnit);
        this.isMeasuring = true;
        logger.info("Measurement was enabled for {}.\tSampling period: {}\tSampling unit: {}", sensorId, samplingPeriod, samplingPeriodUnit);
    }

    public void stopMeasuring() {
        if (!isMeasuring) {
            logger.warn("Measurement is already disabled for {}", sensorId);
            return;
        }
        this.scheduler.shutdown();
        this.isMeasuring = false;
        logger.info("Measurement was disabled for {}", sensorId);
    }

    public void addListener(final SensorListener sensorListener) {
        if (this.sensorListeners.contains(sensorListener)) {
            logger.warn("Cannot add the provided SensorListener; it is already subscribed to {}", sensorId);
            return;
        }
        this.sensorListeners.add(sensorListener);
        logger.info("The provided SensorListener was added to {}", sensorId);
    }

    public void removeListener(final SensorListener sensorListener) {
        if (!this.sensorListeners.contains(sensorListener)) {
            logger.warn("Cannot remove the provided SensorListener; it is not subscribed to {}", sensorId);
            return;
        }
        this.sensorListeners.remove(sensorListener);
        logger.info("The provided SensorListener was removed from {}", sensorId);
    }

    /**
     * Takes a new measurement using the sensor's measurement behaviour.
     */
    private synchronized void takeMeasurement() {
        Signal newSignal = measurementBehaviour.takeMeasurement(
                sensorId,
                signalUnit,
                ZoneId.of("UTC"));
        for (final SensorListener listener : this.sensorListeners) {
            listener.onMeasurement(newSignal);
        }
    }
}
