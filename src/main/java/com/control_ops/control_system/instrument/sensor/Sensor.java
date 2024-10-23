package com.control_ops.control_system.instrument.sensor;

import com.control_ops.control_system.instrument.InstrumentId;
import com.control_ops.control_system.instrument.Signal;
import com.control_ops.control_system.instrument.SignalUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sensor {
    private Signal currentSignal;
    private boolean isMeasuring = false;

    private final MeasurementBehaviour measurementBehaviour;
    private final InstrumentId instrumentId;
    private final long samplingPeriod;
    private final TimeUnit samplingPeriodUnit;
    private final SignalUnit signalUnit;
    private final ScheduledExecutorService scheduler;
    private final List<SensorListener> sensorListeners = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(Sensor.class);
    /**
     * Initializes a new sensor object.
     * @param instrumentId A unique string identifying the sensor
     * @param samplingPeriod How often the sensor should record a new measurement
     * @param samplingPeriodUnit The time units in which the sampling period is denominated (e.g. milliseconds)
     * @param signalUnit The measurement unit of data gathered by the sensor
     * @param measurementBehaviour Describes how measurements should be taken
     */
    public Sensor(
            final String instrumentId,
            final long samplingPeriod,
            final TimeUnit samplingPeriodUnit,
            final SignalUnit signalUnit,
            final MeasurementBehaviour measurementBehaviour) {
        this.instrumentId = new InstrumentId(instrumentId);
        this.samplingPeriod = samplingPeriod;
        this.samplingPeriodUnit = samplingPeriodUnit;
        this.signalUnit = signalUnit;
        this.measurementBehaviour = measurementBehaviour;
        this.scheduler = Executors.newScheduledThreadPool(1);
        logger.info(
                "A new sensor was created.\tSensor ID: {}\tSampling period: {} {}\tSignal unit: {}",
                instrumentId,
                samplingPeriod,
                samplingPeriodUnit,
                signalUnit);
    }

    public Signal getCurrentSignal() {
        return currentSignal;
    }

    public InstrumentId getInstrumentID() {
        return instrumentId;
    }

    public void startMeasuring() {
        if (isMeasuring) {
            logger.warn("Measurement is already enabled for {}", instrumentId);
            return;
        }
        this.scheduler.scheduleAtFixedRate(this::takeMeasurement, 0L, this.samplingPeriod, this.samplingPeriodUnit);
        this.isMeasuring = true;
        logger.info("Measurement was enabled for {}.\tSampling period: {} {}", instrumentId, samplingPeriod, samplingPeriodUnit);
    }

    public void stopMeasuring() {
        if (!isMeasuring) {
            logger.warn("Measurement is already disabled for {}", instrumentId);
            return;
        }
        this.scheduler.shutdown();
        this.isMeasuring = false;
        logger.info("Measurement was disabled for {}", instrumentId);
    }

    public void addListener(final SensorListener sensorListener) {
        if (this.sensorListeners.contains(sensorListener)) {
            logger.warn("Cannot add the provided SensorListener; it is already subscribed to {}", instrumentId);
            return;
        }
        this.sensorListeners.add(sensorListener);
        logger.info("The provided SensorListener was added to {}", instrumentId);
    }

    public void removeListener(final SensorListener sensorListener) {
        if (!this.sensorListeners.contains(sensorListener)) {
            logger.warn("Cannot remove the provided SensorListener; it is not subscribed to {}", instrumentId);
            return;
        }
        this.sensorListeners.remove(sensorListener);
        logger.info("The provided SensorListener was removed from {}", instrumentId);
    }

    /**
     * Takes a new measurement using the sensor's measurement behaviour.
     */
    private synchronized void takeMeasurement() {
        Signal newSignal = measurementBehaviour.takeMeasurement(
                instrumentId,
                signalUnit,
                ZoneId.of("UTC"));
        currentSignal = newSignal;
        for (final SensorListener listener : this.sensorListeners) {
            listener.onMeasurement(newSignal);
        }
    }
}
