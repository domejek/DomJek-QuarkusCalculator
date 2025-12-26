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
        try {
            LOG.info("Initializing InfluxDB connection to: " + influxUrl);
            influxDBClient = InfluxDBClientFactory.create(influxUrl, token.toCharArray(), org, bucket);
            writeApi = influxDBClient.getWriteApiBlocking();
            LOG.info("InfluxDB connection established successfully");
        } catch (Exception e) {
            LOG.severe("Failed to initialize InfluxDB: " + e.getMessage());
            e.printStackTrace();
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

        try {
            Instant timestamp = data.getTimestamp();
            List<Double> rawValues = data.getRawValues();
            double average = data.getAverage();
            List<Double> deviations = data.getDeviations();

            // Speichere Rohwerte
            if (rawValues != null && !rawValues.isEmpty()) {
                LOG.fine("Saving " + rawValues.size() + " raw values");
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
            LOG.fine("Saving average value: " + average);
            Point avgPoint = Point.measurement("average")
                    .addField("value", average)
                    .time(timestamp, WritePrecision.NS);
            writeApi.writePoint(avgPoint);
            LOG.info("Saved average value: " + average);

            // Speichere Abweichungen
            if (deviations != null && !deviations.isEmpty()) {
                LOG.fine("Saving " + deviations.size() + " deviation values");
                for (int i = 0; i < deviations.size(); i++) {
                    Point point = Point.measurement("deviation")
                            .addTag("sensor", "sensor_" + (i + 1))
                            .addField("value", deviations.get(i))
                            .time(timestamp, WritePrecision.NS);
                    writeApi.writePoint(point);
                }
                LOG.info("Saved " + deviations.size() + " deviation values to InfluxDB");
            }

        } catch (Exception e) {
            LOG.severe("Error saving data to InfluxDB: " + e.getMessage());
            e.printStackTrace();
        }
    }
}