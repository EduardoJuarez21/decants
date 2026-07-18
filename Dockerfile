# Stage 1: build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2: run
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache libwebp-tools
WORKDIR /app
RUN mkdir -p /app/uploads/img/alta-perfumeria/hombre \
             /app/uploads/img/alta-perfumeria/mujer \
             /app/uploads/img/arabe
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
