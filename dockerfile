# -------- STAGE 1: Build with Maven (JDK 21) --------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY domain/pom.xml         domain/pom.xml
COPY application/pom.xml    application/pom.xml
COPY infrastructure/pom.xml infrastructure/pom.xml
COPY api/pom.xml            api/pom.xml
COPY boot/pom.xml           boot/pom.xml

RUN mvn -q -B -DskipTests dependency:go-offline

# Codigo fuente
COPY . .

# packageo
RUN mvn -q -B -DskipTests -pl boot -am package

# jar final
RUN cp boot/target/*.jar /workspace/app.jar


# -------- STAGE 2: Runtime (JRE only) --------
FROM eclipse-temurin:21-jre AS runtime

# Optional JVM ergonomics for containers
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

WORKDIR /app
COPY --from=build /workspace/app.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
