package com.control_ops.control_system.sensor;

import com.control_ops.control_system.instrument.Signal;
import com.control_ops.control_system.instrument.SignalUnit;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the Sensor class and SensorListener class that implements the Observer pattern.
 */
class SensorTest {

    private final MeasurementList measurementList = new MeasurementList();
    private final List<Signal> signals = measurementList.getMeasurements();

    private long samplingPeriod = 30L;
    private final TimeUnit samplingTimeUnit = TimeUnit.MILLISECONDS;
    private static int numSensorsInstantiated = 0;

    private Sensor makeDefaultSensor() {
        return new Sensor(generateSensorId(), samplingPeriod, samplingTimeUnit, SignalUnit.CELSIUS, new SampledMeasurement());
    }

    private String generateSensorId() {
        numSensorsInstantiated++;
        return "thermocouple" + numSensorsInstantiated;
    }

    /**
     * Causes the calling thread to wait until at least one measurement is received.
     */
    private void waitForMeasurements() {
        await().atMost(10*samplingPeriod, samplingTimeUnit).until(() -> !signals.isEmpty());
    }

    /**
     * Tests that multiple sensors with the same sensor ID cannot be instantiated.
     */
    @Test
    void testSensorInstantiation() {
        new Sensor("fakeId", samplingPeriod, samplingTimeUnit, SignalUnit.CELSIUS, new SampledMeasurement());
        final MeasurementBehaviour measurementBehaviour = new SampledMeasurement();
        assertThatExceptionOfType(Sensor.SensorAlreadyExistsException.class).isThrownBy(
                () -> new Sensor(
                        "fakeId",
                        samplingPeriod,
                        samplingTimeUnit,
                        SignalUnit.CELSIUS,
                        measurementBehaviour)
        );
    }

    /**
     * Tests that the sensor's startMeasuring() method commences the generation and transmission of measurements.
     */
    @Test
    void testStartMeasuring() {
        final Sensor sensor = makeDefaultSensor();
        sensor.addListener(measurementList);
        assertThrows(
                ConditionTimeoutException.class,
                this::waitForMeasurements);
        assertThat(signals).isEmpty();

        sensor.startMeasuring();
        this.waitForMeasurements();
        assertThat(signals).isNotEmpty();
    }

    /**
     * Tests that the sensor's stopMeasuring() method ceases the generation and transmission of measurements.
     */
    @Test
    void testStopMeasuring() {
        final Sensor sensor = makeDefaultSensor();
        sensor.addListener(measurementList);
        sensor.startMeasuring();
        this.waitForMeasurements();
        sensor.stopMeasuring();
        signals.clear();
        assertThrows(ConditionTimeoutException.class, this::waitForMeasurements);
    }

    /**
     * Tests that the sensor's addListener() method causes the observer to start receiving measurements.
     */
    @Test
    void testAddListener() {
        final Sensor sensor = makeDefaultSensor();
        sensor.addListener(measurementList);
        sensor.startMeasuring();
        await().atMost(100*samplingPeriod, samplingTimeUnit).until(() -> !signals.isEmpty());
        assertThat(signals).hasSizeGreaterThan(1);

        // The measurement list has already been added as a listener, so adding it again should do nothing
        sensor.addListener(measurementList);
    }

    /**
     * Tests that the sensor's removeObserver method causes the observer to stop receiving measurements.
     */
    @Test
    void testRemoveListener() {
        final Sensor sensor = makeDefaultSensor();
        sensor.addListener(measurementList);
        sensor.removeListener(measurementList);
        sensor.startMeasuring();
        await().during(10*samplingPeriod, samplingTimeUnit);
        assertThat(signals).isEmpty();

        // The measurement list has already been removed as a listener, so removing it again should do nothing
        sensor.removeListener(measurementList);
    }

    /**
     * Tests that the sensor actually takes unique measurements.
     */
    @Test
    void testTakeMeasurement() {
        final long minimumMeasurements = 100L;
        final Sensor sensor = makeDefaultSensor();
        sensor.addListener(measurementList);

        sensor.startMeasuring();
        await().atMost(10, TimeUnit.SECONDS).until(() -> signals.size() >= minimumMeasurements);
        sensor.stopMeasuring();
        for (int i = 1; i < signals.size(); i++) {
            assertThat(signals.get(i - 1)).isNotEqualTo(signals.get(i));
            assertThat(signals.get(i).unit()).isEqualTo(SignalUnit.CELSIUS);
        }
    }

    /**
     * Tests that the sensor takes measurements in chronological order.
     */
    @Test
    void testMeasurementSequence() {
        final long minimumMeasurements = 100L;
        final Sensor sensor = makeDefaultSensor();
        sensor.addListener(measurementList);

        sensor.startMeasuring();
        await().atMost(10, TimeUnit.SECONDS).until(() -> signals.size() >= minimumMeasurements);
        sensor.stopMeasuring();
        for (int i = 1; i < signals.size(); i++) {
            final long elapsedTime = Duration.between(
                    signals.get(i - 1).dateTime(),
                    signals.get(i).dateTime()).toMillis();
            assertThat(elapsedTime).isNotNegative();
        }



    }


    /**
     * Calculates the fractional error between an expected sampling period and an actual sampling period. The actual
     * sampling period is calculating by comparing the total time measurements were being taken to the number of
     * measurements that were taken.
     * @param expectedSamplingPeriod The sampling period that was set on the sensor
     * @param numMeasurements The total number of measurements that were exported by the sensor
     * @param firstMeasurementTime The time at which the sensor took the first measurement
     * @param lastMeasurementTime The time at which the sensor took the last measurement
     * @return The fractional error between the expected and actual sampling periods
     */
    double calculateSamplingPeriodError(
            long expectedSamplingPeriod,
            long numMeasurements,
            final ZonedDateTime firstMeasurementTime,
            final ZonedDateTime lastMeasurementTime) {
        final long totalDuration = Duration.between(firstMeasurementTime, lastMeasurementTime).toMillis();
        final double actualSamplingPeriod = (double)totalDuration / (double)(numMeasurements - 1);
        return Math.abs((actualSamplingPeriod - (double)expectedSamplingPeriod) / (double)expectedSamplingPeriod);
    }


    /**
     * Tests that the actual time interval between measurements matches the one set using the sensor's samplingPeriod
     * field; the fractional error between the two is used to determine whether the test passes.
     * <br><br>
     * The error depends on non-deterministic threading behaviour; the error is therefore calculated over a large number
     * of measurements and compared to a threshold to smooth out the results.
     * @param expectedSamplingPeriod The sampling period to be set on the sensor
     * @param minimumMeasurements The minimum number of measurements required to calculate the fractional error
     * @param maxFractionalError The maximum tolerable fractional error between the expected and actual sampling periods
     */
    @ParameterizedTest
    @CsvSource({
            "50, 150, 0.01",
            "100, 75, 0.01",
            "200, 40, 0.01",
            "500, 15, 0.01"
    })
    void testSamplingPeriod(
            final long expectedSamplingPeriod,
            final long minimumMeasurements,
            final double maxFractionalError) {
        this.samplingPeriod = expectedSamplingPeriod;
        final Sensor sensor = new Sensor(
                generateSensorId(),
                samplingPeriod,
                samplingTimeUnit,
                SignalUnit.CELSIUS,
                new SampledMeasurement());
        sensor.addListener(measurementList);

        sensor.startMeasuring();
        await().atMost(60, TimeUnit.SECONDS).until(() -> signals.size() >= minimumMeasurements);
        sensor.stopMeasuring();

        final Signal firstSignal = signals.getFirst();
        final Signal lastSignal = signals.getLast();

        final double averageSamplingPeriodError = this.calculateSamplingPeriodError(
                samplingPeriod,
                signals.size(),
                firstSignal.dateTime(),
                lastSignal.dateTime());
        assertThat(averageSamplingPeriodError).isLessThan(maxFractionalError);
    }
}
