package com.control_ops.control_system;

import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.awaitility.Awaitility.await;

public class PeriodicExecutorTest {

    private final List<ZonedDateTime> executionTimes = new ArrayList<>();

    @Test
    void testStart() {
        final PeriodicExecutor periodicExecutor = new PeriodicExecutor(
                "scheduler",
                50,
                TimeUnit.MILLISECONDS,
                this::addExecutionTime);
        periodicExecutor.start();
        final int numExecutions = 5;
        waitForExecutions(numExecutions, 2000);
        assertThat(executionTimes).hasSizeGreaterThanOrEqualTo(numExecutions);
    }

    @Test
    void testStop() {
        final PeriodicExecutor periodicExecutor = new PeriodicExecutor(
                "scheduler",
                50,
                TimeUnit.MILLISECONDS,
                this::addExecutionTime);
        periodicExecutor.start();
        waitForExecutions(1, 2000);
        periodicExecutor.stop();
        assertThatExceptionOfType(ConditionTimeoutException.class).isThrownBy(() -> waitForExecutions(1, 300));
    }

    @Test
    void testMultipleStartsAndStops() {
        final PeriodicExecutor periodicExecutor = new PeriodicExecutor(
                "scheduler",
                50,
                TimeUnit.MILLISECONDS,
                this::addExecutionTime);

        final int waitDurationMs = 200;
        int previousSize = 0;

        periodicExecutor.start();
        waitForExecutions(1, waitDurationMs);
        assertThat(executionTimes).hasSizeGreaterThan(previousSize);
        periodicExecutor.stop();
        previousSize = executionTimes.size();

        periodicExecutor.start();
        waitForExecutions(1, waitDurationMs);
        assertThat(executionTimes).hasSizeGreaterThan(previousSize);
        periodicExecutor.stop();
        previousSize = executionTimes.size();

        periodicExecutor.start();
        waitForExecutions(1, waitDurationMs);
        assertThat(executionTimes).hasSizeGreaterThan(previousSize);
        periodicExecutor.stop();
    }

    @ParameterizedTest
    @CsvSource({
            "100, 50",
            "500, 10",
            "1000, 5"
    })
    void testExecutionSequence(final long executionPeriodMs, final int numExecutions) {
        final PeriodicExecutor periodicExecutor = new PeriodicExecutor(
                "test periodic executor",
                executionPeriodMs,
                TimeUnit.MILLISECONDS,
                this::addExecutionTime);
        periodicExecutor.start();
        waitForExecutions(numExecutions, (numExecutions + 2)*executionPeriodMs);
        periodicExecutor.stop();
        assertExecutionSequence(executionTimes);
    }

    /**
     * Tests that the actual time interval between executions matches the Scheduler's execution period.
     * <br><br>
     * The actual execution period depends on non-deterministic threading behaviour; the error is therefore calculated
     * over a number of executions and compared to a threshold to smooth out the results.
     */
    @ParameterizedTest
    @CsvSource({
            "100, 25",
            "500, 15",
            "1000, 10"
    })
    void testExecutionPeriod(final long executionPeriodMs, final long numExecutions) {
        final PeriodicExecutor periodicExecutor = new PeriodicExecutor(
                "scheduler",
                executionPeriodMs,
                TimeUnit.MILLISECONDS,
                this::addExecutionTime);
        periodicExecutor.start();
        waitForExecutions(numExecutions, (numExecutions + 2)*executionPeriodMs);
        periodicExecutor.stop();

        assertExecutionPeriod(executionTimes, executionPeriodMs, TimeUnit.MILLISECONDS, 0.01);
    }

    private void waitForExecutions(final long numExecutions, final long maxWaitDurationMs) {
        final long initialExecutions = executionTimes.size();
        await().atMost(maxWaitDurationMs, TimeUnit.MILLISECONDS).until(() -> executionTimes.size() - initialExecutions >= numExecutions);
    }

    private void addExecutionTime() {
        executionTimes.add(ZonedDateTime.now(ZoneId.of("UTC")));
    }

    /**
     * Asserts that an actual execution period is equal to an expected execution period, within a certain error
     * tolerance
     */
    public static void assertExecutionPeriod(
            final List<ZonedDateTime> executionTimes,
            final long expectedExecutionPeriod,
            final TimeUnit executionPeriodTimeUnit,
            final double maxErrorFraction) {

        final double executionDuration = Duration.between(executionTimes.getFirst(), executionTimes.getLast()).toMillis();
        final double actualExecutionPeriodMs = executionDuration / (double)(executionTimes.size() - 1);

        final double expectedExecutionPeriodMs = TimeUnit.MILLISECONDS.convert(expectedExecutionPeriod, executionPeriodTimeUnit);

        final double errorFraction =
                Math.abs(actualExecutionPeriodMs - expectedExecutionPeriodMs) / expectedExecutionPeriodMs;
        assertThat(errorFraction).isLessThanOrEqualTo(maxErrorFraction);
    }

    public static void assertExecutionSequence(final List<ZonedDateTime> executionTimes) {
        for (int i = 1; i < executionTimes.size(); i++) {
            final long elapsedTime = Duration.between(
                    executionTimes.get(i - 1),
                    executionTimes.get(i)).toMillis();
            assertThat(elapsedTime).isNotNegative();
        }
    }
}
