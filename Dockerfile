FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

EXPOSE ${PORT}

# Oluşturulan JAR dosyasını kopyala (target klasöründe oluşur)
COPY --from=build /app/target/*.jar app.jar

ENV TZ=Europe/Istanbul

ENTRYPOINT ["java", "-jar", "app.jar"]
