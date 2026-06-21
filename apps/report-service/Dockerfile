# Etapa 1: Compilar la aplicación
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw clean package -DskipTests

# Etapa 2: Imagen ligera de ejecución
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /app/target/*.jar /app-bin/app.jar
RUN chown -R appuser:appgroup /app-bin
USER appuser
EXPOSE 8089
CMD ["java", "-jar", "/app-bin/app.jar"]
