# ── Stage 1: Builder ───────────────────────────────────────────────
FROM eclipse-temurin:24-jdk-alpine AS builder
WORKDIR /workspace

# Kopiuj Maven Wrapper i wszystkie pom.xml osobno — cache layer
# zmiana kodu źródłowego nie unieważnia dependency download
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY common/pom.xml            common/pom.xml
COPY api-gateway/pom.xml       api-gateway/pom.xml
COPY data-collector/pom.xml    data-collector/pom.xml
COPY competitor-agent/pom.xml  competitor-agent/pom.xml

RUN chmod +x mvnw

# Pobierz zależności (cached gdy pom.xml się nie zmienił)
RUN ./mvnw dependency:go-offline \
      -pl competitor-agent -am \
      --no-transfer-progress -q

# Kopiuj źródła i buduj (tylko moduły potrzebne do competitor-agent)
COPY common/src            common/src/
COPY competitor-agent/src  competitor-agent/src/
RUN ./mvnw package \
      -pl competitor-agent -am \
      -DskipTests \
      --no-transfer-progress -q

# ── Stage 2: Extractor — rozpakowuje Spring Boot Layered JAR ───────
FROM eclipse-temurin:24-jdk-alpine AS extractor
WORKDIR /workspace
COPY --from=builder /workspace/competitor-agent/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# ── Stage 3: Runtime — minimalne JRE, non-root user ───────────────
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app

# Non-root user (wymóg bezpieczeństwa w produkcji)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Warstwy od najbardziej stabilnej do najbardziej zmiennej
# (zmiana kodu = rebuild tylko warstwy application/)
COPY --from=extractor /workspace/dependencies/          ./
COPY --from=extractor /workspace/spring-boot-loader/    ./
COPY --from=extractor /workspace/snapshot-dependencies/ ./
COPY --from=extractor /workspace/application/           ./

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "org.springframework.boot.loader.launch.JarLauncher"]