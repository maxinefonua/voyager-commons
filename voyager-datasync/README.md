## AirlineFullSync Order of Operations
### As of July 3, 2025:

Pre-syncing Checklist
- Update enum <strong>[Airline.java]</strong> with intended airline and code 
- Update PostgreSQL table <strong>[airline]</strong> with intended with query:
  -     INSERT INTO airline(name) VALUES ('AIRLINE_NAME')
- Build and publish <strong>voyager-commons</strong> to .m2 with command:
  -     mvn clean install -U
- Clean, compile, start <strong>voyager-api</strong> to pull airline enum updates
  -     mvn clean compile

### 1. RoutesSync
    - pulls airports of routes for intended airline
    - publishes routes via Voyager API
    - prints airline airports to local file for processing
    - multithreaded
### 2. FlightsSync
     - iterates local file of airline airports to process
     - pulls flight info for intended airline at each airport 
     - publishes flights via Voyager API
     - single-threaded FlightRadarService for rate limiting
### 3. AirlineSync
    - fetches flights via Voyager API by airline
    - prints PostgreSQL insertion script of airline airports to local file

Post-syncing Checklist
- Copy and run PostgreSQL insertion script to update <strong>[airline_airports]</strong> table
- Save selected null flight airport codes
- Reprocess those airports with -st=flights_sync,airline_sync
- Repeat Steps 2, 3 and post-sync until 0 created flights and 0 patched flights