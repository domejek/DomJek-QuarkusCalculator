package org.example.model;

import java.time.Instant;
import java.util.List;

public class SensorData {
    private Instant timestamp;
    private List<Double> rawValues;
    private Double average;
    private List<Double> deviations;

    public SensorData() {
        this.timestamp = Instant.now();
    }

    public SensorData(Instant timestamp, List<Double> rawValues) {
        this.timestamp = timestamp;
        this.rawValues = rawValues;
    }

    // Getters and Setters
    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<Double> getRawValues() {
        return rawValues;
    }

    public void setRawValues(List<Double> rawValues) {
        this.rawValues = rawValues;
    }

    public Double getAverage() {
        return average;
    }

    public void setAverage(Double average) {
        this.average = average;
    }

    public List<Double> getDeviations() {
        return deviations;
    }

    public void setDeviations(List<Double> deviations) {
        this.deviations = deviations;
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "timestamp=" + timestamp +
                ", rawValues=" + rawValues +
                ", average=" + average +
                ", deviations=" + deviations +
                '}';
    }
}