# syntax=docker/dockerfile:1

FROM node:24-alpine AS frontend-build
WORKDIR /workspace/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
ENV VITE_API_BASE_URL=""
RUN npm run build

FROM eclipse-temurin:17-jdk-jammy AS backend-build
WORKDIR /workspace/backend
COPY backend/gradlew backend/gradlew.bat backend/settings.gradle backend/build.gradle ./
COPY backend/gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon
COPY backend/src ./src
COPY --from=frontend-build /workspace/frontend/dist ./src/main/resources/static
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd --system greenstone && useradd --system --gid greenstone greenstone \
    && mkdir -p /tmp/greenstone-uploads \
    && chown -R greenstone:greenstone /app /tmp/greenstone-uploads
COPY --from=backend-build --chown=greenstone:greenstone /workspace/backend/build/libs/backend-*.jar app.jar
USER greenstone
ENV PORT=8080 \
    UPLOAD_DIRECTORY=/tmp/greenstone-uploads \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
