# Flash Cards Java
[![Java CI with Maven](https://github.com/codingchica/flash_cards_java/actions/workflows/maven.yml/badge.svg)](https://github.com/codingchica/flash_cards_java/actions/workflows/maven.yml)

## How to start the flash_cards_java application
---

1. Run `mvn clean install` to build your application
2. Start application with `java -jar flash-cards-api/target/flash-cards-api-0.1-SNAPSHOT.jar server flash-cards-api/src/main/resources/appConfig/prod.yml`
3. To check that your application is running enter url `http://localhost:8080/ui/index.html`

## Health Check
---

To see your application's health enter url `http://localhost:8081/healthcheck`
