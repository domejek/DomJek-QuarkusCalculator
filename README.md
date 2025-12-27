# Calculator Project - Quarkus mit InfluxDB

Dieses Projekt ruft alle 10 Sekunden 5 Werte von einer öffentlichen API ab, berechnet den Mittelwert und die Abweichungen und speichert alles in InfluxDB.

## Voraussetzungen

- Java 17 oder höher
- Maven 3.8+
- Docker Desktop

## Projekt-Setup

### 🔧 .env Konfiguration

Bevor Sie starten, erstellen/kopieren Sie die `.env` Datei:

```bash
cp .env.example .env  # Falls .env.example existiert
# Oder direkt bearbeiten:
nano .env
```

**Wichtige Variablen in `.env`:**
- `INFLUXDB_TOKEN` - Ihr persönlicher InfluxDB Token
- `INFLUXDB_ORG` - Organisation Name
- `INFLUXDB_BUCKET` - Bucket Name
- `DATA_SOURCE_URL` - API für Datenquelle

### 🚀 Automatischer Start mit Docker Compose

```bash
docker-compose up --build
```

Dadurch werden beide Services gestartet:
- **InfluxDB**: Port 8086 - Datenbank für Zeitreihendaten
- **Calculator App**: Port 8080 - Quarkus Anwendung mit Scheduler

Die Quarkus Anwendung startet automatisch, sobald die InfluxDB bereit ist.

### Manueller Start (für Entwicklung)

#### 1. InfluxDB starten
```bash
docker-compose up -d influxdb
```

#### 2. Quarkus-Anwendung starten
```bash
./mvnw clean quarkus:dev
```

Oder bei Windows:
```bash
mvnw.cmd clean quarkus:dev
```

## Zugangsdaten

- **InfluxDB**: `http://localhost:8086`
- **Username**: admin
- **Password**: adminpassword
- **Organization**: myorg
- **Bucket**: calculator
- **Token**: my-super-secret-auth-token

## Funktionsweise

1. **DataFetchService**: Ruft alle 10 Sekunden 5 Zufallswerte von der Random.org API ab
2. **CalculationService**: Berechnet Mittelwert und Abweichungen
3. **InfluxDBService**: Speichert alle Daten in InfluxDB:
    - **Measurement "raw"**: 5 einzelne Rohwerte mit Tags (sensor_1 bis sensor_5)
    - **Measurement "average"**: Der berechnete Mittelwert
    - **Measurement "deviation"**: 5 Abweichungen mit Tags (sensor_1 bis sensor_5)

## InfluxDB Web-Interface

Öffne `http://localhost:8086` im Browser und logge dich ein:
- Username: `admin`
- Password: `adminpassword`

### Daten abfragen (Data Explorer)

Alle Measurements:
```flux
from(bucket: "calculator")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "raw" or r._measurement == "average" or r._measurement == "deviation" or r._measurement == "sum_average" or r._measurement == "sum_deviation")
```

Nur kumulative Summen:
```flux
from(bucket: "calculator")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "sum_average" or r._measurement == "sum_deviation")
```

## Projektstruktur

```
calculator-project/
├── src/main/java/com/example/
│   ├── model/
│   │   └── SensorData.java          # Datenmodell
│   ├── service/
│   │   ├── DataFetchService.java    # API-Abfrage
│   │   ├── CalculationService.java  # Berechnungen
│   │   └── InfluxDBService.java     # InfluxDB-Integration
│   └── scheduler/
│       └── DataCollectionScheduler.java  # Scheduler (alle 10s)
├── src/main/resources/
│   └── application.properties       # Konfiguration
├── docker-compose.yml               # InfluxDB Container
└── pom.xml                          # Maven Dependencies
```

## Konfiguration anpassen

In `src/main/resources/application.properties`:

- **Datenquelle ändern**: `data.source.url`
- **Intervall ändern**: `scheduler.cron` (aktuell: `*/10 * * * * ?` = alle 10 Sekunden)
- **InfluxDB-Verbindung**: `influxdb.*` Eigenschaften

## Health Checks

- **InfluxDB**: `http://localhost:8086/health`
- **Calculator App**: `http://localhost:8080/q/health`

## Stoppen

```bash
# Beide Services stoppen:
docker-compose down

# Services mit Daten löschen:
docker-compose down -v

# Logs anzeigen:
docker-compose logs -f calculator-app
docker-compose logs -f influxdb
```

## Verbesserungen für Docker-Umgebung

- **Automatische Abhängigkeitsauflösung**: Calculator App wartet auf InfluxDB
- **Retry-Logik**: Mehrere Versuche bei Verbindungsfehlern
- **Health Checks**: Überwachung der Service-Gesundheit
- **Environment-Variablen**: Flexible Konfiguration für Docker
- **Robuste Fehlerbehandlung**: Bessere Logging und Wiederholungsversuche

## Troubleshooting

### InfluxDB-Verbindungsfehler
- Prüfe ob der Container läuft: `docker ps`
- Prüfe Logs: `docker logs influxdb-calculator`

### Keine Daten in InfluxDB
- Überprüfe Quarkus-Logs auf Fehler
- Stelle sicher, dass die Random.org API erreichbar ist
- Teste den Bucket im InfluxDB Web-Interface

## Beispiel alternative Datenquelle

Wenn du eine andere API verwenden möchtest, ändere in `application.properties`:

```properties
# Beispiel: JSON API
data.source.url=https://api.example.com/data

# Dann passe DataFetchService.java an, um JSON zu parsen
```