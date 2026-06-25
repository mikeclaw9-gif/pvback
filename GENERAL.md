# GENERAL.md - Documentación Completa del Proyecto pventabase

> **Fecha:** 19 Junio 2026
> **Propósito:** Documento maestro para recrear el proyecto pventabase desde cero en su estado actual.

---

## 1. VISIÓN GENERAL

**pventabase** es una API REST de punto de venta (POS) construida con **Spring Boot 3.2.5**, **Java 17**, **Maven multi-módulo**, **PostgreSQL**, **Spring Security con JWT**, y documentada con **Springdoc OpenAPI**. Sigue una arquitectura hexagonal simplificada con módulos independientes por dominio de negocio.

### Stack Tecnológico Completo

| Componente | Tecnología |
|------------|-----------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Build | Maven (multi-módulo) |
| BD Producción | PostgreSQL |
| BD Testing | H2 (in-memory) |
| ORM | Spring Data JPA / Hibernate |
| Migraciones | Flyway |
| Seguridad | Spring Security + JWT (jjwt 0.12.5) |
| Documentación API | Springdoc OpenAPI 2.3.0 |
| Mapeo DTO | MapStruct 1.5.5.Final |
| Reducción código | Lombok 1.18.30 |
| Testing | Testcontainers 1.19.8 |
| Validación | Jakarta Validation |
| JSON | Jackson |

### Estructura de Módulos

```
pventabase/                         # Parent POM (pom.xml)
├── pventabase-common/              # Shared Kernel (entidad base, excepciones, DTOs, constantes)
├── pventabase-usuarios/            # Dominio Usuarios (CRUD + roles)
├── pventabase-login/               # Autenticación JWT (login/register)
├── pventabase-inventario/          # Dominio Productos (CRUD inventario)
├── pventabase-clientes/            # Dominio Clientes (CRUD + borrado lógico)
└── pventabase-app/                 # Entry point, config global, seguridad, Flyway, Swagger
```

### Puertos y URLs

| Recurso | URL |
|---------|-----|
| API Base | `http://localhost:8090/api` |
| Swagger UI | `http://localhost:8090/api/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8090/api/v3/api-docs` |
| BD PostgreSQL | `localhost:5432/pventai_db` (usuario: `miguel`, pass: `Gallego`) |

---

## 2. REQUISITOS PREVIOS PARA RECREAR

- **JDK 17** (Eclipse Temurin o similar)
- **Maven 3.9+**
- **PostgreSQL 14+** con base de datos `pventai_db` creada
- Usuario PostgreSQL `miguel` con password `Gallego` y permisos en `pventai_db`

---

## 3. PASO A PASO PARA RECREAR DESDE CERO

### 3.1. Crear la estructura Maven multi-módulo

```
pventabase/
├── pom.xml                    # Parent POM (gestión de dependencias)
├── pventabase-common/
│   ├── pom.xml
│   └── src/main/java/com/pventabase/common/
├── pventabase-usuarios/
│   ├── pom.xml
│   └── src/main/java/com/pventabase/usuarios/
├── pventabase-login/
│   ├── pom.xml
│   └── src/main/java/com/pventabase/login/
├── pventabase-inventario/
│   ├── pom.xml
│   └── src/main/java/com/pventabase/inventario/
├── pventabase-clientes/
│   ├── pom.xml
│   └── src/main/java/com/pventabase/clientes/
├── pventabase-app/
│   ├── pom.xml
│   └── src/main/java/com/pventabase/app/
└── docker-compose.yml
```

### 3.2. Parent POM (pventabase/pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <groupId>com.pventabase</groupId>
    <artifactId>pventabase</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>pventabase</name>
    <description>Point of Sale API</description>

    <modules>
        <module>pventabase-common</module>
        <module>pventabase-usuarios</module>
        <module>pventabase-app</module>
        <module>pventabase-login</module>
        <module>pventabase-inventario</module>
        <module>pventabase-clientes</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>${java.version}</maven.compiler.source>
        <maven.compiler.target>${java.version}</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <lombok.version>1.18.30</lombok.version>
        <mapstruct.version>1.5.5.Final</mapstruct.version>
        <springdoc.version>2.3.0</springdoc.version>
        <testcontainers.version>1.19.8</testcontainers.version>
        <jjwt.version>0.12.5</jjwt.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
                <scope>provided</scope>
            </dependency>
            <dependency>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct</artifactId>
                <version>${mapstruct.version}</version>
            </dependency>
            <dependency>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>${mapstruct.version}</version>
                <scope>provided</scope>
            </dependency>
            <dependency>
                <groupId>org.springdoc</groupId>
                <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
                <version>${springdoc.version}</version>
            </dependency>
            <dependency>
                <groupId>org.testcontainers</groupId>
                <artifactId>testcontainers-bom</artifactId>
                <version>${testcontainers.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.pventabase</groupId>
                <artifactId>pventabase-common</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.pventabase</groupId>
                <artifactId>pventabase-usuarios</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.pventabase</groupId>
                <artifactId>pventabase-login</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.pventabase</groupId>
                <artifactId>pventabase-inventario</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.pventabase</groupId>
                <artifactId>pventabase-clientes</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-api</artifactId>
                <version>${jjwt.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-impl</artifactId>
                <version>${jjwt.version}</version>
                <scope>runtime</scope>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-jackson</artifactId>
                <version>${jjwt.version}</version>
                <scope>runtime</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <configuration>
                        <source>${java.version}</source>
                        <target>${java.version}</target>
                        <annotationProcessorPaths>
                            <path>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok</artifactId>
                                <version>${lombok.version}</version>
                            </path>
                            <path>
                                <groupId>org.mapstruct</groupId>
                                <artifactId>mapstruct-processor</artifactId>
                                <version>${mapstruct.version}</version>
                            </path>
                            <path>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok-mapstruct-binding</artifactId>
                                <version>0.2.0</version>
                            </path>
                        </annotationProcessorPaths>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

### 3.3. .gitignore

```
target/
*.class
*.jar
*.war
*.log
.idea/
*.iml
.vscode/
.settings/
.project
.classpath
.DS_Store
*.swp
*~
application-local.properties
application-dev.properties
.env
```

---

## 4. MÓDULO pventabase-common (Shared Kernel)

### 4.1. pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.pventabase</groupId>
        <artifactId>pventabase</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <artifactId>pventabase-common</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
        </dependency>
    </dependencies>
</project>
```

### 4.2. BaseEntity.java

```java
package com.pventabase.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(nullable = false)
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

### 4.3. Excepciones

#### BusinessException.java
```java
package com.pventabase.common.exception;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final String errorCode;
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
```

#### ResourceNotFoundException.java
```java
package com.pventabase.common.exception;
import com.pventabase.common.constants.ErrorCodes;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, Object id) {
        super(ErrorCodes.RESOURCE_NOT_FOUND, resource + " no encontrado con id: " + id);
    }
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(ErrorCodes.RESOURCE_NOT_FOUND, resource + " no encontrado con " + field + ": " + value);
    }
}
```

#### DuplicateResourceException.java
```java
package com.pventabase.common.exception;
import com.pventabase.common.constants.ErrorCodes;

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String resource, String field, Object value) {
        super(ErrorCodes.DUPLICATE_RESOURCE, resource + " ya existe con " + field + ": " + value);
    }
}
```

#### InvalidStateException.java
```java
package com.pventabase.common.exception;
import com.pventabase.common.constants.ErrorCodes;

public class InvalidStateException extends BusinessException {
    public InvalidStateException(String message) {
        super(ErrorCodes.INVALID_STATE, message);
    }
}
```

### 4.4. DTOs Base

#### BaseResponseDTO.java
```java
package com.pventabase.common.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public abstract class BaseResponseDTO {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Boolean activo;
}
```

#### PageResponseDTO.java
```java
package com.pventabase.common.dto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PageResponseDTO<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private boolean empty;
}
```

### 4.5. Constantes

#### AppConstants.java
```java
package com.pventabase.common.constants;

public final class AppConstants {
    private AppConstants() {}

    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIR = "asc";

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_BEARER = "Bearer ";

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_VENDEDOR = "ROLE_VENDEDOR";
}
```

#### ErrorCodes.java
```java
package com.pventabase.common.constants;

public final class ErrorCodes {
    private ErrorCodes() {}

    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String DUPLICATE_RESOURCE = "DUPLICATE_RESOURCE";
    public static final String INVALID_STATE = "INVALID_STATE";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
}
```

---

## 5. MÓDULO pventabase-usuarios

### 5.1. pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.pventabase</groupId>
        <artifactId>pventabase</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <artifactId>pventabase-usuarios</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>com.pventabase</groupId>
            <artifactId>pventabase-common</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
        </dependency>
    </dependencies>
</project>
```

### 5.2. RolUsuario.java (Enum)

```java
package com.pventabase.usuarios.entity;

public enum RolUsuario {
    ROLE_ADMIN,
    ROLE_USER,
    ROLE_VENDEDOR
}
```

### 5.3. Usuario.java (Entity)

```java
package com.pventabase.usuarios.entity;

import com.pventabase.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario extends BaseEntity {

    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String apellido;

    @NotBlank @Email @Size(max = 150)
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol = RolUsuario.ROLE_USER;

    @Size(max = 20) @Column(length = 20)
    private String telefono;
}
```

### 5.4. DTOs

#### UsuarioRequestDTO.java
```java
package com.pventabase.usuarios.dto;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UsuarioRequestDTO {
    @NotBlank @Size(max = 100) private String nombre;
    @NotBlank @Size(max = 100) private String apellido;
    @NotBlank @Email @Size(max = 150) private String email;
    @NotBlank @Size(max = 255) private String password;
    private RolUsuario rol;
    @Size(max = 20) private String telefono;
}
```

#### UsuarioResponseDTO.java
```java
package com.pventabase.usuarios.dto;
import com.pventabase.common.dto.BaseResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UsuarioResponseDTO extends BaseResponseDTO {
    private String nombre;
    private String apellido;
    private String email;
    private RolUsuario rol;
    private String telefono;
}
```

### 5.5. UsuarioMapper.java

```java
package com.pventabase.usuarios.mapper;

import com.pventabase.usuarios.dto.UsuarioRequestDTO;
import com.pventabase.usuarios.dto.UsuarioResponseDTO;
import com.pventabase.usuarios.entity.Usuario;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponseDTO toResponseDTO(Usuario usuario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Usuario toEntity(UsuarioRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntity(UsuarioRequestDTO requestDTO, @MappingTarget Usuario usuario);
}
```

### 5.6. UsuarioRepository.java

```java
package com.pventabase.usuarios.repository;

import com.pventabase.usuarios.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

### 5.7. UsuarioService.java

```java
package com.pventabase.usuarios.service;

import com.pventabase.common.dto.PageResponseDTO;
import com.pventabase.common.exception.*;
import com.pventabase.usuarios.dto.*;
import com.pventabase.usuarios.entity.Usuario;
import com.pventabase.usuarios.mapper.UsuarioMapper;
import com.pventabase.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Transactional(readOnly = true)
    public PageResponseDTO<UsuarioResponseDTO> findAll(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return buildPageResponse(usuarioRepository.findAll(pageable));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO findById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        return usuarioMapper.toResponseDTO(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO findByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));
        return usuarioMapper.toResponseDTO(usuario);
    }

    public UsuarioResponseDTO create(UsuarioRequestDTO requestDTO) {
        if (usuarioRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Usuario", "email", requestDTO.getEmail());
        }
        Usuario usuario = usuarioMapper.toEntity(requestDTO);
        usuario = usuarioRepository.save(usuario);
        return usuarioMapper.toResponseDTO(usuario);
    }

    public UsuarioResponseDTO update(Long id, UsuarioRequestDTO requestDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        if (!usuario.getEmail().equals(requestDTO.getEmail())
                && usuarioRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Usuario", "email", requestDTO.getEmail());
        }
        usuarioMapper.updateEntity(requestDTO, usuario);
        usuario = usuarioRepository.save(usuario);
        return usuarioMapper.toResponseDTO(usuario);
    }

    public void delete(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", id);
        }
        usuarioRepository.deleteById(id);
    }

    public UsuarioResponseDTO toggleActivo(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        usuario.setActivo(!usuario.getActivo());
        usuario = usuarioRepository.save(usuario);
        return usuarioMapper.toResponseDTO(usuario);
    }

    private PageResponseDTO<UsuarioResponseDTO> buildPageResponse(Page<Usuario> page) {
        return PageResponseDTO.<UsuarioResponseDTO>builder()
                .content(page.getContent().stream().map(usuarioMapper::toResponseDTO).toList())
                .page(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .last(page.isLast()).first(page.isFirst()).empty(page.isEmpty())
                .build();
    }
}
```

### 5.8. UsuarioController.java

```java
package com.pventabase.usuarios.controller;

import com.pventabase.common.constants.AppConstants;
import com.pventabase.common.dto.PageResponseDTO;
import com.pventabase.usuarios.dto.*;
import com.pventabase.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<UsuarioResponseDTO>> findAll(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir) {
        return ResponseEntity.ok(usuarioService.findAll(page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioResponseDTO> findByEmail(@PathVariable String email) {
        return ResponseEntity.ok(usuarioService.findByEmail(email));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> create(@Valid @RequestBody UsuarioRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.create(requestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> update(@PathVariable Long id,
                                                      @Valid @RequestBody UsuarioRequestDTO requestDTO) {
        return ResponseEntity.ok(usuarioService.update(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<UsuarioResponseDTO> toggleActivo(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.toggleActivo(id));
    }
}
```

---

## 6. MÓDULO pventabase-login

### 6.1. pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project ...>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.pventabase</groupId>
        <artifactId>pventabase</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <artifactId>pventabase-login</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>com.pventabase</groupId>
            <artifactId>pventabase-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.pventabase</groupId>
            <artifactId>pventabase-usuarios</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
</project>
```

### 6.2. JwtTokenProvider.java

```java
package com.pventabase.login.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration}") long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(String email, String rol) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRolFromToken(String token) {
        return parseClaims(token).get("rol", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

### 6.3. JwtAuthenticationFilter.java

```java
package com.pventabase.login.security;

import com.pventabase.common.constants.AppConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmailFromToken(token);
            String rol = jwtTokenProvider.getRolFromToken(token);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email, null, List.of(new SimpleGrantedAuthority(rol)));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AppConstants.HEADER_AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AppConstants.HEADER_BEARER)) {
            return bearerToken.substring(AppConstants.HEADER_BEARER.length());
        }
        return null;
    }
}
```

### 6.4. DTOs de Login

#### LoginRequestDTO.java
```java
package com.pventabase.login.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequestDTO {
    @NotBlank @Email private String email;
    @NotBlank private String password;
}
```

#### LoginResponseDTO.java
```java
package com.pventabase.login.dto;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class LoginResponseDTO {
    private String token;
    private String tipo;
    private String email;
    private String nombre;
    private String apellido;
    private String rol;
}
```

#### RegisterRequestDTO.java
```java
package com.pventabase.login.dto;
import com.pventabase.usuarios.entity.RolUsuario;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequestDTO {
    @NotBlank @Size(max = 100) private String nombre;
    @NotBlank @Size(max = 100) private String apellido;
    @NotBlank @Email @Size(max = 150) private String email;
    @NotBlank @Size(max = 255) private String password;
    private RolUsuario rol;
    @Size(max = 20) private String telefono;
}
```

### 6.5. AuthService.java

```java
package com.pventabase.login.service;

import com.pventabase.common.exception.*;
import com.pventabase.login.dto.*;
import com.pventabase.login.security.JwtTokenProvider;
import com.pventabase.usuarios.entity.Usuario;
import com.pventabase.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO requestDTO) {
        Usuario usuario = usuarioRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", requestDTO.getEmail()));
        if (!usuario.getActivo()) {
            throw new InvalidStateException("Usuario desactivado");
        }
        if (!passwordEncoder.matches(requestDTO.getPassword(), usuario.getPassword())) {
            throw new InvalidStateException("Credenciales invalidas");
        }
        String token = jwtTokenProvider.generateToken(usuario.getEmail(), usuario.getRol().name());
        return LoginResponseDTO.builder()
                .token(token).tipo("Bearer")
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .rol(usuario.getRol().name())
                .build();
    }

    public LoginResponseDTO register(RegisterRequestDTO requestDTO) {
        if (usuarioRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Usuario", "email", requestDTO.getEmail());
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(requestDTO.getNombre());
        usuario.setApellido(requestDTO.getApellido());
        usuario.setEmail(requestDTO.getEmail());
        usuario.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        usuario.setRol(requestDTO.getRol() != null ? requestDTO.getRol() : RolUsuario.ROLE_USER);
        usuario.setTelefono(requestDTO.getTelefono());
        usuario = usuarioRepository.save(usuario);
        String token = jwtTokenProvider.generateToken(usuario.getEmail(), usuario.getRol().name());
        return LoginResponseDTO.builder()
                .token(token).tipo("Bearer")
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .rol(usuario.getRol().name())
                .build();
    }
}
```

### 6.6. AuthController.java

```java
package com.pventabase.login.controller;

import com.pventabase.login.dto.*;
import com.pventabase.login.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        return ResponseEntity.ok(authService.login(requestDTO));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody RegisterRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(requestDTO));
    }
}
```

---

## 7. MÓDULO pventabase-inventario

### 7.1. pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project ...>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.pventabase</groupId>
        <artifactId>pventabase</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <artifactId>pventabase-inventario</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>com.pventabase</groupId>
            <artifactId>pventabase-common</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
        </dependency>
    </dependencies>
</project>
```

### 7.2. Producto.java (Entity)

```java
package com.pventabase.inventario.entity;

import com.pventabase.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "producto")
@Getter @Setter
@AttributeOverrides({
    @AttributeOverride(name = "createdAt", column = @Column(name = "fecha_creacion")),
    @AttributeOverride(name = "updatedAt", column = @Column(name = "fecha_modificacion"))
})
public class Producto extends BaseEntity {

    @NotBlank @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @NotBlank @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotNull @DecimalMin("0.00")
    @Column(name = "precio_compra", nullable = false, precision = 19, scale = 2)
    private BigDecimal precioCompra;

    @NotNull @DecimalMin("0.00")
    @Column(name = "precio_venta", nullable = false, precision = 19, scale = 2)
    private BigDecimal precioVenta;

    @Min(0) @Column(nullable = false)
    private Integer existencia = 0;
}
```

### 7.3. DTOs Inventario

#### ProductoRequestDTO.java
```java
package com.pventabase.inventario.dto;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class ProductoRequestDTO {
    @NotBlank @Size(min = 1, max = 50) private String codigo;
    @NotBlank @Size(min = 1, max = 150) private String nombre;
    private String descripcion;
    @NotNull @DecimalMin("0.00") private BigDecimal precioCompra;
    @NotNull @DecimalMin("0.00") private BigDecimal precioVenta;
    @Min(0) private Integer existencia = 0;
}
```

#### ProductoResponseDTO.java
```java
package com.pventabase.inventario.dto;
import com.pventabase.common.dto.BaseResponseDTO;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class ProductoResponseDTO extends BaseResponseDTO {
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer existencia;
}
```

### 7.4. ProductoMapper.java

```java
package com.pventabase.inventario.mapper;

import com.pventabase.inventario.dto.ProductoRequestDTO;
import com.pventabase.inventario.dto.ProductoResponseDTO;
import com.pventabase.inventario.entity.Producto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    ProductoResponseDTO toResponseDTO(Producto producto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Producto toEntity(ProductoRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "activo", ignore = true)
    void updateEntity(ProductoRequestDTO requestDTO, @MappingTarget Producto producto);
}
```

### 7.5. ProductoRepository.java

```java
package com.pventabase.inventario.repository;

import com.pventabase.inventario.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
```

### 7.6. ProductoService.java

```java
package com.pventabase.inventario.service;

import com.pventabase.common.dto.PageResponseDTO;
import com.pventabase.common.exception.*;
import com.pventabase.inventario.dto.*;
import com.pventabase.inventario.entity.Producto;
import com.pventabase.inventario.mapper.ProductoMapper;
import com.pventabase.inventario.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    @Transactional(readOnly = true)
    public PageResponseDTO<ProductoResponseDTO> findAll(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return buildPageResponse(productoRepository.findAll(PageRequest.of(page, size, sort)));
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO findById(Long id) {
        return productoMapper.toResponseDTO(productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id)));
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO findByCodigo(String codigo) {
        return productoMapper.toResponseDTO(productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "codigo", codigo)));
    }

    public ProductoResponseDTO create(ProductoRequestDTO requestDTO) {
        if (productoRepository.existsByCodigo(requestDTO.getCodigo()))
            throw new DuplicateResourceException("Producto", "codigo", requestDTO.getCodigo());
        return productoMapper.toResponseDTO(productoRepository.save(productoMapper.toEntity(requestDTO)));
    }

    public ProductoResponseDTO update(Long id, ProductoRequestDTO requestDTO) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
        if (!producto.getCodigo().equals(requestDTO.getCodigo())
                && productoRepository.existsByCodigo(requestDTO.getCodigo()))
            throw new DuplicateResourceException("Producto", "codigo", requestDTO.getCodigo());
        productoMapper.updateEntity(requestDTO, producto);
        return productoMapper.toResponseDTO(productoRepository.save(producto));
    }

    public void delete(Long id) {
        if (!productoRepository.existsById(id))
            throw new ResourceNotFoundException("Producto", id);
        productoRepository.deleteById(id);
    }

    public ProductoResponseDTO toggleActivo(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
        producto.setActivo(!producto.getActivo());
        return productoMapper.toResponseDTO(productoRepository.save(producto));
    }

    private PageResponseDTO<ProductoResponseDTO> buildPageResponse(Page<Producto> page) {
        return PageResponseDTO.<ProductoResponseDTO>builder()
                .content(page.getContent().stream().map(productoMapper::toResponseDTO).toList())
                .page(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .last(page.isLast()).first(page.isFirst()).empty(page.isEmpty())
                .build();
    }
}
```

### 7.7. ProductoController.java

```java
package com.pventabase.inventario.controller;

import com.pventabase.common.constants.AppConstants;
import com.pventabase.common.dto.PageResponseDTO;
import com.pventabase.inventario.dto.*;
import com.pventabase.inventario.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<ProductoResponseDTO>> findAll(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir) {
        return ResponseEntity.ok(productoService.findAll(page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.findById(id));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ProductoResponseDTO> findByCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(productoService.findByCodigo(codigo));
    }

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> create(@Valid @RequestBody ProductoRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.create(requestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> update(@PathVariable Long id,
                                                       @Valid @RequestBody ProductoRequestDTO requestDTO) {
        return ResponseEntity.ok(productoService.update(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<ProductoResponseDTO> toggleActivo(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.toggleActivo(id));
    }
}
```

---

## 8. MÓDULO pventabase-clientes

### 8.1. pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project ...>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.pventabase</groupId>
        <artifactId>pventabase</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <artifactId>pventabase-clientes</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>com.pventabase</groupId>
            <artifactId>pventabase-common</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
        </dependency>
    </dependencies>
</project>
```

### 8.2. Cliente.java (Entity)

```java
package com.pventabase.clientes.entity;

import com.pventabase.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "clientes")
@Getter @Setter
public class Cliente extends BaseEntity {

    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String apellido;

    @NotBlank @Size(max = 255)
    @Column(name = "domicilio", nullable = false, length = 255)
    private String direccion;

    @Size(max = 20)
    @Column(name = "celular", length = 20)
    private String telefono;

    @NotBlank @Email @Size(max = 150)
    @Column(name = "correo", nullable = false, unique = true, length = 150)
    private String email;

    @Column(precision = 10, scale = 2)
    private BigDecimal credito;

    @Column(nullable = false)
    private Boolean eliminado = false;

    @Size(max = 20) @Column(length = 20)
    private String documento;
}
```

### 8.3. DTOs Clientes

#### ClienteRequestDTO.java
```java
package com.pventabase.clientes.dto;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class ClienteRequestDTO {
    @NotBlank @Size(max = 100) private String nombre;
    @NotBlank @Size(max = 100) private String apellido;
    @NotBlank @Size(max = 255) private String direccion;
    @Size(max = 20) private String telefono;
    @NotBlank @Email @Size(max = 150) private String email;
    private BigDecimal credito;
    private Boolean activo;
    @Size(max = 20) private String documento;
}
```

#### ClienteResponseDTO.java
```java
package com.pventabase.clientes.dto;
import com.pventabase.common.dto.BaseResponseDTO;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class ClienteResponseDTO extends BaseResponseDTO {
    private String nombre;
    private String apellido;
    private String direccion;
    private String telefono;
    private String email;
    private BigDecimal credito;
    private Boolean eliminado;
    private String documento;
}
```

### 8.4. ClienteMapper.java

```java
package com.pventabase.clientes.mapper;

import com.pventabase.clientes.dto.ClienteRequestDTO;
import com.pventabase.clientes.dto.ClienteResponseDTO;
import com.pventabase.clientes.entity.Cliente;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    ClienteResponseDTO toResponseDTO(Cliente cliente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    Cliente toEntity(ClienteRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    void updateEntity(ClienteRequestDTO requestDTO, @MappingTarget Cliente cliente);
}
```

### 8.5. ClienteRepository.java

```java
package com.pventabase.clientes.repository;

import com.pventabase.clientes.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

### 8.6. ClienteService.java

```java
package com.pventabase.clientes.service;

import com.pventabase.common.dto.PageResponseDTO;
import com.pventabase.common.exception.*;
import com.pventabase.clientes.dto.*;
import com.pventabase.clientes.entity.Cliente;
import com.pventabase.clientes.mapper.ClienteMapper;
import com.pventabase.clientes.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Transactional(readOnly = true)
    public PageResponseDTO<ClienteResponseDTO> findAll(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return buildPageResponse(clienteRepository.findAll(PageRequest.of(page, size, sort)));
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO findById(Long id) {
        return clienteMapper.toResponseDTO(clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id)));
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO findByEmail(String email) {
        return clienteMapper.toResponseDTO(clienteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "email", email)));
    }

    public ClienteResponseDTO create(ClienteRequestDTO requestDTO) {
        if (clienteRepository.existsByEmail(requestDTO.getEmail()))
            throw new DuplicateResourceException("Cliente", "email", requestDTO.getEmail());
        return clienteMapper.toResponseDTO(clienteRepository.save(clienteMapper.toEntity(requestDTO)));
    }

    public ClienteResponseDTO update(Long id, ClienteRequestDTO requestDTO) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        if (!cliente.getEmail().equals(requestDTO.getEmail())
                && clienteRepository.existsByEmail(requestDTO.getEmail()))
            throw new DuplicateResourceException("Cliente", "email", requestDTO.getEmail());
        clienteMapper.updateEntity(requestDTO, cliente);
        return clienteMapper.toResponseDTO(clienteRepository.save(cliente));
    }

    public void delete(Long id) {
        if (!clienteRepository.existsById(id))
            throw new ResourceNotFoundException("Cliente", id);
        clienteRepository.deleteById(id);
    }

    public ClienteResponseDTO toggleActivo(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        cliente.setActivo(!cliente.getActivo());
        return clienteMapper.toResponseDTO(clienteRepository.save(cliente));
    }

    public ClienteResponseDTO marcarEliminado(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        cliente.setEliminado(true);
        return clienteMapper.toResponseDTO(clienteRepository.save(cliente));
    }

    private PageResponseDTO<ClienteResponseDTO> buildPageResponse(Page<Cliente> page) {
        return PageResponseDTO.<ClienteResponseDTO>builder()
                .content(page.getContent().stream().map(clienteMapper::toResponseDTO).toList())
                .page(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .last(page.isLast()).first(page.isFirst()).empty(page.isEmpty())
                .build();
    }
}
```

### 8.7. ClienteController.java

```java
package com.pventabase.clientes.controller;

import com.pventabase.common.constants.AppConstants;
import com.pventabase.common.dto.PageResponseDTO;
import com.pventabase.clientes.dto.*;
import com.pventabase.clientes.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<ClienteResponseDTO>> findAll(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir) {
        return ResponseEntity.ok(clienteService.findAll(page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.findById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ClienteResponseDTO> findByEmail(@PathVariable String email) {
        return ResponseEntity.ok(clienteService.findByEmail(email));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> create(@Valid @RequestBody ClienteRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.create(requestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> update(@PathVariable Long id,
                                                      @Valid @RequestBody ClienteRequestDTO requestDTO) {
        return ResponseEntity.ok(clienteService.update(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<ClienteResponseDTO> toggleActivo(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.toggleActivo(id));
    }

    @PatchMapping("/{id}/marcar-eliminado")
    public ResponseEntity<ClienteResponseDTO> marcarEliminado(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.marcarEliminado(id));
    }
}
```

---

## 9. MÓDULO pventabase-app (Entry Point)

### 9.1. pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project ...>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.pventabase</groupId>
        <artifactId>pventabase</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <artifactId>pventabase-app</artifactId>
    <packaging>jar</packaging>
    <name>pventabase-app</name>
    <description>Entry Point - Application Bootstrap</description>
    <dependencies>
        <!-- Módulos internos del proyecto -->
        <dependency><groupId>com.pventabase</groupId><artifactId>pventabase-common</artifactId></dependency>
        <dependency><groupId>com.pventabase</groupId><artifactId>pventabase-usuarios</artifactId></dependency>
        <dependency><groupId>com.pventabase</groupId><artifactId>pventabase-login</artifactId></dependency>
        <dependency><groupId>com.pventabase</groupId><artifactId>pventabase-inventario</artifactId></dependency>
        <dependency><groupId>com.pventabase</groupId><artifactId>pventabase-clientes</artifactId></dependency>

        <!-- Spring Boot Starters -->
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>

        <!-- Documentación -->
        <dependency><groupId>org.springdoc</groupId><artifactId>springdoc-openapi-starter-webmvc-ui</artifactId></dependency>

        <!-- BD -->
        <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
        <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency>

        <!-- Utilidades -->
        <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId></dependency>

        <!-- Testing -->
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
        <dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-test</artifactId><scope>test</scope></dependency>
        <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>test</scope></dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <mainClass>com.pventabase.app.PventabaseApplication</mainClass>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.flywaydb</groupId>
                <artifactId>flyway-maven-plugin</artifactId>
                <configuration>
                    <url>jdbc:postgresql://localhost:5432/pventabase_db</url>
                    <user>app_user</user>
                    <password>app_pass</password>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 9.2. PventabaseApplication.java

```java
package com.pventabase.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.pventabase")
@EntityScan(basePackages = "com.pventabase")
@EnableJpaRepositories(basePackages = "com.pventabase")
public class PventabaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(PventabaseApplication.class, args);
    }
}
```

### 9.3. SecurityConfig.java

```java
package com.pventabase.app.config;

import com.pventabase.login.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### 9.4. OpenApiConfig.java

```java
package com.pventabase.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("pventabase API")
                        .description("Point of Sale API")
                        .version("1.0-SNAPSHOT")
                        .contact(new Contact().name("Desarrollador").email("dev@pventabase.com"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }

    @Bean
    public GroupedOpenApi usuariosApi() {
        return GroupedOpenApi.builder().group("usuarios").pathsToMatch("/usuarios/**").build();
    }

    @Bean
    public GroupedOpenApi loginApi() {
        return GroupedOpenApi.builder().group("login").pathsToMatch("/auth/**").build();
    }

    @Bean
    public GroupedOpenApi inventarioApi() {
        return GroupedOpenApi.builder().group("inventario").pathsToMatch("/productos/**").build();
    }

    @Bean
    public GroupedOpenApi clientesApi() {
        return GroupedOpenApi.builder().group("clientes").pathsToMatch("/clientes/**").build();
    }
}
```

### 9.5. GlobalExceptionHandler.java

```java
package com.pventabase.app.config;

import com.pventabase.common.constants.ErrorCodes;
import com.pventabase.common.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(InvalidStateException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                details.put(error.getField(), error.getDefaultMessage()));
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR,
                "Error de validacion", details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_ERROR,
                "Error interno del servidor");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String errorCode, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(errorCode, message, status.value(), LocalDateTime.now(), null));
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String errorCode,
                                                         String message, Map<String, String> details) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(errorCode, message, status.value(), LocalDateTime.now(), details));
    }

    private record ErrorResponse(String errorCode, String message, int status,
                                 LocalDateTime timestamp, Map<String, String> details) {}
}
```

### 9.6. application.properties

```properties
# Server
server.port=8090
server.address=0.0.0.0
server.servlet.context-path=/api

# PostgreSQL Datasource
spring.datasource.url=jdbc:postgresql://localhost:5432/pventai_db
spring.datasource.username=miguel
spring.datasource.password=Gallego
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.open-in-view=false

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# Jackson
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ss
spring.jackson.time-zone=America/Argentina/Buenos_Aires

# JWT
jwt.secret=PventabaseClaveSecretaSegura2026ParaJWT
jwt.expiration=86400000

# Logging
logging.level.com.pventabase=DEBUG
logging.level.org.springframework.security=INFO

# Springdoc OpenAPI
springdoc.paths-to-match=/usuarios/**,/auth/**,/productos/**,/clientes/**
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
```

### 9.7. Migraciones Flyway

#### V1__create_usuarios_table.sql
```sql
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    telefono VARCHAR(20),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_activo ON usuarios(activo);
```

#### V2__add_audit_columns_to_usuarios.sql
```sql
ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
```

#### V3__create_productos_table.sql
```sql
CREATE TABLE IF NOT EXISTS producto (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio_compra NUMERIC(19,2) NOT NULL,
    precio_venta NUMERIC(19,2) NOT NULL,
    existencia INTEGER NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT producto_codigo_key UNIQUE (codigo),
    CONSTRAINT producto_existencia_check CHECK (existencia >= 0),
    CONSTRAINT producto_precio_compra_check CHECK (precio_compra >= 0),
    CONSTRAINT producto_precio_venta_check CHECK (precio_venta >= 0)
);
CREATE INDEX IF NOT EXISTS idx_producto_codigo ON producto(codigo);
CREATE INDEX IF NOT EXISTS idx_producto_activo ON producto(activo) WHERE activo = TRUE;
CREATE INDEX IF NOT EXISTS idx_producto_nombre_btree ON producto(nombre);
```

#### V4__create_clientes_table.sql
```sql
CREATE TABLE IF NOT EXISTS clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    domicilio VARCHAR(255) NOT NULL,
    celular VARCHAR(20),
    correo VARCHAR(150) NOT NULL,
    credito NUMERIC(10,2),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    eliminado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT clientes_correo_key UNIQUE (correo)
);
CREATE INDEX IF NOT EXISTS idx_clientes_correo ON clientes(correo);
CREATE INDEX IF NOT EXISTS idx_clientes_activo ON clientes(activo) WHERE activo = TRUE;
```

#### V5__add_documento_to_clientes.sql
```sql
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS documento VARCHAR(20);
CREATE INDEX IF NOT EXISTS idx_clientes_documento ON clientes(documento);
```

---

## 10. DOCKER

### 10.1. Dockerfile (multi-stage)

```dockerfile
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY pventabase-common/pom.xml pventabase-common/
COPY pventabase-usuarios/pom.xml pventabase-usuarios/
COPY pventabase-login/pom.xml pventabase-login/
COPY pventabase-inventario/pom.xml pventabase-inventario/
COPY pventabase-clientes/pom.xml pventabase-clientes/
COPY pventabase-app/pom.xml pventabase-app/

RUN mvn dependency:go-offline -B

COPY pventabase-common/src pventabase-common/src/
COPY pventabase-usuarios/src pventabase-usuarios/src/
COPY pventabase-login/src pventabase-login/src/
COPY pventabase-inventario/src pventabase-inventario/src/
COPY pventabase-clientes/src pventabase-clientes/src/
COPY pventabase-app/src pventabase-app/src/

RUN mvn package -DskipTests -B -pl pventabase-app -am

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder /app/pventabase-app/target/*.jar app.jar

EXPOSE 8080

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 10.2. docker-compose.yml

```yaml
services:
  app:
    image: pventabase:1.0
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8090:8080"
    extra_hosts:
      - "host.docker.internal:host-gateway"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/pventabase_db
      - SPRING_DATASOURCE_USERNAME=miguel
      - SPRING_DATASOURCE_PASSWORD=Gallego
    restart: unless-stopped
```

---

## 11. ENDPOINTS COMPLETOS DE LA API

### 11.1. Autenticación (`/auth`)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/auth/login` | Iniciar sesión (email + password) | No |
| POST | `/auth/register` | Registrar nuevo usuario | No |

### 11.2. Usuarios (`/usuarios`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/usuarios` | Lista paginada (page, size, sortBy, sortDir) |
| GET | `/usuarios/{id}` | Buscar por ID |
| GET | `/usuarios/email/{email}` | Buscar por email |
| POST | `/usuarios` | Crear usuario |
| PUT | `/usuarios/{id}` | Actualizar usuario |
| DELETE | `/usuarios/{id}` | Eliminar usuario (físico) |
| PATCH | `/usuarios/{id}/toggle-activo` | Activar/Desactivar |

### 11.3. Productos (`/productos`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/productos` | Lista paginada |
| GET | `/productos/{id}` | Buscar por ID |
| GET | `/productos/codigo/{codigo}` | Buscar por código |
| POST | `/productos` | Crear producto |
| PUT | `/productos/{id}` | Actualizar producto |
| DELETE | `/productos/{id}` | Eliminar producto (físico) |
| PATCH | `/productos/{id}/toggle-activo` | Activar/Desactivar |

### 11.4. Clientes (`/clientes`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/clientes` | Lista paginada |
| GET | `/clientes/{id}` | Buscar por ID |
| GET | `/clientes/email/{email}` | Buscar por email |
| POST | `/clientes` | Crear cliente |
| PUT | `/clientes/{id}` | Actualizar cliente |
| DELETE | `/clientes/{id}` | Eliminar cliente (físico) |
| PATCH | `/clientes/{id}/toggle-activo` | Activar/Desactivar |
| PATCH | `/clientes/{id}/marcar-eliminado` | Borrado lógico |

---

## 12. CONFIGURACIÓN DE SEGURIDAD

- **CSRF:** Deshabilitado
- **Sesión:** STATELESS (sin sesión HTTP)
- **CORS:** Permitido cualquier origen (`setAllowedOriginPatterns("*")`) con `allowCredentials(true)`
- **Métodos HTTP permitidos:** GET, POST, PUT, DELETE, PATCH, OPTIONS
- **Headers permitidos:** Authorization, Content-Type, Accept, Origin, X-Requested-With
- **JWT:** HMAC-SHA con clave secreta (`jwt.secret`), expiración 24h (`86400000ms`)
- **Password encoder:** BCrypt
- **Rutas públicas:** Todas (`.anyRequest().permitAll()`), pero el JWT filter se ejecuta siempre
- **JWT Filter:** Extrae token `Bearer` del header `Authorization`, valida, y carga `SecurityContext` con rol como `SimpleGrantedAuthority`

---

## 13. ESQUEMA DE BASE DE DATOS (PostgreSQL)

### Tabla `usuarios`

| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | BIGSERIAL | PRIMARY KEY |
| nombre | VARCHAR(100) | NOT NULL |
| apellido | VARCHAR(100) | NOT NULL |
| email | VARCHAR(150) | NOT NULL, UNIQUE |
| password | VARCHAR(255) | NOT NULL |
| rol | VARCHAR(20) | NOT NULL, DEFAULT 'ROLE_USER' |
| telefono | VARCHAR(20) | |
| activo | BOOLEAN | NOT NULL, DEFAULT TRUE |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() |
| updated_at | TIMESTAMP | |
| created_by | VARCHAR(255) | |
| updated_by | VARCHAR(255) | |

### Tabla `producto`

| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | BIGSERIAL | PRIMARY KEY |
| codigo | VARCHAR(50) | NOT NULL, UNIQUE |
| nombre | VARCHAR(150) | NOT NULL |
| descripcion | TEXT | |
| precio_compra | NUMERIC(19,2) | NOT NULL, CHECK >= 0 |
| precio_venta | NUMERIC(19,2) | NOT NULL, CHECK >= 0 |
| existencia | INTEGER | NOT NULL, DEFAULT 0, CHECK >= 0 |
| activo | BOOLEAN | NOT NULL, DEFAULT TRUE |
| fecha_creacion | TIMESTAMP | NOT NULL, DEFAULT NOW() |
| fecha_modificacion | TIMESTAMP | NOT NULL, DEFAULT NOW() |
| created_by | VARCHAR(255) | |
| updated_by | VARCHAR(255) | |

### Tabla `clientes`

| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | BIGSERIAL | PRIMARY KEY |
| nombre | VARCHAR(100) | NOT NULL |
| apellido | VARCHAR(100) | NOT NULL |
| domicilio | VARCHAR(255) | NOT NULL |
| celular | VARCHAR(20) | |
| correo | VARCHAR(150) | NOT NULL, UNIQUE |
| credito | NUMERIC(10,2) | |
| activo | BOOLEAN | NOT NULL, DEFAULT TRUE |
| eliminado | BOOLEAN | NOT NULL, DEFAULT FALSE |
| documento | VARCHAR(20) | |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() |
| created_by | VARCHAR(255) | |
| updated_by | VARCHAR(255) | |

---

## 14. COMANDOS ÚTILES

### Compilar
```bash
mvn clean compile
```

### Ejecutar
```bash
mvn spring-boot:run -pl pventabase-app -am
```

### Packagear
```bash
mvn package -DskipTests -pl pventabase-app -am
```

### Reinstalar módulo (cuando no se reflejan cambios)
```bash
mvn install -pl pventabase-clientes -am -DskipTests
```

### Flyway repair (si hay mismatch en migraciones)
```bash
mvn flyway:repair -pl pventabase-app
```

### Docker
```bash
docker-compose up --build
```

---

## 15. ANEXO: ESTRUCTURA COMPLETA DE ARCHIVOS

```
pventabase/
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── pom.xml                                 # Parent POM (59 lines aprox)
│
├── pventabase-common/
│   ├── pom.xml
│   └── src/main/java/com/pventabase/common/
│       ├── constants/
│       │   ├── AppConstants.java
│       │   └── ErrorCodes.java
│       ├── dto/
│       │   ├── BaseResponseDTO.java
│       │   └── PageResponseDTO.java
│       ├── entity/
│       │   └── BaseEntity.java
│       └── exception/
│           ├── BusinessException.java
│           ├── DuplicateResourceException.java
│           ├── InvalidStateException.java
│           └── ResourceNotFoundException.java
│
├── pventabase-usuarios/
│   ├── pom.xml
│   └── src/main/java/com/pventabase/usuarios/
│       ├── controller/UsuarioController.java
│       ├── dto/
│       │   ├── UsuarioRequestDTO.java
│       │   └── UsuarioResponseDTO.java
│       ├── entity/
│       │   ├── RolUsuario.java
│       │   └── Usuario.java
│       ├── mapper/UsuarioMapper.java
│       ├── repository/UsuarioRepository.java
│       └── service/UsuarioService.java
│
├── pventabase-login/
│   ├── pom.xml
│   └── src/main/java/com/pventabase/login/
│       ├── controller/AuthController.java
│       ├── dto/
│       │   ├── LoginRequestDTO.java
│       │   ├── LoginResponseDTO.java
│       │   └── RegisterRequestDTO.java
│       ├── security/
│       │   ├── JwtAuthenticationFilter.java
│       │   └── JwtTokenProvider.java
│       └── service/AuthService.java
│
├── pventabase-inventario/
│   ├── pom.xml
│   └── src/main/java/com/pventabase/inventario/
│       ├── controller/ProductoController.java
│       ├── dto/
│       │   ├── ProductoRequestDTO.java
│       │   └── ProductoResponseDTO.java
│       ├── entity/Producto.java
│       ├── mapper/ProductoMapper.java
│       ├── repository/ProductoRepository.java
│       └── service/ProductoService.java
│
├── pventabase-clientes/
│   ├── pom.xml
│   ├── README.md
│   └── src/main/java/com/pventabase/clientes/
│       ├── controller/ClienteController.java
│       ├── dto/
│       │   ├── ClienteRequestDTO.java
│       │   └── ClienteResponseDTO.java
│       ├── entity/Cliente.java
│       ├── mapper/ClienteMapper.java
│       ├── repository/ClienteRepository.java
│       └── service/ClienteService.java
│
└── pventabase-app/
    ├── pom.xml
    └── src/main/
        ├── java/com/pventabase/app/
        │   ├── PventabaseApplication.java
        │   └── config/
        │       ├── GlobalExceptionHandler.java
        │       ├── OpenApiConfig.java
        │       └── SecurityConfig.java
        └── resources/
            ├── application.properties
            └── db/migration/
                ├── V1__create_usuarios_table.sql
                ├── V2__add_audit_columns_to_usuarios.sql
                ├── V3__create_productos_table.sql
                ├── V4__create_clientes_table.sql
                └── V5__add_documento_to_clientes.sql
```

**Total: 59 archivos | 6 módulos Maven | 8 tablas BD (3 de datos + migraciones)**

---

## 16. REGLAS DE ARQUITECTURA (Obligatorias)

1. **Inyección de dependencias:** Solo por constructor (`@RequiredArgsConstructor`)
2. **Transacciones:** `@Transactional` en Services, nunca en Controllers o Repositories
3. **Validación:** `@Valid` en DTOs de Controller
4. **Mapeo DTO-Entity:** MapStruct con `componentModel="spring"`. Ignorar siempre: `id`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `activo`. En Cliente ignorar también `eliminado`. En Usuario ignorar `password` en `updateEntity`
5. **Entidades:** Siempre heredar de `BaseEntity` (tiene auditoría: createdAt, updatedAt, createdBy, updatedBy, activo)
6. **BD:** Nombres de columnas en la BD pueden diferir de los campos Java usando `@Column(name = "columna_bd")`
7. **Seguridad:** Stateless JWT, roles como `SimpleGrantedAuthority`
8. **Config:** Solo `application.properties` (no YAML)
9. **Java 17 + Maven** con `maven-compiler-plugin` configurado con annotation processors de Lombok + MapStruct

---

> **Fin del documento GENERAL.md**
> Proyecto listo para ser recreado desde cero con esta documentación.
