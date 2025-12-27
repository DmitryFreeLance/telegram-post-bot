# ---- build stage ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -e -DskipTests package && \
    ls -la target && \
    JAR="$(ls -1 target/*-shaded.jar 2>/dev/null | head -n 1)" && \
    if [ -z "$JAR" ]; then JAR="$(ls -1 target/*.jar | grep -v '\.original$' | head -n 1)"; fi && \
    echo "Using jar: $JAR" && \
    cp "$JAR" /src/app.jar

# ---- runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

ENV DB_PATH=/data/bot.db \
    POST_IMAGE_PATH=/app/1.jpg \
    GROUP_CHAT_ID=-1003060928185

RUN mkdir -p /data

COPY --from=build /src/app.jar /app/app.jar
COPY src/main/resources/1.jpg /app/1.jpg

CMD ["java", "-jar", "/app/app.jar"]