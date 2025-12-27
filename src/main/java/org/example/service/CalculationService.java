package org.example.service;

import org.example.model.SensorData;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

@ApplicationScoped
public class CalculationService {

    private static final Logger LOG = Logger.getLogger(CalculationService.class.getName());

    // Thread-safe cumulative sums
    private volatile double sumAverage = 0.0;
    private volatile double sumDeviation = 0.0;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public double calculateAverage(SensorData data) {
        if (data == null || data.getRawValues() == null || data.getRawValues().isEmpty()) {
            LOG.warning("No data to calculate average");
            return 0.0;
        }

        List<Double> values = data.getRawValues();
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }

        double average = sum / values.size();
        LOG.info("Calculated average: " + average + " from " + values.size() + " values");
        return average;
    }

    public List<Double> calculateDeviations(SensorData data, double average) {
        if (data == null || data.getRawValues() == null || data.getRawValues().isEmpty()) {
            LOG.warning("No data to calculate deviations");
            return new ArrayList<>();
        }

        List<Double> deviations = new ArrayList<>();
        for (double value : data.getRawValues()) {
            double deviation = value - average;
            deviations.add(deviation);
        }

        LOG.info("Calculated " + deviations.size() + " deviations: " + deviations);
        return deviations;
    }

    public void performCalculations(SensorData data) {
        if (data == null) {
            LOG.warning("Cannot perform calculations on null data");
            return;
        }

        double average = calculateAverage(data);
        data.setAverage(average);

        List<Double> deviations = calculateDeviations(data, average);
        data.setDeviations(deviations);

        // Update cumulative sums
        updateCumulativeSums(average, deviations);

        // Set cumulative sums in data object for InfluxDB
        data.setSumAverage(getSumAverage());
        data.setSumDeviation(getSumDeviation());

        LOG.info("All calculations completed for data: " + data);
    }

    private void updateCumulativeSums(double average, List<Double> deviations) {
        lock.writeLock().lock();
        try {
            sumAverage += average;
            
            double deviationSum = 0.0;
            for (double deviation : deviations) {
                deviationSum += Math.abs(deviation);
            }
            sumDeviation += deviationSum;
            
            LOG.info("Updated cumulative sums - sumAverage: " + sumAverage + ", sumDeviation: " + sumDeviation);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public double getSumAverage() {
        lock.readLock().lock();
        try {
            return sumAverage;
        } finally {
            lock.readLock().unlock();
        }
    }

    public double getSumDeviation() {
        lock.readLock().lock();
        try {
            return sumDeviation;
        } finally {
            lock.readLock().unlock();
        }
    }
}