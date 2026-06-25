# SPEC_BACK.md — Especificación del Proyecto pventabase (Backend)

> **Versión:** 1.0-SNAPSHOT
> **Fecha:** 19 Junio 2026
> **Propósito:** Documento de especificación y definición del backend de pventabase.

---

## 1. DEFINICIÓN DEL PROYECTO

**pventabase** es una API REST para un sistema de punto de venta (POS). Su propósito es gestionar usuarios, productos, clientes y autenticación mediante JWT.

### Stack Tecnológico

| Componente | Especificación |
|------------|---------------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Build | Maven 3.9+ (multi-módulo) |
| Base de Datos | PostgreSQL 14+ |
| ORM | Spring Data JPA / Hibernate |
| Migraciones | Flyway |
| Seguridad | Spring Security + JWT (jjwt 0.12.5) |
| Documentación API | Springdoc OpenAPI 2.3.0 |
| Mapeo DTO | MapStruct 1.5.5.Final |
| Reducción código | Lombok 1.18.30 |
| Testing | Testcontainers 1.19.8, H2 |
| Validación | Jakarta Validation |
| Serialización | Jackson |

---

## 2. ARQUITECTURA

### 2.1. Estructura Multi-Módulo

```
pventabase (Parent POM)
├── pventabase-common       → Shared Kernel (entidades base, excepciones, DTOs, constantes)
├── pventabase-usuarios     → Dominio Usuarios (CRUD + roles)
├── pventabase-login        → Autenticación JWT (login/register)
├── pventabase-inventario   → Dominio Productos (CRUD inventario)
├── pventabase-clientes     → Dominio Clientes (CRUD + borrado lógico)
└── pventabase-app          → Entry point, config global, seguridad, Flyway, Swagger
```

### 2.2. Capas por Módulo

Cada módulo de dominio sigue la misma estructura:

```
src/main/java/com/pventabase/<modulo>/
├── controller/     → REST endpoints (@RestController)
├── dto/            → Data Transfer Objects (Request/Response)
├── entity/         → Entidades JPA (heredan de BaseEntity)
├── mapper/         → MapStruct mappers (componentModel="spring")
├── repository/     → Spring Data JPA repositorios
└── service/        → Lógica de negocio (@Transactional)
```

### 2.3. Principios Arquitectónicos

- **Inyección de dependencias:** Solo por constructor (`@RequiredArgsConstructor`)
- **Transacciones:** `@Transactional` exclusivamente en Services
- **Validación:** `@Valid` en DTOs de Controller
- **Responsabilidad única:** Cada módulo maneja un dominio de negocio específico
- **Acoplamiento débil:** Los módulos dependen solo de `pventabase-common`
- **Excepción:** `pventabase-login` depende también de `pventabase-usuarios` (reutiliza `UsuarioRepository` y `RolUsuario`)

---

## 3. MODELO DE DATOS

### 3.1. Entidad Base (BaseEntity)

Todas las entidades heredan de `BaseEntity` que provee:

| Campo | Tipo | BD | Descripción |
|-------|------|-----|-------------|
| id | Long | `id` BIGSERIAL PK | Identificador único |
| createdAt | LocalDateTime | `created_at` | Fecha de creación (autogenerada) |
| updatedAt | LocalDateTime | `updated_at` | Fecha de modificación (autogenerada) |
| createdBy | String | `created_by` | Usuario que creó |
| updatedBy | String | `updated_by` | Usuario que actualizó |
| activo | Boolean | `activo` DEFAULT TRUE | Estado activo/inactivo |

### 3.2. Tabla: usuarios

Representa los usuarios del sistema con roles.

| Campo Java | Columna BD | Tipo BD | Restricciones |
|------------|------------|---------|---------------|
| id | id | BIGSERIAL | PK |
| nombre | nombre | VARCHAR(100) | NOT NULL |
| apellido | apellido | VARCHAR(100) | NOT NULL |
| email | email | VARCHAR(150) | NOT NULL, UNIQUE |
| password | password | VARCHAR(255) | NOT NULL |
| rol | rol | VARCHAR(20) | NOT NULL, DEFAULT 'ROLE_USER' |
| telefono | telefono | VARCHAR(20) | |
| activo | activo | BOOLEAN | NOT NULL, DEFAULT TRUE |
| createdAt | created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() |
| updatedAt | updated_at | TIMESTAMP | |
| createdBy | created_by | VARCHAR(255) | |
| updatedBy | updated_by | VARCHAR(255) | |

**Roles disponibles:**
- `ROLE_ADMIN` — Administrador
- `ROLE_USER` — Usuario estándar
- `ROLE_VENDEDOR` — Vendedor

### 3.3. Tabla: producto

Representa los productos del inventario.

| Campo Java | Columna BD | Tipo BD | Restricciones |
|------------|------------|---------|---------------|
| id | id | BIGSERIAL | PK |
| codigo | codigo | VARCHAR(50) | NOT NULL, UNIQUE |
| nombre | nombre | VARCHAR(150) | NOT NULL |
| descripcion | descripcion | TEXT | |
| precioCompra | precio_compra | NUMERIC(19,2) | NOT NULL, CHECK >= 0 |
| precioVenta | precio_venta | NUMERIC(19,2) | NOT NULL, CHECK >= 0 |
| existencia | existencia | INTEGER | NOT NULL, DEFAULT 0, CHECK >= 0 |
| activo | activo | BOOLEAN | NOT NULL, DEFAULT TRUE |
| createdAt | fecha_creacion | TIMESTAMP | NOT NULL, DEFAULT NOW() |
| updatedAt | fecha_modificacion | TIMESTAMP | NOT NULL, DEFAULT NOW() |
| createdBy | created_by | VARCHAR(255) | |
| updatedBy | updated_by | VARCHAR(255) | |

### 3.4. Tabla: clientes

Representa los clientes del sistema. Soporta borrado lógico.

| Campo Java | Columna BD | Tipo BD | Restricciones |
|------------|------------|---------|---------------|
| id | id | BIGSERIAL | PK |
| nombre | nombre | VARCHAR(100) | NOT NULL |
| apellido | apellido | VARCHAR(100) | NOT NULL |
| direccion | domicilio | VARCHAR(255) | NOT NULL |
| telefono | celular | VARCHAR(20) | |
| email | correo | VARCHAR(150) | NOT NULL, UNIQUE |
| credito | credito | NUMERIC(10,2) | |
| activo | activo | BOOLEAN | NOT NULL, DEFAULT TRUE |
| eliminado | eliminado | BOOLEAN | NOT NULL, DEFAULT FALSE |
| documento | documento | VARCHAR(20) | |
| createdAt | created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() |
| updatedAt | updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() |
| createdBy | created_by | VARCHAR(255) | |
| updatedBy | updated_by | VARCHAR(255) | |

**Nota de mapeo:** La entidad `Cliente` usa `@Column(name = ...)` para mapear nombres del frontend (`direccion`, `telefono`, `email`) a columnas existentes en BD (`domicilio`, `celular`, `correo`).

### 3.5. Migraciones Flyway (orden de aplicación)

| Migración | Descripción |
|-----------|-------------|
| V1 | Crear tabla `usuarios` con índices |
| V2 | Agregar columnas `created_by` y `updated_by` a `usuarios` |
| V3 | Crear tabla `producto` con constraints CHECK |
| V4 | Crear tabla `clientes` con borrado lógico |
| V5 | Agregar columna `documento` a `clientes` |

---

## 4. API REST — ESPECIFICACIÓN DE ENDPOINTS

### 4.1. Autenticación

**Base:** `/auth` (público)

#### POST /auth/login
- **Body:** `LoginRequestDTO` (email, password)
- **Response:** `LoginResponseDTO` (token, tipo, email, nombre, apellido, rol)
- **Errores:** 404 (usuario no encontrado), 400 (desactivado o credenciales inválidas)
- **Comportamiento:** Verifica email + password con BCrypt, genera token JWT

#### POST /auth/register
- **Body:** `RegisterRequestDTO` (nombre, apellido, email, password, rol, telefono)
- **Response:** `LoginResponseDTO` (token + datos usuario)
- **Errores:** 409 (email duplicado)
- **Comportamiento:** Crea usuario con password encriptado, genera token JWT

### 4.2. Usuarios

**Base:** `/usuarios` (autenticado)

| Método | Endpoint | Comportamiento |
|--------|----------|----------------|
| GET | `/usuarios` | Lista paginada (page, size, sortBy, sortDir) |
| GET | `/usuarios/{id}` | Buscar por ID (404 si no existe) |
| GET | `/usuarios/email/{email}` | Buscar por email (404 si no existe) |
| POST | `/usuarios` | Crear (409 si email duplicado) |
| PUT | `/usuarios/{id}` | Actualizar (ignora password, 409 si email duplicado por otro) |
| DELETE | `/usuarios/{id}` | Eliminar físicamente (404 si no existe) |
| PATCH | `/usuarios/{id}/toggle-activo` | Invertir estado `activo` |

### 4.3. Productos

**Base:** `/productos` (autenticado)

| Método | Endpoint | Comportamiento |
|--------|----------|----------------|
| GET | `/productos` | Lista paginada |
| GET | `/productos/{id}` | Buscar por ID |
| GET | `/productos/codigo/{codigo}` | Buscar por código único |
| POST | `/productos` | Crear (409 si código duplicado) |
| PUT | `/productos/{id}` | Actualizar |
| DELETE | `/productos/{id}` | Eliminar físicamente |
| PATCH | `/productos/{id}/toggle-activo` | Invertir estado `activo` |

### 4.4. Clientes

**Base:** `/clientes` (autenticado)

| Método | Endpoint | Comportamiento |
|--------|----------|----------------|
| GET | `/clientes` | Lista paginada |
| GET | `/clientes/{id}` | Buscar por ID |
| GET | `/clientes/email/{email}` | Buscar por email |
| POST | `/clientes` | Crear (409 si email duplicado) |
| PUT | `/clientes/{id}` | Actualizar |
| DELETE | `/clientes/{id}` | Eliminar físicamente |
| PATCH | `/clientes/{id}/toggle-activo` | Invertir estado `activo` |
| PATCH | `/clientes/{id}/marcar-eliminado` | Marcar `eliminado = true` (borrado lógico) |

### 4.5. Formato de Respuestas

#### Éxito (paginado)
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 50,
  "totalPages": 5,
  "last": false,
  "first": true,
  "empty": false
}
```

#### Éxito (individual)
```json
{
  "id": 1,
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan@email.com",
  "rol": "ROLE_USER",
  "telefono": "123456789",
  "activo": true,
  "createdAt": "2026-06-19T10:00:00",
  "updatedAt": "2026-06-19T10:00:00",
  "createdBy": null,
  "updatedBy": null
}
```
*(Los DTOs de respuesta extienden `BaseResponseDTO`, heredando id, activo, createdAt, updatedAt, createdBy, updatedBy)*

#### Error
```json
{
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Usuario no encontrado con id: 999",
  "status": 404,
  "timestamp": "2026-06-19T10:00:00",
  "details": null
}
```

**Códigos de error:**
| errorCode | HTTP | Descripción |
|-----------|------|-------------|
| `RESOURCE_NOT_FOUND` | 404 | Recurso no encontrado (por ID o campo único) |
| `DUPLICATE_RESOURCE` | 409 | Recurso duplicado (email, código) |
| `INVALID_STATE` | 400 | Estado inválido (usuario desactivado, credenciales incorrectas) |
| `VALIDATION_ERROR` | 400 | Error de validación de campos (Jakarta Validation) |
| `INTERNAL_ERROR` | 500 | Error interno del servidor |

---

## 5. SEGURIDAD

### 5.1. Autenticación JWT

- **Algoritmo:** HMAC-SHA256
- **Clave:** `jwt.secret` (configurable en `application.properties`)
- **Expiración:** 24 horas (`jwt.expiration=86400000`)
- **Claims:** `sub` = email, `rol` = nombre del rol (ej: ROLE_ADMIN)
- **Header:** `Authorization: Bearer <token>`

### 5.2. Filtro JWT

`JwtAuthenticationFilter` (OncePerRequestFilter):
1. Extrae token del header `Authorization`
2. Valida el token (firma + expiración)
3. Si es válido, carga `SecurityContext` con `UsernamePasswordAuthenticationToken(email, null, [rol])`
4. Si no hay token o es inválido, la request continúa sin autenticación

### 5.3. CORS

- `allowedOriginPatterns: *` (cualquier origen)
- `allowCredentials: true`
- Métodos: GET, POST, PUT, DELETE, PATCH, OPTIONS
- Headers: Authorization, Content-Type, Accept, Origin, X-Requested-With

### 5.4. CSRF

Deshabilitado (API stateless con JWT).

### 5.5. Sesión

`SessionCreationPolicy.STATELESS` — no se crean sesiones HTTP.

**Nota:** Actualmente `.anyRequest().permitAll()` permite acceso sin token. El filtro JWT se ejecuta y autentica si hay token válido, pero no bloquea requests sin token.

---

## 6. MAPEO DTO-ENTITY (MapStruct)

### 6.1. Reglas de mapeo (`toEntity` y `updateEntity`)

Los mappers ignoran estos campos para evitar que valores `null` del frontend sobreescriban los defaults de `BaseEntity`:

| Campo ignorado | Motivo |
|----------------|--------|
| id | Generado automáticamente por BD |
| createdAt | Generado por @PrePersist en BaseEntity |
| updatedAt | Generado por @PrePersist/@PreUpdate |
| createdBy | Auditoría (se asigna manualmente si aplica) |
| updatedBy | Auditoría |
| activo | Default `true` en BaseEntity |

**Adicionales por dominio:**
- `UsuarioMapper.updateEntity`: también ignora `password` (no se actualiza por update genérico)
- `ClienteMapper.toEntity` y `updateEntity`: también ignoran `eliminado` (default `false`)

### 6.2. Configuración del compilador

El `maven-compiler-plugin` usa estos annotation processors en orden:
1. `lombok` 1.18.30
2. `mapstruct-processor` 1.5.5.Final
3. `lombok-mapstruct-binding` 0.2.0 (evita warnings de orden)

---

## 7. CONFIGURACIÓN

### 7.1. Server

| Propiedad | Valor |
|-----------|-------|
| Puerto | 8090 |
| Dirección | 0.0.0.0 (accesible desde la red) |
| Context path | `/api` |

### 7.2. Base de Datos

| Propiedad | Valor |
|-----------|-------|
| URL | `jdbc:postgresql://localhost:5432/pventai_db` |
| Usuario | `miguel` |
| Password | `Gallego` |
| DDL auto | `validate` (Flyway gestiona el schema) |
| Open-in-view | `false` |

### 7.3. JWT

| Propiedad | Valor |
|-----------|-------|
| Secret | `PventabaseClaveSecretaSegura2026ParaJWT` |
| Expiración | 86400000ms (24h) |

### 7.4. Springdoc

| Propiedad | Valor |
|-----------|-------|
| Paths a documentar | `/usuarios/**,/auth/**,/productos/**,/clientes/**` |
| API docs path | `/v3/api-docs` |
| Swagger UI path | `/swagger-ui.html` |
| Orden operaciones | Por método HTTP |

### 7.5. Jackson

| Propiedad | Valor |
|-----------|-------|
| Formato fecha | `yyyy-MM-dd'T'HH:mm:ss` |
| Timezone | `America/Argentina/Buenos_Aires` |
| Write dates as timestamps | `false` |

---

## 8. DOCKER

### 8.1. Dockerfile (multi-stage)

- **Stage 1 (builder):** `maven:3.9-eclipse-temurin-17-alpine`
  - Copia todos los `pom.xml` primero para cachear dependencias
  - Ejecuta `mvn dependency:go-offline`
  - Copia el source de todos los módulos
  - Ejecuta `mvn package -DskipTests -pl pventabase-app -am`
- **Stage 2 (runtime):** `eclipse-temurin:17-jre-alpine`
  - Crea usuario no-root `appuser`
  - Copia el JAR resultando
  - Expone puerto 8080
  - EntryPoint: `java -jar app.jar`

### 8.2. docker-compose.yml

```yaml
services:
  app:
    image: pventabase:1.0
    build: .
    ports:
      - "8090:8080"
    extra_hosts:
      - "host.docker.internal:host-gateway"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/pventabase_db
      - SPRING_DATASOURCE_USERNAME=miguel
      - SPRING_DATASOURCE_PASSWORD=Gallego
```

**Nota:** docker-compose no incluye base de datos PostgreSQL; asume una instancia externa accesible via `host.docker.internal`.

---

## 9. PRÓXIMOS MÓDULOS PENDIENTES

| Módulo | Descripción |
|--------|-------------|
| `pventabase-proveedores` | Gestión de proveedores y compras |
| `pventabase-ventas` | Gestión de ventas y facturación |
| `pventabase-creditos` | Gestión de créditos y cobranzas |

---

> **Fin de SPEC_BACK.md**
> Documento de especificación del backend pventabase.
