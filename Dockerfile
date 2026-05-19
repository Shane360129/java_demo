# Stage 1: build frontend
FROM node:20-alpine AS frontend
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npx vite build --outDir dist --emptyOutDir

# Stage 2: build backend
FROM maven:3.9-eclipse-temurin-17 AS backend
WORKDIR /app
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
COPY --from=frontend /app/dist ./src/main/resources/static
RUN mvn -B -q clean package -DskipTests

# Stage 3: runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend /app/target/task-demo.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
