# syntax=docker/dockerfile:1
# ---- build stage -----------------------------------------------------------
FROM amazoncorretto:26 AS build
WORKDIR /app
# Resolve dependencies first so source-only changes reuse this layer.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B -DskipTests package && mv target/eventflow-*.jar app.jar

# ---- runtime stage ---------------------------------------------------------
FROM amazoncorretto:26-headless
RUN useradd --system --uid 10001 --no-create-home occavie
WORKDIR /app
COPY --from=build /app/app.jar app.jar
USER occavie
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
# Render sets PORT; the app falls back to 8080 locally.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
