package org.example.service;

import org.example.model.SensorData;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class DataFetchService {

    private static final Logger LOG = Logger.getLogger(DataFetchService.class.getName());

    @ConfigProperty(name = "data.source.url", defaultValue = "https://www.random.org/integers/?num=5&min=1&max=100&col=1&base=10&format=plain&rnd=new")
    String dataSourceUrl;

    public SensorData fetchData() {
        try {
            LOG.info("Fetching data from: " + dataSourceUrl);

            URL url = new URL(dataSourceUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                LOG.warning("HTTP error code: " + responseCode);
                return null;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );

            List<Double> rawValues = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null && rawValues.size() < 5) {
                try {
                    double value = Double.parseDouble(line.trim());
                    rawValues.add(value);
                } catch (NumberFormatException e) {
                    LOG.warning("Could not parse value: " + line);
                }
            }
            reader.close();

            if (rawValues.size() == 5) {
                SensorData data = new SensorData();
                data.setRawValues(rawValues);
                LOG.info("Successfully fetched " + rawValues.size() + " values: " + rawValues);
                return data;
            } else {
                LOG.warning("Expected 5 values but got " + rawValues.size());
                return null;
            }

        } catch (Exception e) {
            LOG.severe("Error fetching data: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}