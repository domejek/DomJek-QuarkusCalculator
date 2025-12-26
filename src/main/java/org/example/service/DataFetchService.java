package org.example.service;

import com.example.model.SensorData;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DataFetchService {

    private static final Logger LOG = Logger.getLogger(DataFetchService.class);

    @ConfigProperty(name = "data.source.url")
    String dataSourceUrl;

    /**
     * Ruft Daten von einer öffentlichen API ab
     * @return SensorData mit 5 Rohwerten
     */
    public SensorData fetchData() {
        try {
            LOG.debug("Fetching data from: " + dataSourceUrl);

            URL url = new URL(dataSourceUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                List<Double> values = new ArrayList<>();
                String line;

                while ((line = in.readLine()) != null && values.size() < 5) {
                    try {
                        double value = Double.parseDouble(line.trim());
                        values.add(value);
                    } catch (NumberFormatException e) {
                        LOG.warn("Could not parse value: " + line);
                    }
                }
                in.close();

                if (values.size() == 5) {
                    SensorData data = new SensorData(Instant.now(), values);
                    LOG.info("Successfully fetched 5 values: " + values);
                    return data;
                } else {
                    LOG.error("Expected 5 values but got " + values.size());
                    return null;
                }
            } else {
                LOG.error("HTTP request failed with response code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            LOG.error("Error fetching data from API", e);
            return null;
        }
    }
}