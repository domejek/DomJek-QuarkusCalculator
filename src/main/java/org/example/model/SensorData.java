package org.example.model;

import java.time.Instant;
import java.util.List;

/**
 * Datenmodell für Sensordaten
 */
public class SensorData {
    private List<Double> rawValues;
    private double average;
    private List<Double> deviations;
    private double sumAverage;
    private double sumDeviation;
    private Instant timestamp;

    public SensorData() {
        this.timestamp = Instant.now();
    }

    public SensorData(List<Double> rawValues, double average, List<Double> deviations) {
        this.rawValues = rawValues;
        this.average = average;
        this.deviations = deviations;
        this.timestamp = Instant.now();
    }

    public SensorData(List<Double> rawValues, double average, List<Double> deviations, double sumAverage, double sumDeviation) {
        this.rawValues = rawValues;
        this.average = average;
        this.deviations = deviations;
        this.sumAverage = sumAverage;
        this.sumDeviation = sumDeviation;
        this.timestamp = Instant.now();
    }

    public List<Double> getRawValues() {
        return rawValues;
    }

    public void setRawValues(List<Double> rawValues) {
        this.rawValues = rawValues;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public List<Double> getDeviations() {
        return deviations;
    }

    public void setDeviations(List<Double> deviations) {
        this.deviations = deviations;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public double getSumAverage() {
        return sumAverage;
    }

    public void setSumAverage(double sumAverage) {
        this.sumAverage = sumAverage;
    }

    public double getSumDeviation() {
        return sumDeviation;
    }

    public void setSumDeviation(double sumDeviation) {
        this.sumDeviation = sumDeviation;
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "rawValues=" + rawValues +
                ", average=" + average +
                ", deviations=" + deviations +
                ", sumAverage=" + sumAverage +
                ", sumDeviation=" + sumDeviation +
                ", timestamp=" + timestamp +
                '}';
    }
}