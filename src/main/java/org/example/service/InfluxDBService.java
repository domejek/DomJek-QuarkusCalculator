package org.example.service;

import com.example.model.SensorData;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Instant;

@ApplicationScoped
public class InfluxDBService {

    private static final Logger LOG = Logger.getLogger(InfluxDBService.class);

    @ConfigProperty(name = "influxdb.url")
    String influxUrl;

    @ConfigProperty(name = "influxdb.token")
    String influxToken;

    @ConfigProperty(name = "influxdb.org")
    String influxOrg;

    @ConfigProperty(name = "influxdb.bucket")
    String influxBucket;

    private InfluxDBClient influxDBClient;
    private WriteApiBlocking writeApi;

    @PostConstruct
    void initialize() {
        try {
            LOG.info("Initializing InfluxDB connection to: " + influxUrl);
            influxDBClient = InfluxDBClientFactory.create(influxUrl, influxToken.toCharArray(), influxOrg, influxBucket);
            writeApi = influxDBClient.getWriteApiBlocking();
            LOG.info("InfluxDB connection established successfully");
        } catch (Exception e) {
            LOG.error("Failed to initialize InfluxDB connection", e);
        }
    }

    @PreDestroy
    void cleanup() {
        if (influxDBClient != null) {
            LOG.info("Closing InfluxDB connection");
            influxDBClient.close();
        }
    }

    /**
     * Schreibt die Sensordaten in InfluxDB
     * @param data SensorData mit allen berechneten Werten
     */
    public void writeData(SensorData data) {
        if (data == null) {
            LOG.warn("Cannot write null data to InfluxDB");
            return;
        }

        try {
            Instant timestamp = data.getTimestamp();

            // Schreibe Rohwerte (measurement: raw)
            if (data.getRawValues() != null) {
                for (int i = 0; i < data.getRawValues().size(); i++) {
                    Point point = Point.measurement("raw")
                            .addTag("sensor", "sensor_" + (i + 1))
                            .addField("value", data.getRawValues().get(i))
                            .time(timestamp, WritePrecision.NS);

                    writeApi.writePoint(point);
                }
                LOG.debug("Wrote " + data.getRawValues().size() + " raw values to InfluxDB");
            }

            // Schreibe Mittelwert (measurement: average)
            if (data.getAverage() != null) {
                Point avgPoint = Point.measurement("average")
                        .addField("value", data.getAverage())
                        .time(timestamp, WritePrecision.NS);

                writeApi.writePoint(avgPoint);
                LOG.debug("Wrote average value to InfluxDB: " + data.getAverage());
            }

            // Schreibe Abweichungen (measurement: deviation)
            if (data.getDeviations() != null) {
                for (int i = 0; i < data.getDeviations().size(); i++) {
                    Point point = Point.measurement("deviation")
                            .addTag("sensor", "sensor_" + (i + 1))
                            .addField("value", data.getDeviations().get(i))
                            .time(timestamp, WritePrecision.NS);

                    writeApi.writePoint(point);
                }
                LOG.debug("Wrote " + data.getDeviations().size() + " deviation values to InfluxDB");
            }

            LOG.info("Successfully wrote all data to InfluxDB at timestamp: " + timestamp);

        } catch (Exception e) {
            LOG.error("Error writing data to InfluxDB", e);
        }
    }
}