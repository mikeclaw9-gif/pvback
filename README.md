# pvback — Pventa POS Backend

API REST de punto de venta (POS) construida con Spring Boot 3.2.5, Java 17, PostgreSQL y JWT.

## Arquitectura

Multi-módulo Maven con 10 módulos:

| Módulo | Propósito |
|--------|-----------|
| `pventabase-common` | Clases compartidas (BaseEntity, excepciones, DTOs, constantes) |
| `pventabase-usuarios` | CRUD de usuarios y roles |
| `pventabase-login` | Autenticación JWT (login/register) |
| `pventabase-inventario` | CRUD de productos |
| `pventabase-clientes` | CRUD de clientes con borrado suave |
| `pventabase-ventas` | Gestión de ventas, detalles, tickets, finalizar/anular |
| `pventabase-gastos` | Gestión de gastos por categoría y método de pago |
| `pventabase-corte-caja` | Apertura y cierre de caja con totales automáticos |
| `pventabase-reportes` | Reportes exportables (Excel, PDF, JSON, impresión) con gráficos |
| `pventabase-app` | Punto de entrada, seguridad, migrations Flyway |

## Requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 15+

## Ejecución rápida

Primera vez (o después de `mvn clean`):

```sh
# Compilar e instalar todos los módulos en el repo local de Maven
mvn install -DskipTests

# Ejecutar
mvn spring-boot:run -pl pventabase-app
```

**Importante**: `mvn package` no basta — `spring-boot:run` resuelve dependencias desde `~/.m2/repository`, no desde `target/` de los módulos hermanos. Usar `mvn install` (o `mvn install -DskipTests -pl pventabase-app -am` para solo afectar el árbol de `app`).

## Documentación API

- Swagger UI: http://localhost:8090/api/swagger-ui.html
- OpenAPI: http://localhost:8090/api/v3/api-docs

## Licencia

Uso interno — Pventa POS
