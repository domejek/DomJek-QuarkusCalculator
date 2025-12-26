package org.example.scheduler;

import com.example.model.SensorData;
import com.example.service.CalculationService;
import com.example.service.DataFetchService;
import com.example.service.InfluxDBService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DataCollectionScheduler {

    private static final Logger LOG = Logger.getLogger(DataCollectionScheduler.class);

    @Inject
    DataFetchService dataFetchService;

    @Inject
    CalculationService calculationService;

    @Inject
    InfluxDBService influxDBService;

    /**
     * Wird alle 10 Sekunden ausgeführt
     */
    @Scheduled(cron = "{scheduler.cron}")
    void collectAndProcessData() {
        LOG.info("===== Starting data collection cycle =====");

        try {
            // Schritt 1: Daten von API abrufen
            SensorData data = dataFetchService.fetchData();

            if (data == null) {
                LOG.error("Failed to fetch data from API");
                return;
            }

            // Schritt 2: Berechnungen durchführen
            calculationService.processData(data);

            // Schritt 3: In InfluxDB speichern
            influxDBService.writeData(data);

            LOG.info("===== Data collection cycle completed successfully =====");

        } catch (Exception e) {
            LOG.error("Error during data collection cycle", e);
        }
    }
}