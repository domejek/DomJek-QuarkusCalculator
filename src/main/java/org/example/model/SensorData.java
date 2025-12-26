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

<<<<<<< HEAD
=======
    // Getter und Setter
>>>>>>> 067a69547868743e1eaafb63af5e9a9f23f64dcc
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

    @Override
    public String toString() {
        return "SensorData{" +
                "rawValues=" + rawValues +
                ", average=" + average +
                ", deviations=" + deviations +
                ", timestamp=" + timestamp +
                '}';
    }
}