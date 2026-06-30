<!-- CODEGRAPH_START -->
## CodeGraph

Este repo tiene `.codegraph/` — usa `codegraph_explore` (MCP) o `codegraph explore` (shell) ANTES de grep/Read para entender símbolos y flujos. Name a file or symbol in the query.
<!-- CODEGRAPH_END -->

## pventabase

POS REST API. Spring Boot 3.2.5 / Java 17 / Maven multi-módulo / PostgreSQL + Flyway / JWT auth.

### Módulos (10)

| Módulo | Propósito |
|--------|-----------|
| `pventabase-common` | `BaseEntity`, excepciones, DTOs, constantes |
| `pventabase-usuarios` | CRUD usuarios + roles |
| `pventabase-login` | `/auth/login`, `/auth/register`, JWT |
| `pventabase-inventario` | CRUD productos |
| `pventabase-clientes` | CRUD clientes + soft delete |
| `pventabase-ventas` | Ventas, detalles, finalizar/anular, ticket |
| `pventabase-gastos` | Gastos por categoría + método pago |
| `pventabase-corte-caja` | Apertura/cierre caja con totales automáticos |
| `pventabase-reportes` | Reportes exportables (Excel/PDF/JSON/Print) |
| `pventabase-app` | Entry point, SecurityConfig, GlobalExceptionHandler, OpenAPI, Flyway migrations |

### Arquitectura

- **Entry point**: `PventabaseApplication` — `scanBasePackages = "com.pventabase"`, igual para `@EntityScan` y `@EnableJpaRepositories`
- **Inyección**: solo constructor con `@RequiredArgsConstructor`
- **`@Transactional`** solo en Services; `@Valid` solo en DTOs de Controller
- **Layers por módulo**: `controller/` → `dto/` → `entity/` → `mapper/` → `repository/` → `service/`
- **Dependencias cross-module**: `ventas` → `clientes`+`inventario`+`usuarios`; `corte-caja` → `ventas`+`gastos`+`usuarios`; `reportes` → todos los módulos de dominio
- **Paginación**: todos los `findAll` usan `PageResponseDTO<T>` con `page`, `size`, `sortBy`, `sortDir`

### Comandos

```sh
# Build completo (skip tests)
mvn clean install -DskipTests

# Build + install solo app module (lo mismo que Dockerfile, pero install, no package)
mvn install -DskipTests -pl pventabase-app -am

# Run (requiere haber hecho install primero)
mvn spring-boot:run -pl pventabase-app
```

### API

| Recurso | URL |
|---------|-----|
| Base | `http://localhost:8090/api` |
| Swagger UI | `/api/swagger-ui.html` |
| OpenAPI JSON | `/api/v3/api-docs` |

### Seguridad

- **Endpoints públicos**: `/health`, `/auth/**`, `/v3/api-docs/**`, `/swagger-ui/**`
- **Todo lo demás**: `.anyRequest().authenticated()` — el JWT filter corre primero, si hay token válido lo autentica, si no hay token deniega 401
- JWT: HMAC-SHA256, secret `PventabaseClaveSecretaSegura2026ParaJWT`, exp 24h
- Claims: `sub` = email, `rol` = string (e.g. `ROLE_ADMIN`)
- Password encoder: BCrypt
- CSRF disabled, STATELESS, CORS `*` con credentials
- Controladores obtienen usuario autenticado vía `Authentication authentication` como parámetro del método

### BD

- PostgreSQL. `ddl-auto=validate` — Flyway administra el schema
- `spring.jpa.open-in-view=false` — si ves `LazyInitializationException`, falta `@Transactional(readOnly = true)`
- Timezone: `America/Argentina/Buenos_Aires`
- 10 migrations (V1–V10) en `pventabase-app/src/main/resources/db/migration/`

### Quirks de mapeo

- `Cliente`: `direccion` → DB `domicilio`, `telefono` → `celular`, `email` → `correo`
- `Producto`: override de columnas audit → `fecha_creacion`/`fecha_modificacion`
- Todos los MapStruct mappers ignoran `BaseEntity` fields (`id`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `activo`) en `toEntity`/`updateEntity`
- `UsuarioMapper.updateEntity` también ignora `password`
- `ClienteMapper.toEntity`/`updateEntity` también ignoran `eliminado`
- Lombok + MapStruct: requieren `lombok-mapstruct-binding` 0.2.0 en annotationProcessorPaths

### Decisiones de diseño notables (no revertir)

- `VentaService.create()` crea ventas con estado `PENDIENTE`, stock se descuenta solo en `finalizarVenta()`
- `VentaService.agregarDetalle()` y `eliminarDetalle()` solo funcionan en estado `PENDIENTE`
- `DetalleVentaResponseDTO` incluye `productoDescripcion` mapeado desde `Producto.descripcion`; `TicketResponseDTO.LineaTicket` incluye `descripcion` (ambos agregados en la sesión del 30 Jun 2026)
- `ReporteController` recibe filtros como string JSON (`filter` param) parseado con Jackson, no query params individuales
- `@SuppressFBWarnings("EI_EXPOSE_REP2")` en constructores con `@RequiredArgsConstructor(onConstructor_ = ...)` — patrón usado en services

### Checkstyle

Solo `pventabase-ventas/checkstyle.xml` existe con reglas mínimas. No hay SpotBugs config ni CI.

### Docker

```sh
docker build -t pventabase:1.0 .
docker compose up
```

`docker-compose.yml` NO tiene PostgreSQL — espera instancia externa en `host.docker.internal:5432/pventabase_db`.

### Lo que NO existe

- Tests (cero en todo el repo)
- Lint/format/typecheck tooling
- CI workflows
- Pre-commit hooks

### How to Answer

Siempre responde en español, con respuestas cortas. Cuando te pidan explicación, da más detalle. Termina siempre con: `---RESPUESTA---`
