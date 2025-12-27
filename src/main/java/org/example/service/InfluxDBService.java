package org.example.service;

import org.example.model.SensorData;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class InfluxDBService {

    private static final Logger LOG = Logger.getLogger(InfluxDBService.class.getName());

    @ConfigProperty(name = "influxdb.url")
    String influxUrl;

    @ConfigProperty(name = "influxdb.token")
    String token;

    @ConfigProperty(name = "influxdb.org")
    String org;

    @ConfigProperty(name = "influxdb.bucket")
    String bucket;

    private InfluxDBClient influxDBClient;
    private WriteApiBlocking writeApi;

    @PostConstruct
    public void init() {
        int maxRetries = 5;
        int retryDelay = 10000; // 10 seconds
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                LOG.info("Initializing InfluxDB connection to: " + influxUrl + " (attempt " + attempt + "/" + maxRetries + ")");
                influxDBClient = InfluxDBClientFactory.create(influxUrl, token.toCharArray(), org, bucket);
                writeApi = influxDBClient.getWriteApiBlocking();
                
                // Test connection with a simple point
                Point testPoint = Point.measurement("test")
                        .addField("value", 1.0)
                        .time(Instant.now(), WritePrecision.NS);
                writeApi.writePoint(testPoint);
                LOG.info("InfluxDB connection established successfully");
                return;
                
            } catch (Exception e) {
                LOG.warning("Failed to initialize InfluxDB (attempt " + attempt + "/" + maxRetries + "): " + e.getMessage());
                
                if (attempt == maxRetries) {
                    LOG.severe("Failed to initialize InfluxDB after " + maxRetries + " attempts");
                    e.printStackTrace();
                    return;
                }
                
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    @PreDestroy
    public void cleanup() {
        if (influxDBClient != null) {
            influxDBClient.close();
            LOG.info("InfluxDB connection closed");
        }
    }

    public void saveSensorData(SensorData data) {
        if (data == null || writeApi == null) {
            LOG.warning("Cannot save null data or writeApi not initialized");
            return;
        }

        int maxRetries = 3;
        int retryDelay = 2000; // 2 seconds
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Instant timestamp = data.getTimestamp();
                List<Double> rawValues = data.getRawValues();
                double average = data.getAverage();
                List<Double> deviations = data.getDeviations();

                // Speichere Rohwerte
                if (rawValues != null && !rawValues.isEmpty()) {
                    LOG.info("Saving " + rawValues.size() + " raw values (attempt " + attempt + "/" + maxRetries + ")");
                    for (int i = 0; i < rawValues.size(); i++) {
                        Point point = Point.measurement("raw")
                                .addTag("sensor", "sensor_" + (i + 1))
                                .addField("value", rawValues.get(i))
                                .time(timestamp, WritePrecision.NS);
                        writeApi.writePoint(point);
                    }
                    LOG.info("Saved " + rawValues.size() + " raw values to InfluxDB");
                }

                // Speichere Durchschnitt
                LOG.info("Saving average value: " + average + " (attempt " + attempt + "/" + maxRetries + ")");
                Point avgPoint = Point.measurement("average")
                        .addField("value", average)
                        .time(timestamp, WritePrecision.NS);
                writeApi.writePoint(avgPoint);
                LOG.info("Saved average value: " + average);

            // Speichere Abweichungen
            if (deviations != null && !deviations.isEmpty()) {
                LOG.info("Saving " + deviations.size() + " deviation values (attempt " + attempt + "/" + maxRetries + ")");
                for (int i = 0; i < deviations.size(); i++) {
                    Point point = Point.measurement("deviation")
                            .addTag("sensor", "sensor_" + (i + 1))
                            .addField("value", deviations.get(i))
                            .time(timestamp, WritePrecision.NS);
                    writeApi.writePoint(point);
                }
                LOG.info("Saved " + deviations.size() + " deviation values to InfluxDB");
            }

            // Speichere kumulative Summe der Durchschnittswerte
            LOG.info("Saving sum_average value: " + data.getSumAverage() + " (attempt " + attempt + "/" + maxRetries + ")");
            Point sumAvgPoint = Point.measurement("sum_average")
                    .addField("value", data.getSumAverage())
                    .time(timestamp, WritePrecision.NS);
            writeApi.writePoint(sumAvgPoint);
            LOG.info("Saved sum_average value: " + data.getSumAverage());

            // Speichere kumulative Summe der Abweichungen
            LOG.info("Saving sum_deviation value: " + data.getSumDeviation() + " (attempt " + attempt + "/" + maxRetries + ")");
            Point sumDevPoint = Point.measurement("sum_deviation")
                    .addField("value", data.getSumDeviation())
                    .time(timestamp, WritePrecision.NS);
            writeApi.writePoint(sumDevPoint);
            LOG.info("Saved sum_deviation value: " + data.getSumDeviation());

            return; // Success, exit retry loop

            } catch (Exception e) {
                LOG.warning("Error saving data to InfluxDB (attempt " + attempt + "/" + maxRetries + "): " + e.getMessage());
                
                if (attempt == maxRetries) {
                    LOG.severe("Failed to save data to InfluxDB after " + maxRetries + " attempts");
                    e.printStackTrace();
                    return;
                }
                
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}