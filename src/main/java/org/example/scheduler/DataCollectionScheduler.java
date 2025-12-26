package org.example.scheduler;

import org.example.model.SensorData;
import org.example.service.DataFetchService;
import org.example.service.CalculationService;
import org.example.service.InfluxDBService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.logging.Logger;

@ApplicationScoped
public class DataCollectionScheduler {

    private static final Logger LOG = Logger.getLogger(DataCollectionScheduler.class.getName());

    @Inject
    DataFetchService dataFetchService;

    @Inject
    CalculationService calculationService;

    @Inject
    InfluxDBService influxDBService;

    @Scheduled(every = "10s")
    public void collectData() {
        LOG.info("=== Starting data collection cycle ===");

        try {
            SensorData data = dataFetchService.fetchData();

            if (data == null) {
                LOG.warning("Failed to fetch data, skipping this cycle");
                return;
            }

            calculationService.performCalculations(data);

            influxDBService.saveSensorData(data);

            LOG.info("=== Data collection cycle completed successfully ===");

        } catch (Exception e) {
            LOG.severe("Error during data collection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}