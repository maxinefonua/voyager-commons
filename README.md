# <img src="voyager-models/src/main/resources/images/logo.svg" width="30"> Voyager Commons
### commons modules for Voyager project
A personal project I took on to relearn full-cycle development, and to better organize my travel wish list. Modules created to decouple dependencies and enable continuous integration and development.

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

## Project Modules:
#### <i>voyager-datasync</i>
- bash scripts and executable jars to fetch/update db entries
- buildable jars as standalone CronJobs for syncing data
#### <i>voyager-models</i>
- shared data models and utils to centralize edits
#### <i>voyager-sdk</i>
- an SDK with no Spring dependencies for Voyager API services

### Modules Tech Stack:
- Bash
- Lombok
- JSoup
- Spring Validation


Full README and LICENSE coming soon.