# Buildeo con mvn
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace


COPY pom.xml .
COPY domain/pom.xml domain/pom.xml
COPY application/pom.xml application/pom.xml
COPY infrastructure/pom.xml infrastructure/pom.xml
COPY api/pom.xml api/pom.xml
COPY boot/pom.xml boot/pom.xml

RUN mvn -q -B -DskipTests dependency:go-offline


COPY . .

RUN mvn -q -B -DskipTests -pl boot -am package

RUN cp boot/target/*.jar /workspace/app.jar


# Imagen runtime, unicamente JRE
FROM eclipse-temurin:21-jre AS runtime

# usuario no root 
RUN useradd --system --create-home --uid 10001 appuser
WORKDIR /app

COPY --from=build --chown=10001:10001 /workspace/app.jar /app/app.jar

# Drop root privileges
USER appuser

# Sensible JVM defaults for containers, , evita que JVM tome todo, guardamos espacio para threads, os overhead
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
