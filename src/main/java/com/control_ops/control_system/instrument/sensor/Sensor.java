package com.control_ops.control_system.instrument.sensor;

import com.control_ops.control_system.PeriodicExecutor;
import com.control_ops.control_system.instrument.Signal;
import com.control_ops.control_system.instrument.SignalUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Sensor {
    private Signal currentSignal;
    private final MeasurementBehaviour measurementBehaviour;
    private final int id;
    private final SignalUnit signalUnit;
    private final List<SensorListener> sensorListeners = new ArrayList<>();
    private final PeriodicExecutor periodicExecutor;
    private static final Logger logger = LoggerFactory.getLogger(Sensor.class);

    /**
     * Initializes a new sensor object.
     * @param id A unique value identifying the sensor
     * @param samplingPeriod How often the sensor should record a new measurement
     * @param samplingPeriodUnit The time units in which the sampling period is denominated (e.g. milliseconds)
     * @param signalUnit The measurement unit of data gathered by the sensor
     * @param measurementBehaviour Describes how measurements should be taken
     */
    public Sensor(
            final int id,
            final long samplingPeriod,
            final TimeUnit samplingPeriodUnit,
            final SignalUnit signalUnit,
            final MeasurementBehaviour measurementBehaviour) {
        this.id = id;
        this.signalUnit = signalUnit;
        this.measurementBehaviour = measurementBehaviour;
        this.periodicExecutor = new PeriodicExecutor(this.toString(), samplingPeriod, samplingPeriodUnit, this::takeMeasurement);

        logger.info(
                "A new sensor was created.\tSensor ID: {}\tSampling period: {} {}\tSignal unit: {}",
                id,
                samplingPeriod,
                samplingPeriodUnit,
                signalUnit);
    }

    public Signal getCurrentSignal() {
        return currentSignal;
    }

    public int getId() {
        return id;
    }

    public void startMeasuring() {
        periodicExecutor.start();
    }

    public void stopMeasuring() {
        periodicExecutor.stop();
    }

    public void addListener(final SensorListener sensorListener) {
        if (this.sensorListeners.contains(sensorListener)) {
            logger.warn("Cannot add the provided SensorListener; it is already subscribed to {}", id);
            return;
        }
        this.sensorListeners.add(sensorListener);
        logger.info("The provided SensorListener was added to {}", id);
    }

    public void removeListener(final SensorListener sensorListener) {
        if (!this.sensorListeners.contains(sensorListener)) {
            logger.warn("Cannot remove the provided SensorListener; it is not subscribed to {}", id);
            return;
        }
        this.sensorListeners.remove(sensorListener);
        logger.info("The provided SensorListener was removed from {}", id);
    }

    /**
     * Takes a new measurement using the sensor's measurement behaviour.
     */
    private synchronized void takeMeasurement() {
        Signal newSignal = measurementBehaviour.takeMeasurement(
                signalUnit,
                ZoneId.of("UTC"));
        currentSignal = newSignal;
        for (final SensorListener listener : this.sensorListeners) {
            listener.onMeasurement(newSignal);
        }
    }

    @Override
    public String toString() {
        return "Sensor" + id;
    }
}
