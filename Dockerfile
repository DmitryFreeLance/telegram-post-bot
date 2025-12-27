# ---- build stage ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -e -DskipTests package

# ---- runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# default locations (override via env)
ENV DB_PATH=/data/bot.db \
    POST_IMAGE_PATH=/app/1.jpg \
    GROUP_CHAT_ID=-1003060928185

# Create a writable folder for SQLite DB
RUN mkdir -p /data

# App jar
COPY --from=build /src/target/telegram-post-bot-1.0.0-shaded.jar /app/app.jar

# Put image into image by default (can be overridden by mounting /app/1.jpg)
COPY src/main/resources/1.jpg /app/1.jpg

CMD ["java", "-jar", "/app/app.jar"]
