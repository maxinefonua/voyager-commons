# Datasync Jobs
Updated Oct 19, 2025

This module packages runnable sync classes as separate JAR files.
- used in GitHub Actions to deploy onto EC2 instance
- scheduled to run every Monday for weekly updates to flight data
- implemented with multithreaded requests to external service calls for increased processing rates
- each JAR accepts option flags for reusability

### Build Runnable JARs
```bash
  mvn clean package -P multiple-jar-profile
```
The above command packages each sync JAR separately. See module [pom.xml](pom.xml) for packaging details. If running copied + pasted bash commands from this README, set the required environment variables first using actual values

### JAR Option Flags
All sync JARs share the following flags. Exceptions thrown when required flags are missing.

| Option Flags | Description                      |  Required  | Default | Objective                                       |
|:-----:|:---------------------------------|:----------:|:-------:|:------------------------------------------------|
|     `-h`     | Voyager API host                 |  **Yes**   |    -    | reusability across environments                 |
|     `-p`     | port                             |     No     |  3000   | tailor environment endpoint                     |
|    `-at`     | Voyager API authentication token |  **Yes**   |    -    | authorizes calls to admin endpoints             |
|    `-gn`     | registered Geonames username     |  **Yes**   |    -    | enables geocoded enrichment                     |
|    `-tc`     | thread count for multithreading  |     No     |   10    | increase processing rate<br/>capped at **1000** |


## AirportSync
Runnable Class: [`AirportSync.java`](./src/main/java/org/voyager/AirportSync.java)
- Syncs airport type data sourced from [ch-aviation.com](https://www.ch-aviation.com/airports)
- Enriches [Voyager API](https://github.com/maxinefonua/voyager-api) airports with geocoded data from [GeoNames.com](https://www.geonames.org/export/web-services.html#findNearbyPlaceName)
- Any failures are patched with type`UNVERIFIED`(see[`AirportType.java`](../voyager-models/src/main/java/org/voyager/model/airport/AirportType.java))
#### bash Run Command
```bash
  java -jar ./target/airports-sync-$JAR_VERSION.jar -h=$VOYAGER_API_HOST -at=$VOYAGER_API_KEY -gn=$GEONAMES_API_USERNAME -tp=unverified
```
### Overridden and Additional Flags:
The following flags are specific to AirportSync. See linked files for implementation details.

| Option Flags | Description                             |                    Required                     |    Default    | Objective                                                                                                 |
|:------------:|:----------------------------------------|:-----------------------------------------------:|:-------------:|:----------------------------------------------------------------------------------------------------------|
|    `-sy`     | determines default sync or custom modes |                       No                        | `VERIFY_TYPE` | see SyncMode enum in[`AirportSyncConfig.java`](./src/main/java/org/voyager/config/AirportSyncConfig.java) |
|    `-tc`     | thread count for multithreading         |                       No                        |      100      | increased processing throughput                                                                           |
|    `-tl`     | airport type list to process            |                       No                        | `UNVERIFIED`  | see[`AirportType.java`](../voyager-models/src/main/java/org/voyager/model/airport/AirportType.java)       |
|    `-il`     | IATA list for airport codes             |     **Yes** when in <br/>sync mode`ADD_MISSING`      |       -       | used to build airport records and to insert to Voyager                                                    | 

## FlightSync
Runnable Class: [`FlightSync.java`](./src/main/java/org/voyager/FlightSync.java)
- Syncs flight data of civil airports sourced from [Flightradar24.com](https://www.flightradar24.com/data/airlines)
- Uses an executor service for limited multithreaded processing
- Enriches [Voyager API](https://github.com/maxinefonua/voyager-api) airports and routes using [ch-aviation.com](https://www.ch-aviation.com/airports) and [GeoNames.com](https://www.geonames.org/export/web-services.html#findNearbyPlaceName)
- Upserts airline airport records for successfully processed active flights
- Writes failures to target directory for further analysis or retries
### Run JAR Command
FlightSync uses a graavl Script processing engine that logs warnings each time it is used. To siphon those logs separately and have only programmed logs printing to console, the VM option`-Dpolyglot.log.file=./truffle.log`is added to the bash command below.
```bash
    java -Dpolyglot.log.file=./truffle.log -jar ./target/flights-sync-$JAR_VERSION.jar -h=$VOYAGER_API_HOST -at=$VOYAGER_API_KEY -gn=$GEONAMES_API_USERNAME
```
### Overridden and Additional Flags:
The following flags are specific to FlightSync. See linked files for implementation details.

| Option Flags | Description                             |                    Required                     |   Default    | Objective                                                                                               |
|:------------:|:----------------------------------------|:-----------------------------------------------:|:------------:|:--------------------------------------------------------------------------------------------------------|
|    `-sy`     | determines default sync or custom modes |                       No                        | `FULL_SYNC`  | see SyncMode enum in[`FlightSyncConfig.java`](./src/main/java/org/voyager/config/FlightSyncConfig.java) |
|    `-tc`     | thread count for multithreading         |                       No                        |      3       | limits request rates to external enrichment service<br>capped at **5**                                  |