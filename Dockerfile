FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY pventabase-common/pom.xml pventabase-common/
COPY pventabase-usuarios/pom.xml pventabase-usuarios/
COPY pventabase-login/pom.xml pventabase-login/
COPY pventabase-inventario/pom.xml pventabase-inventario/
COPY pventabase-clientes/pom.xml pventabase-clientes/
COPY pventabase-ventas/pom.xml pventabase-ventas/
COPY pventabase-app/pom.xml pventabase-app/

RUN mvn dependency:go-offline -B

COPY pventabase-common/src pventabase-common/src/
COPY pventabase-usuarios/src pventabase-usuarios/src/
COPY pventabase-login/src pventabase-login/src/
COPY pventabase-inventario/src pventabase-inventario/src/
COPY pventabase-clientes/src pventabase-clientes/src/
COPY pventabase-ventas/src pventabase-ventas/src/
COPY pventabase-app/src pventabase-app/src/

RUN mvn package -DskipTests -B -pl pventabase-app -am

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder /app/pventabase-app/target/*.jar app.jar

EXPOSE 8080

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
