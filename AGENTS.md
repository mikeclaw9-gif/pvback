<!-- CODEGRAPH_START -->
## CodeGraph

In repositories indexed by CodeGraph (a `.codegraph/` directory exists at the repo root), reach for it BEFORE grep/find or reading files when you need to understand or locate code:

- **MCP tools** (when available): `codegraph_explore` answers most code questions in one call — the relevant symbols' verbatim source plus the call paths between them. `codegraph_node` returns one symbol's source + callers, or reads a whole file with line numbers. If the tools are listed but deferred, load them by name via tool search.
- **Shell** (always works): `codegraph explore "<symbol names or question>"` and `codegraph node <symbol-or-file>` print the same output.

If there is no `.codegraph/` directory, skip CodeGraph entirely — indexing is the user's decision.
<!-- CODEGRAPH_END -->

## Project: pventabase

Point-of-sale REST API. Spring Boot 3.2.5 / Java 17 / Maven multi-module / PostgreSQL + Flyway / JWT auth.

### Modules

| Module | Purpose |
|--------|---------|
| `pventabase-common` | Shared kernel — `BaseEntity`, exceptions, DTOs, constants |
| `pventabase-usuarios` | User CRUD + roles (ROLE_ADMIN, ROLE_USER, ROLE_VENDEDOR) |
| `pventabase-login` | JWT auth (`/auth/login`, `/auth/register`). Depends on usuarios module |
| `pventabase-inventario` | Products CRUD |
| `pventabase-clientes` | Clients CRUD + soft delete |
| `pventabase-ventas` | Sales CRUD, details, ticket generation, finalize/void flow |
| `pventabase-gastos` | Expenses CRUD with categories and payment methods |
| `pventabase-corte-caja` | Cash register opening/closing with automatic totals |
| `pventabase-reportes` | Reports (ventas, productos, gastos, stock, clientes, cortes, dashboard) with Excel/PDF/JSON/Print export |
| `pventabase-app` | Entry point, SecurityConfig, GlobalExceptionHandler, OpenAPI, Flyway migrations |

### Architecture conventions

- Entry point: `PventabaseApplication` (`pventabase-app`) — `@SpringBootApplication(scanBasePackages = "com.pventabase")` scans all modules; same for `@EntityScan` and `@EnableJpaRepositories`
- Constructor injection only (`@RequiredArgsConstructor`)
- `@Transactional` exclusively on Service classes; `@Valid` on Controller DTOs
- Layered per module: `controller/` → `dto/` → `entity/` → `mapper/` → `repository/` → `service/`
- Cross-module dependencies allowed (e.g. `pventabase-ventas` depends on `pventabase-clientes`, `pventabase-inventario`, `pventabase-usuarios`; `pventabase-corte-caja` depends on `pventabase-ventas`, `pventabase-gastos`, `pventabase-usuarios`; `pventabase-reportes` depends on all domain modules)

### Key commands

```sh
# Build the whole project (skip tests)
mvn clean package -DskipTests

# Build only the app module (what Dockerfile does)
mvn package -DskipTests -pl pventabase-app -am

# Run
mvn spring-boot:run -pl pventabase-app
```

### API & server

| Resource | URL |
|----------|-----|
| Base | `http://localhost:8090/api` |
| Swagger UI | `/api/swagger-ui.html` |
| OpenAPI JSON | `/api/v3/api-docs` |

All endpoints except `/auth/**` require `Authorization: Bearer <token>`. **However**, `SecurityConfig` currently has `.anyRequest().permitAll()` — the JWT filter runs (authenticates if valid token present) but does **not** block unauthenticated requests.

### Security

- JWT with HMAC-SHA256. Secret: `PventabaseClaveSecretaSegura2026ParaJWT`. Expiration: 24h (86400000ms).
- Password encoder: BCrypt (`PasswordEncoder` bean)
- CSRF disabled. Session: STATELESS. CORS: all origins allowed with credentials.
- JWT claims: `sub` = email, `rol` = role string (e.g. `ROLE_ADMIN`)

### Database

- PostgreSQL. DDL: `spring.jpa.hibernate.ddl-auto=validate` — Flyway manages schema
- 10 Flyway migrations (V1–V10) in `pventabase-app/src/main/resources/db/migration/`

### Notable mapping quirks

- `Cliente` entity maps frontend field names to different DB columns: `direccion` → `domicilio`, `telefono` → `celular`, `email` → `correo`
- All MapStruct mappers (via `componentModel = "spring"`) ignore `BaseEntity` fields (id, createdAt, updatedAt, createdBy, updatedBy, activo) on `toEntity`/`updateEntity`
- `UsuarioMapper.updateEntity` also ignores `password`
- `ClienteMapper.toEntity`/`updateEntity` also ignore `eliminado`
- Lombok + MapStruct annotation processing order is handled by `lombok-mapstruct-binding` 0.2.0 in the compiler plugin config

### Key bugs fixed in this session

- `VentaService.create()` used `COMPLETADA` status but `agregarDetalle()` required `PENDIENTE` — changed create to set `PENDIENTE` and removed stock deduction (now done in `finalizarVenta()`)
- `GlobalExceptionHandler` was missing handlers for `VentaNoEncontradaException` and `StockInsuficienteException` — added both (return 404 and 400 respectively)
- Added `metodoPago` field to `Venta` entity for payment method breakdown in reports and corte de caja
- `ReporteService` had no `@Transactional` causing `LazyInitializationException` on lazy associations — added `@Transactional(readOnly = true)`
- `ReporteController` expected individual query params but frontend sends `filter` JSON string — changed to parse `filter` parameter with Jackson

### Flyway migrations

| Migration | Description |
|-----------|-------------|
| V1 | Create usuarios table |
| V2 | Add audit columns to usuarios |
| V3 | Create producto table |
| V4 | Create clientes table |
| V5 | Add documento to clientes |
| V6 | Create ventas + detalle_venta tables, add pesado to producto |
| V7 | Add imagen_url to producto |
| V8 | Create gastos table |
| V9 | Add metodo_pago to ventas |
| V10 | Create cortes_caja table |

### Docker

```sh
# Build image
docker build -t pventabase:1.0 .

# Run (expects external PostgreSQL)
docker compose up
```

Note: `docker-compose.yml` has no PostgreSQL service — assumes an external instance at `host.docker.internal:5432/pventabase_db`. The Dockerfile builds with `mvn package -DskipTests -pl pventabase-app -am`.

### What's missing

- No tests exist anywhere in the repo
- No lint/format/typecheck tooling configured
- No CI workflows
- No pre-commit hooks

### How to Answer
Always answer in Spanish, using short answers; when asked for an explanation, provide more detailed answers.
Always add the following text at the end of your answers: “ ---RESPUESTA---”
