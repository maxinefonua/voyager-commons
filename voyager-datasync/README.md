# Datasync Jobs
Updated Oct 19, 2025

This module packages runnable sync classes as separate JAR files.
- used in GitHub Actions to deploy onto EC2 instance
- `AirportSync`and`FlightSync` scheduled to run every Monday for weekly updates
- `CountrySync`manually run due to rarity of country updates
- implemented with multithreaded requests to external service calls for increased processing rates
- each JAR accepts option flags for reusability

### JAR Option Flags
All sync JARs share the following flags. Exceptions thrown when required flags are missing.

| Option Flags | Description                      |  Required  | Default | Objective                                       |
|:-----:|:---------------------------------|:----------:|:-------:|:------------------------------------------------|
|     `-h`     | Voyager API host                 |  **Yes**   |    -    | reusability across environments                 |
|     `-p`     | port                             |     No     |  3000   | tailor environment endpoint                     |
|    `-at`     | Voyager API authentication token |  **Yes**   |    -    | authorizes calls to admin endpoints             |
|    `-gn`     | registered Geonames username     |  **Yes**   |    -    | enables geocoded enrichment                     |

### Build Runnable JARs
```bash
  mvn clean package -P multiple-jar-profile
```
The above command packages each sync JAR separately. See module [pom.xml](pom.xml) for packaging details. See below for JAR specific info.
- [AirportsSync](#airportsync)
- [FlightSync](#flightsync)
- [AirportsSync](#airportsync)
<hr>

## AirportSync
Runnable Class: [`AirportSync.java`](src/main/java/org/voyager/sync/AirportSync.java)
- Syncs airport type data sourced from [ch-aviation.com](https://www.ch-aviation.com/airports)
- Enriches [Voyager API](https://github.com/maxinefonua/voyager-api) airports with geocoded data from [GeoNames.com](https://www.geonames.org/export/web-services.html#findNearbyPlaceName)
- Any failures are patched with type`UNVERIFIED`(see[`AirportType.java`](../voyager-models/src/main/java/org/voyager/commons/model/airport/AirportType.java))
#### bash Run Command*
*If running copied + pasted bash commands from this README, set the required environment variables first with actual values.
```bash
  java -jar target/airports-sync-$JAR_VERSION.jar -h=$VOYAGER_API_HOST -at=$VOYAGER_API_KEY -gn=$GEONAMES_API_USERNAME -tp=unverified
```
### Additional Flags:
The following flags are specific to AirportSync. See linked files for implementation details.

| Option Flags | Description                                |                    Required                     |      Default       | Objective                                                                                                                                           |
|:------------:|:-------------------------------------------|:-----------------------------------------------:|:------------------:|:----------------------------------------------------------------------------------------------------------------------------------------------------|
|    `-sy`     | `SyncMode`value                            |                       No                        |    `FULL_SYNC`     | sets processing mode to given value<br>see`SyncMode`enum in[`AirportSyncConfig.java`](src/main/java/org/voyager/sync/config/AirportSyncConfig.java) |
|    `-tc`     | thread count for multithreading            |                       No                        |        100         | increases processing throughput                                                                                                                     |
|    `-tl`     | comma-separated list of`AiportType`values  |                       No                        | `UNVERIFIED,OTHER` | process airports of given types<br>see[`AirportType.java`](../voyager-models/src/main/java/org/voyager/commons/model/airport/AirportType.java)      |
|    `-il`     | comma-separated list of IATA airport codes |     **Yes** when in sync mode`ADD_MISSING`      |         -          | builds and inserts airport records to Voyager                                                                                                       | 

<hr>

## FlightSync
Runnable Class: [`FlightSync.java`](src/main/java/org/voyager/sync/FlightSync.java)
- Syncs flight data of civil airports sourced from [Flightradar24.com](https://www.flightradar24.com/data/airlines)
- Uses an executor service for limited multithreaded processing
- Enriches [Voyager API](https://github.com/maxinefonua/voyager-api) airports and routes using [ch-aviation.com](https://www.ch-aviation.com/airports) and [GeoNames.com](https://www.geonames.org/export/web-services.html#findNearbyPlaceName)
- Upserts airline airport records for successfully processed active flights
- Writes failures to target directory for further analysis or retries
#### bash Run Command*
*If running copied + pasted bash commands from this README, set the required environment variables first with actual values.

FlightSync uses a graavl Script processing engine that logs warnings each time it is used. To siphon those logs separately and have only programmed logs printing to console, the VM option`-Dpolyglot.log.file=./truffle.log`is added to the bash command below.
```bash
    java -Dpolyglot.log.file=./truffle.log -jar target/flights-sync-$JAR_VERSION.jar -h=$VOYAGER_API_HOST -at=$VOYAGER_API_KEY -gn=$GEONAMES_API_USERNAME
```
### Additional Flags:
The following flags are specific to FlightSync. See linked files for implementation details.

| Option Flags | Description                              |             Required              |   Default   | Objective                                                                                                                                                                                  |
|:------------:|:-----------------------------------------|:---------------------------------:|:-----------:|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|    `-sy`     | `SyncMode` value                         |                No                 | `FULL_SYNC` | sets processing mode to given value<br>see SyncMode enum in[`FlightSyncConfig.java`](src/main/java/org/voyager/sync/config/FlightSyncConfig.java)                                          |
|    `-tc`     | thread count for multithreading          |                No                 |      3      | limits request rates to external enrichment service<br>capped at **5**                                                                                                                     |
|    `-al`     | comma-separated list of `Airline` values | **Yes** when in`AIRLINE_SYNC`mode |      -      | fetches and inserts flight data for given airline values<br>see[`Airline.java`](../voyager-models/src/main/java/org/voyager/commons/model/airline/Airline.java)                            |
|    `-rf`     | full path and name of retry file         |  **Yes** when in`RETRY_SYNC`mode  |      -      | fetches and inserts flight data for failed routes in given retry file |

<hr>

## CountrySync
Runnable Class: [`CountrySync.java`](src/main/java/org/voyager/sync/CountrySync.java)
- Syncs country data sourced from [GeoNames.com](https://www.geonames.org/export/web-services.html#findNearbyPlaceName)
- Upserts missing country records
- Does not use multithreaded processing, due to few number of countries
#### bash Run Command*
*If running copied + pasted bash commands from this README, set the required environment variables first with actual values.
```bash
    java -jar target/country-sync-$JAR_VERSION.jar -h=$VOYAGER_API_HOST -at=$VOYAGER_API_KEY -gn=$GEONAMES_API_USERNAME
```
### Overridden and Additional Flags:
The following flags are specific to FlightSync. See linked files for implementation details.