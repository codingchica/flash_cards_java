# Flash Cards Java
[![Java CI with Maven](https://github.com/codingchica/flash_cards_java/actions/workflows/maven.yml/badge.svg)](https://github.com/codingchica/flash_cards_java/actions/workflows/maven.yml)

## How to start the flash_cards_java application
---

1. Run `mvn clean install` to build your application
2. Change to the sub-directory `cd flash-cards-api`
3. Start application with `java -jar target/flash-cards-api-0.1-SNAPSHOT.jar server appConfig/test-component.yml`
4. To check that your application is running enter url `http://localhost:8080`

## Health Check
---

To see your application's health enter url `http://localhost:8081/healthcheck`
