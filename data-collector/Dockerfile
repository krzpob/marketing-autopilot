# ── Etap 1: Budowanie ────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Kopiujemy najpierw tylko POM-y — Docker cache nie będzie rebuildo­wał
# zależności przy każdej zmianie kodu źródłowego
COPY pom.xml                        ./
COPY common/pom.xml                 common/
COPY api-gateway/pom.xml            api-gateway/
COPY data-collector/pom.xml         data-collector/
COPY competitor-agent/pom.xml       competitor-agent/

RUN ./mvnw dependency:go-offline -pl data-collector -am -q 2>/dev/null || true

# Kopiujemy źródła i budujemy
COPY .mvn/                          .mvn/
COPY mvnw                           ./
COPY common/src                     common/src/
COPY data-collector/src             data-collector/src/

RUN ./mvnw clean package \
        -pl data-collector -am \
        -DskipTests \
        --no-transfer-progress \
    && mv data-collector/target/data-collector-*.jar app.jar

# Rozpakowujemy warstwy Spring Boot — lepsze cache'owanie w runtime image
RUN java -Djarmode=layertools -jar app.jar extract --destination layers

# ── Etap 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Metadane obrazu
LABEL org.opencontainers.image.title="marketing-autopilot/data-collector"
LABEL org.opencontainers.image.description="Instagram data collection service"
LABEL org.opencontainers.image.source="https://github.com/krzpob/marketing-autopilot"

# Użytkownik bez uprawnień root — bezpieczeństwo
RUN addgroup -S autopilot && adduser -S datacollector -G autopilot

WORKDIR /app

# Warstwy Spring Boot — od najrzadziej do najczęściej zmienianych
# (dependencies → spring-boot-loader → snapshot-dependencies → application)
COPY --from=builder /build/layers/dependencies/          ./
COPY --from=builder /build/layers/spring-boot-loader/    ./
COPY --from=builder /build/layers/snapshot-dependencies/ ./
COPY --from=builder /build/layers/application/           ./

RUN chown -R datacollector:autopilot /app

USER datacollector

# Port HTTP serwisu
EXPOSE 8083

# Healthcheck przez Spring Boot Actuator
HEALTHCHECK \
    --interval=30s \
    --timeout=5s \
    --start-period=60s \
    --retries=3 \
    CMD wget -qO- http://localhost:8083/actuator/health | grep -q '"status":"UP"' || exit 1

# JVM tuning dla kontenerów:
# -XX:+UseContainerSupport        — JVM respektuje CPU/RAM limity kontenera
# -XX:MaxRAMPercentage=75.0       — max 75% RAM kontenera dla heapu
# -XX:+ExitOnOutOfMemoryError     — szybki fail zamiast degradacji
# -Djava.security.egd             — szybszy start (entropii w kontenerze mało)
ENV JAVA_OPTS="\
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]