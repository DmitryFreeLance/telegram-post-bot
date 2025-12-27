# Multi-stage build: Maven -> slim JRE
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

COPY src ./src

# Build and normalize jar name
RUN mvn -q -DskipTests package && \
    echo "Built artifacts in target/:" && ls -la target && \
    JAR="$(ls -1 target/*.jar | grep -vE '(^|/)(original-|.*-sources\.jar$|.*-javadoc\.jar$)' | head -n 1)" && \
    echo "Using jar: ${JAR}" && \
    cp "${JAR}" /build/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app

VOLUME ["/data", "/assets"]
ENV DB_PATH=/data/bot.db \
    POST_IMAGE_PATH=/assets/1.jpg \
    GROUP_CHAT_ID=-1003060928185

COPY --from=build /build/app.jar /app/app.jar

# IMPORTANT: запуск через -cp (не нужен Main-Class в манифесте)
ENTRYPOINT ["java", "-cp", "/app/app.jar", "com.workspacers.postbot.Main"]