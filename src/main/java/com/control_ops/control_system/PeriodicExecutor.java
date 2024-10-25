package com.control_ops.control_system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PeriodicExecutor {
    private ScheduledExecutorService scheduler;
    private boolean isExecuting = false;

    private final String name;
    private final long executionPeriod;
    private final TimeUnit executionPeriodUnit;
    private final Runnable runnable;

    private static final Logger logger = LoggerFactory.getLogger(PeriodicExecutor.class);

    public PeriodicExecutor(
            final String name,
            final long executionPeriod,
            final TimeUnit executionPeriodUnit,
            final Runnable runnable) {
        this.name = name;
        this.executionPeriod = executionPeriod;
        this.executionPeriodUnit = executionPeriodUnit;
        this.runnable = runnable;
    }

    public void start() {
        if (!isExecuting) {
            this.scheduler = Executors.newScheduledThreadPool(1);
            this.scheduler.scheduleAtFixedRate(
                    runnable,
                    0L,
                    executionPeriod,
                    executionPeriodUnit);
            isExecuting = true;
            logger.info("{} was started", name);
        } else {
            logger.warn("Cannot start {}; it is already executing", name);
        }
    }

    public void stop() {
        if (isExecuting) {
            this.scheduler.shutdown();
            isExecuting = false;
            logger.info("{} scheduler was stopped", name);
        } else {
            logger.warn("Cannot stop {}; it is already stopped", name);
        }
    }
}
