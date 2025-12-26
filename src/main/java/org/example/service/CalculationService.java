package org.example.service;

import org.example.model.SensorData;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class CalculationService {

    private static final Logger LOG = Logger.getLogger(CalculationService.class.getName());

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

        LOG.info("All calculations completed for data: " + data);
    }
}