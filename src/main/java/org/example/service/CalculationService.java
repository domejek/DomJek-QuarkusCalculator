package org.example.service;

import com.example.model.SensorData;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CalculationService {

    private static final Logger LOG = Logger.getLogger(CalculationService.class);

    /**
     * Berechnet den Mittelwert aus den Rohwerten
     * @param data SensorData mit Rohwerten
     */
    public void calculateAverage(SensorData data) {
        if (data == null || data.getRawValues() == null || data.getRawValues().isEmpty()) {
            LOG.warn("Cannot calculate average: data is null or empty");
            return;
        }

        List<Double> values = data.getRawValues();
        double sum = 0.0;

        for (Double value : values) {
            sum += value;
        }

        double average = sum / values.size();
        data.setAverage(average);

        LOG.debug("Calculated average: " + average + " from values: " + values);
    }

    /**
     * Berechnet die Abweichungen vom Mittelwert
     * @param data SensorData mit Rohwerten und Mittelwert
     */
    public void calculateDeviations(SensorData data) {
        if (data == null || data.getRawValues() == null || data.getAverage() == null) {
            LOG.warn("Cannot calculate deviations: missing data or average");
            return;
        }

        List<Double> deviations = new ArrayList<>();
        double average = data.getAverage();

        for (Double rawValue : data.getRawValues()) {
            double deviation = rawValue - average;
            deviations.add(deviation);
        }

        data.setDeviations(deviations);

        LOG.debug("Calculated deviations: " + deviations);
    }

    /**
     * Führt alle Berechnungen durch
     * @param data SensorData mit Rohwerten
     */
    public void processData(SensorData data) {
        calculateAverage(data);
        calculateDeviations(data);

        LOG.info("Processed data: " + data);
    }
}