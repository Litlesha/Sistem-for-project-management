FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app
COPY . /app

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk

WORKDIR /app

# ВАЖНО: используем jar-файл, собранный Maven'ом
COPY --from=builder /app/target/CourseProject-0.0.1-SNAPSHOT.jar /app/diploma.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "diploma.jar"]
