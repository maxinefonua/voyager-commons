# <img src="voyager-models/src/main/resources/images/logo.svg" width="30"> Voyager Commons
### commons modules for Voyager project
A personal project I took on to relearn full-cycle development, and to better utilize my airline employee flight benefits. Modules created to decouple dependencies and enable continuous integration and development.

### Project Repos:
- Voyager UI: https://github.com/maxinefonua/voyager-ui
  - mapped requests and web feature functions
  - dynamic page injection and  targeted fragment reloads
- Voyager API: https://github.com/maxinefonua/voyager-api
  - standalone backend services
  - caching, request limits, auth tokens
- Voyager Commons: https://github.com/maxinefonua/voyager-commons
  - an SDK for API services
  - scripts and jars for data syncing
- Voyager Tests: https://github.com/maxinefonua/voyager-tests
  - functional tests built with JUnit 5
  - an uber jar deployed and used for application deployments

## Commons Modules:
#### <i>voyager-datasync</i>
- buildable JARs for orchestrating scheduled sync jobs
#### <i>voyager-models</i>
- shared data models and utils to centralize edits
#### <i>voyager-sdk</i>
- configurable SDK for Voyager API with no Spring dependencies

### Tech Stack:
- Geolocation Data 
  - GeoNames - https://www.geonames.org.com/
  - Nominatim - https://operations.osmfoundation.org/policies/nominatim/
- Flight/Airport Data 
  - AirportsData - https://github.com/mborsetti/airportsdata/
  - FlightRadar24 - https://www.flightradar24.com/about
  - ch-aviation - https://www.ch-aviation.com/about
- Development 
  - GitHub - https://github.com/
  - IntelliJ Community Edition - https://www.jetbrains.com/idea/download/?section=mac
  - Apache Maven - https://maven.apache.org/
- Data Storage and Hosting
  - AWS EC2 Instance - https://docs.aws.amazon.com/ec2/
  - PostgeSQL - https://www.postgresql.org/about/
  - pgAdmin - https://www.pgadmin.org/

Full README and LICENSE coming soon.