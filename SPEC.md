# SPEC.md — Especificación Técnica de pventabase

## 1. Descripción General

API REST para sistema de punto de venta (POS) con control de inventario, ventas, gastos, corte de caja y reportes exportables. Backend monolítico multi-módulo desarrollado en Java 17 con Spring Boot 3.2.5.

## 2. Stack Tecnológico

| Componente | Versión |
|-----------|---------|
| Java | 17 |
| Spring Boot | 3.2.5 |
| Spring Security | 6.x (incluido en Boot) |
| Spring Data JPA | 3.x (incluido en Boot) |
| PostgreSQL | 15+ |
| Flyway | 9.x (incluido en Boot) |
| JWT (jjwt) | 0.12.5 |
| Lombok | 1.18.30 |
| MapStruct | 1.5.5.Final |
| Lombok-MapStruct-Binding | 0.2.0 |
| SpringDoc OpenAPI | 2.3.0 |
| Apache POI | 5.2.5 |
| JFreeChart | 1.5.4 |
| OpenPDF | 1.3.39 |

## 3. Estructura del Proyecto

```
pvback/
├── pom.xml                              (Parent POM — gestión de dependencias)
├── AGENTS.md                            (Instrucciones para asistentes IA)
├── README.md
├── SPEC.md
├── Dockerfile
├── docker-compose.yml
├── pventabase-common/                   (Módulo compartido)
│   └── src/main/java/com/pventabase/common/
│       ├── constants/  AppConstants, ErrorCodes
│       ├── dto/        BaseResponseDTO, PageResponseDTO
│       ├── entity/     BaseEntity (MappedSuperclass con id, createdAt, updatedAt, createdBy, updatedBy, activo)
│       └── exception/  BusinessException, ResourceNotFoundException, DuplicateResourceException, InvalidStateException
├── pventabase-usuarios/                 (Módulo de usuarios)
├── pventabase-login/                    (Módulo de autenticación JWT)
├── pventabase-inventario/               (Módulo de productos/inventario)
├── pventabase-clientes/                 (Módulo de clientes)
├── pventabase-ventas/                   (Módulo de ventas y detalles)
├── pventabase-gastos/                   (Módulo de gastos)
├── pventabase-corte-caja/               (Módulo de corte de caja)
├── pventabase-reportes/                 (Módulo de reportes exportables)
└── pventabase-app/                      (Módulo de entrada — Application, Security, Flyway)
```

## 4. Módulos — Detalle

### 4.1 pventabase-common (compartido)

**Ubicación:** `com.pventabase.common`

**Clases:**
- `BaseEntity` — `@MappedSuperclass` con `id` (Long, auto-increment), `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `activo` (Boolean, default true). `@PrePersist`/`@PreUpdate` para timestamps.
- `BaseResponseDTO` — DTO abstracto con los mismos campos que BaseEntity (para respuestas JSON).
- `PageResponseDTO<T>` — DTO genérico para paginación: `content`, `page`, `size`, `totalElements`, `totalPages`, `last`, `first`, `empty`.
- `AppConstants` — Constantes: `DEFAULT_PAGE=0`, `DEFAULT_SIZE=10`, `DEFAULT_SORT_BY=id`, `DEFAULT_SORT_DIR=asc`, roles, headers.
- `ErrorCodes` — Códigos de error: `RESOURCE_NOT_FOUND`, `DUPLICATE_RESOURCE`, `INVALID_STATE`, `VALIDATION_ERROR`, `INTERNAL_ERROR`.
- `BusinessException` — Excepción base runtime con `errorCode`.
- `ResourceNotFoundException` — extends BusinessException, código 404.
- `DuplicateResourceException` — extends BusinessException, código 409.
- `InvalidStateException` — extends BusinessException, código 400.

### 4.2 pventabase-usuarios

**Paquete:** `com.pventabase.usuarios`

**Endpoint base:** `/api/usuarios`

**Entidad `Usuario`** — Tabla `usuarios`:
| Campo | Tipo | DB Column | Restricciones |
|-------|------|-----------|---------------|
| id | Long (BaseEntity) | id | PK, auto-increment |
| nombre | String | nombre | NOT NULL, max 100 |
| apellido | String | apellido | NOT NULL, max 100 |
| email | String | email | NOT NULL, UNIQUE, max 150 |
| password | String | password | NOT NULL, max 255 (BCrypt hash) |
| rol | RolUsuario (enum) | rol | NOT NULL, default ROLE_USER. STRING: ROLE_ADMIN, ROLE_USER, ROLE_VENDEDOR |
| telefono | String | telefono | max 20 |

**DTOs:** `UsuarioRequestDTO`, `UsuarioResponseDTO` (extends BaseResponseDTO)
**Mapper:** `UsuarioMapper` — ignora `password` en updateEntity
**Repository:** `UsuarioRepository` — `findByEmail`, `existsByEmail`, `existsByEmailAndIdNot`

**Endpoints:**
| Método | Path | Descripción |
|--------|------|-------------|
| GET | `/usuarios` | Lista paginada |
| GET | `/usuarios/{id}` | Por ID |
| GET | `/usuarios/email/{email}` | Por email |
| POST | `/usuarios` | Crear |
| PUT | `/usuarios/{id}` | Actualizar |
| DELETE | `/usuarios/{id}` | Eliminar |
| PATCH | `/usuarios/{id}/toggle-activo` | Activar/desactivar |

### 4.3 pventabase-login

**Paquete:** `com.pventabase.login`

**Endpoint base:** `/api/auth`

**Dependencia:** pventabase-usuarios (reusa UsuarioRepository, RolUsuario)

**Seguridad JWT:**
- Algoritmo: HMAC-SHA256
- Secreto: `PventabaseClaveSecretaSegura2026ParaJWT`
- Expiración: 24 horas (86400000 ms)
- Claims: `sub` = email, `rol` = rol string (ej. `ROLE_ADMIN`)
- Filtro: `JwtAuthenticationFilter` — extrae token del header `Authorization: Bearer <token>`, valida y setea `SecurityContext`

**DTOs:** `LoginRequestDTO` (email, password), `LoginResponseDTO` (token, email, nombre, rol), `RegisterRequestDTO` (nombre, apellido, email, password, rol, telefono)

**Endpoints:**
| Método | Path | Auth | Descripción |
|--------|------|------|-------------|
| POST | `/auth/login` | No | Autentica y retorna JWT |
| POST | `/auth/register` | No | Registra nuevo usuario y retorna JWT |

### 4.4 pventabase-inventario

**Paquete:** `com.pventabase.inventario`

**Endpoint base:** `/api/productos`

**Entidad `Producto`** — Tabla `producto`, extiende BaseEntity con `@AttributeOverrides` para `fecha_creacion`/`fecha_modificacion`:
| Campo | Tipo | DB Column | Restricciones |
|-------|------|-----------|---------------|
| codigo | String | codigo | NOT NULL, UNIQUE, max 50 |
| nombre | String | nombre | NOT NULL, max 150 |
| descripcion | String | descripcion | TEXT |
| precioCompra | BigDecimal | precio_compra | NOT NULL, >= 0, precision 19, scale 2 |
| precioVenta | BigDecimal | precio_venta | NOT NULL, >= 0, precision 19, scale 2 |
| existencia | Integer | existencia | NOT NULL, default 0, >= 0 |
| pesado | Boolean | pesado | NOT NULL, default false |
| imagenUrl | String | imagen_url | max 500 |

**DTOs:** `ProductoRequestDTO`, `ProductoResponseDTO` (extends BaseResponseDTO)

**Endpoints** — CRUD estándar + `GET /productos/codigo/{codigo}` + `PATCH /productos/{id}/toggle-activo`

### 4.5 pventabase-clientes

**Paquete:** `com.pventabase.clientes`

**Endpoint base:** `/api/clientes`

**Entidad `Cliente`** — Tabla `clientes`:
| Campo | Tipo | DB Column | Restricciones | Nota Mapper |
|-------|------|-----------|---------------|-------------|
| nombre | String | nombre | NOT NULL, max 100 | |
| apellido | String | apellido | NOT NULL, max 100 | |
| direccion | String | domicilio | NOT NULL, max 255 | Frontend envía `direccion` → DB `domicilio` |
| telefono | String | celular | max 20 | Frontend envía `telefono` → DB `celular` |
| email | String | correo | NOT NULL, UNIQUE, max 150 | Frontend envía `email` → DB `correo` |
| credito | BigDecimal | credito | precision 10, scale 2 | |
| eliminado | Boolean | eliminado | NOT NULL, default false | Ignorado en toEntity/updateEntity |
| documento | String | documento | max 20 | |

**DTOs:** `ClienteRequestDTO`, `ClienteResponseDTO` (extends BaseResponseDTO)

**Endpoints** — CRUD estándar + `GET /clientes/email/{email}` + `PATCH /clientes/{id}/toggle-activo` + `PATCH /clientes/{id}/marcar-eliminado`

### 4.6 pventabase-ventas

**Paquete:** `com.pventabase.ventas`

**Endpoint base:** `/api/ventas`

**Dependencias:** pventabase-clientes, pventabase-inventario, pventabase-usuarios

**Entidad `Venta`** — Tabla `ventas`:
| Campo | Tipo | DB Column | Restricciones |
|-------|------|-----------|---------------|
| fecha | LocalDateTime | fecha | NOT NULL |
| total | BigDecimal | total | NOT NULL, precision 19, scale 2 |
| descuentoPorcentaje | Integer | descuento_porcentaje | |
| metodoPago | String | metodo_pago | max 50 (EFECTIVO, TARJETA_DEBITO, TARJETA_CREDITO, TRANSFERENCIA) |
| estado | EstadoVenta (enum) | estado | NOT NULL, STRING: PENDIENTE, COMPLETADA, CANCELADA |
| cliente | @ManyToOne(LAZY) | cliente_id | FK → clientes |
| usuario | @ManyToOne(LAZY) | usuario_id | FK → usuarios |
| detalles | @OneToMany(cascade=ALL, orphanRemoval) | | Cascade ALL |

**Entidad `DetalleVenta`** — Tabla `detalle_venta`:
| Campo | Tipo | Restricciones |
|-------|------|---------------|
| venta | @ManyToOne(LAZY) | FK → ventas, NOT NULL |
| producto | @ManyToOne(LAZY) | FK → producto, NOT NULL |
| cantidad | BigDecimal | NOT NULL, precision 19, scale 3 |
| precioUnitario | BigDecimal | NOT NULL, precision 19, scale 2 |
| descuentoPorcentaje | Integer | |
| subtotal | BigDecimal | NOT NULL, precision 19, scale 2. Calculado en @PrePersist/@PreUpdate |

**Flujo de venta:**
1. `POST /ventas` → crea venta PENDIENTE con detalles (NO descuenta stock)
2. `POST /ventas/{id}/detalles` → agrega productos a venta PENDIENTE
3. `PUT /ventas/{id}/finalizar` → cambia a COMPLETADA, descuenta stock
4. `PUT /ventas/{id}/anular` → cambia a CANCELADA, restaura stock

**Excepciones personalizadas:**
- `VentaNoEncontradaException` → 404 NOT_FOUND
- `StockInsuficienteException` → 400 BAD_REQUEST

**Queries agregadas en VentaRepository:**
- `sumTotalCompletadasEntreFechas(inicio, fin)` — suma de totales
- `sumTotalGroupedByMetodoPagoEntreFechas(inicio, fin)` — suma agrupada por método de pago

### 4.7 pventabase-gastos

**Paquete:** `com.pventabase.gastos`

**Endpoint base:** `/api/gastos`

**Entidad `Gasto`** — Tabla `gastos`:
| Campo | Tipo | DB Column | Restricciones |
|-------|------|-----------|---------------|
| descripcion | String | descripcion | NOT NULL, max 255 |
| monto | BigDecimal | monto | NOT NULL, > 0, precision 19, scale 2 |
| fechaGasto | LocalDateTime | fecha_gasto | NOT NULL |
| categoria | String | categoria | NOT NULL, max 50 |
| metodoPago | String | metodo_pago | max 50 |
| observacion | String | observacion | max 500 |

**DTOs:** `GastoRequestDTO`, `GastoResponseDTO` (extends BaseResponseDTO)

**DetalleVentaResponseDTO** (extends BaseResponseDTO):
| Campo | Fuente |
|-------|--------|
| productoId | `Producto.id` |
| productoCodigo | `Producto.codigo` |
| productoNombre | `Producto.nombre` |
| productoDescripcion | `Producto.descripcion` |
| productoPesado | `Producto.pesado` |
| cantidad | `DetalleVenta.cantidad` |
| precioUnitario | `DetalleVenta.precioUnitario` |
| descuentoPorcentaje | `DetalleVenta.descuentoPorcentaje` |
| subtotal | `DetalleVenta.subtotal` |

**TicketResponseDTO** — generado por `VentaService.generateTicket()`:
| Campo | Descripción |
|-------|-------------|
| ventaId | ID de la venta |
| fecha | Fecha de la venta |
| atendidoPor | Email del usuario que atendió |
| cliente | Nombre del cliente o "Mostrador" |
| lineas | List<LineaTicket> con producto, descripcion, cantidad, unidad (kg/pza), precioUnitario, descuento, importe |
| subtotal | Suma de subtotales |
| descuentoPorcentaje | Descuento global de la venta |
| descuentoAplicado | Monto descontado |
| total | Total final |

**Queries adicionales en GastoRepository:**
- `sumMontoEntreFechas(inicio, fin)` — suma de montos en rango (para corte de caja)

### 4.8 pventabase-corte-caja

**Paquete:** `com.pventabase.cortecaja`

**Endpoint base:** `/api/cortes-caja`

**Dependencias:** pventabase-usuarios, pventabase-ventas, pventabase-gastos

**Enum `EstadoCorte`:** `ABIERTO`, `CERRADO`

**Entidad `CorteCaja`** — Tabla `cortes_caja`:
| Campo | Tipo | DB Column | Restricciones |
|-------|------|-----------|---------------|
| fechaApertura | LocalDateTime | fecha_apertura | NOT NULL |
| fechaCierre | LocalDateTime | fecha_cierre | |
| montoInicial | BigDecimal | monto_inicial | NOT NULL, >= 0 |
| montoFinal | BigDecimal | monto_final | |
| totalVentas | BigDecimal | total_ventas | NOT NULL, default 0 |
| totalGastos | BigDecimal | total_gastos | NOT NULL, default 0 |
| totalEfectivo | BigDecimal | total_efectivo | NOT NULL, default 0 |
| totalTarjeta | BigDecimal | total_tarjeta | NOT NULL, default 0 |
| totalTransferencia | BigDecimal | total_transferencia | NOT NULL, default 0 |
| diferencia | BigDecimal | diferencia | NOT NULL, default 0 |
| observacion | String | observacion | max 500 |
| estado | EstadoCorte (enum) | estado | NOT NULL, default ABIERTO |
| usuario | @ManyToOne(LAZY) | usuario_id | FK → usuarios |

**DTOs:** `AbrirCorteRequestDTO` (montoInicial, observacion), `CerrarCorteRequestDTO` (montoFinal, observacion), `CorteCajaResponseDTO` (extends BaseResponseDTO + usuarioEmail, usuarioNombre)

**Flujo:**
1. `POST /cortes-caja/abrir` — crea corte ABIERTO (valida que no haya otro abierto)
2. `POST /cortes-caja/{id}/cerrar` — consulta ventas COMPLETADAS y gastos entre apertura y ahora, calcula totales por método de pago, diferencia = montoFinal - (montoInicial + totalVentas - totalGastos)

### 4.9 pventabase-reportes

**Paquete:** `com.pventabase.reportes`

**Endpoint base:** `/api/reportes`

**Dependencias:** todos los módulos de dominio + Apache POI, JFreeChart, OpenPDF

**Enums:** `ReportFormat` (JSON, EXCEL, PDF, PRINT), `TipoReporte`, `TipoGrafico` (BARRA, PIE, LINEA, NONE)

**DTO común:** `ReporteFilter` — campos opcionales: fechaDesde, fechaHasta, estado, metodoPago, categoria, usuarioEmail, clienteId, limite, stockMinimo, soloActivos, fecha

**DTO salida:** `ReporteData` — titulo, columnas (List<String>), filas (List<Map<String,Object>>), tipoGrafico, graficoNombre

**Utilidades de exportación:**
- `ChartGenerator` — genera gráficos PNG con JFreeChart (barras, pastel, línea)
- `ExcelGenerator` — genera .xlsx con Apache POI (título, encabezados con estilo, bordes)
- `PdfGenerator` — genera .pdf con OpenPDF (tabla con colores, formato)
- `PrintServiceUtil` — envía PDF a la impresora predeterminada vía javax.print

**Reportes y sus gráficos:**
| Reporte | Gráfico | Filtros |
|---------|---------|---------|
| Ventas | Línea (ventas por día) | fechaDesde, fechaHasta, estado, metodoPago |
| Productos más vendidos | Barras (top productos) | fechaDesde, fechaHasta, limite |
| Gastos | Pastel (por categoría) | fechaDesde, fechaHasta, categoria |
| Stock / Inventario | Ninguno (tabla) | stockMinimo, soloActivos |
| Clientes frecuentes | Barras (top clientes) | fechaDesde, fechaHasta, limite |
| Cortes de caja | Ninguno (tabla) | fechaDesde, fechaHasta |
| Dashboard diario | Pastel (ventas por método) | fecha |

### 4.10 pventabase-app (Entry Point)

**Paquete:** `com.pventabase.app`

**Clases:**
- `PventabaseApplication` — `@SpringBootApplication(scanBasePackages = "com.pventabase")`, `@EntityScan`, `@EnableJpaRepositories`
- `SecurityConfig` — SecurityFilterChain con JWT filter, CSRF disabled, STATELESS session, CORS all origins, `.anyRequest().permitAll()` (no bloquea sin auth, pero JWT filter autentica si hay token válido)
- `GlobalExceptionHandler` — `@RestControllerAdvice` con handlers para:
  - `ResourceNotFoundException` → 404
  - `DuplicateResourceException` → 409
  - `InvalidStateException` → 400
  - `VentaNoEncontradaException` → 404
  - `StockInsuficienteException` → 400
  - `MethodArgumentNotValidException` → 400
  - `Exception` (generic) → 500
- `OpenApiConfig` — configuración SpringDoc OpenAPI con title "Pventa API", servidor localhost:8090
- `HealthController` — `GET /health` verifica DB

**Flyway Migrations** en `pventabase-app/src/main/resources/db/migration/`:
| Migration | Descripción |
|-----------|-------------|
| V1 | CREATE TABLE usuarios |
| V2 | ALTER usuarios ADD created_by, updated_by |
| V3 | CREATE TABLE producto |
| V4 | CREATE TABLE clientes |
| V5 | ALTER clientes ADD documento |
| V6 | CREATE TABLE ventas, detalle_venta; ALTER producto ADD pesado |
| V7 | ALTER producto ADD imagen_url |
| V8 | CREATE TABLE gastos |
| V9 | ALTER ventas ADD metodo_pago |
| V10 | CREATE TABLE cortes_caja |

## 5. Seguridad

- JWT con HMAC-SHA256 (jjwt 0.12.5)
- Secreto: `PventabaseClaveSecretaSegura2026ParaJWT`
- Expiración: 24h
- Claims: `sub` (email), `rol` (ROLE_ADMIN, ROLE_USER, ROLE_VENDEDOR)
- Password encoder: BCrypt
- CSRF: deshabilitado
- Sesión: STATELESS
- CORS: todos los orígenes permitidos con credentials
- Endpoints públicos: `/auth/**`
- **Nota:** `anyRequest().permitAll()` — JWT filter autentica si hay token pero no bloquea peticiones sin token

## 6. API — Resumen de Endpoints

Total: **53 endpoints**

| Módulo | Path base | Endpoints |
|--------|-----------|-----------|
| Health | `/health` | 1 |
| Auth | `/auth` | 2 |
| Usuarios | `/usuarios` | 7 |
| Productos | `/productos` | 7 |
| Clientes | `/clientes` | 8 |
| Ventas | `/ventas` | 8 |
| Gastos | `/gastos` | 8 |
| Cortes de Caja | `/cortes-caja` | 5 |
| Reportes | `/reportes` | 7 |

## 7. Configuración de Infraestructura

### application.properties
```properties
server.port=8090
server.servlet.context-path=/api
spring.datasource.url=jdbc:postgresql://localhost:5432/pventabase_db
spring.datasource.username=app_user
spring.datasource.password=app_pass
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

### Dockerfile
```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw package -DskipTests -pl pventabase-app -am
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/pventabase-app/target/*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml
```yaml
version: '3.8'
services:
  pventabase:
    build: .
    ports:
      - "8090:8090"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/pventabase_db
      - SPRING_DATASOURCE_USERNAME=app_user
      - SPRING_DATASOURCE_PASSWORD=app_pass
```
**Nota:** PostgreSQL se asume externo al contenedor.

## 8. Comandos de Desarrollo

```sh
# Compilar todo
mvn compile

# Empaquetar (sin tests)
mvn package -DskipTests -pl pventabase-app -am

# Ejecutar
mvn spring-boot:run -pl pventabase-app

# Ejecutar con variables de entorno
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/pventabase_db \
SPRING_DATASOURCE_USERNAME=app_user \
SPRING_DATASOURCE_PASSWORD=app_pass \
mvn spring-boot:run -pl pventabase-app
```

## 9. Convenciones de Código

- **Inyección:** Solo constructor injection con `@RequiredArgsConstructor`
- **Transacciones:** `@Transactional` exclusivamente en clases Service
- **Validación:** `@Valid` en DTOs de Controller
- **Layers por módulo:** `controller/` → `dto/` → `entity/` → `mapper/` → `repository/` → `service/`
- **Mappers:** MapStruct con `componentModel = "spring"`. Ignorar campos de BaseEntity (id, createdAt, updatedAt, createdBy, updatedBy, activo) en toEntity/updateEntity
- **DTOs Request:** Clases planas con `@Getter @Setter`, validaciones Jakarta
- **DTOs Response:** Extienden `BaseResponseDTO`
- **Entidades:** Extienden `BaseEntity`, `@Entity @Table(name = "...") @Getter @Setter`
- **Paginación:** `PageResponseDTO<T>` con builder
- **Método de pago:** String (no enum), valores típicos: EFECTIVO, TARJETA_DEBITO, TARJETA_CREDITO, TRANSFERENCIA
- **Mapeo Cliente:** `direccion`→`domicilio`, `telefono`→`celular`, `email`→`correo`
- **Reportes:** Los endpoints aceptan `?filter={...}` (JSON string) y `?formato=JSON|EXCEL|PDF|PRINT`

## 10. Pendientes / Mejoras Futuras

- [ ] Tests unitarios e integración
- [ ] Linting y formateo automático
- [ ] CI/CD pipeline
- [ ] Pre-commit hooks
- [ ] Bloqueo real de endpoints por rol (`hasRole()`) en SecurityConfig
- [ ] Paginación y filtros en listados de cortes de caja y gastos
- [ ] Endpoint para reimprimir tickets
- [ ] Catálogo de categorías de gastos (enum fijo vs tabla)
