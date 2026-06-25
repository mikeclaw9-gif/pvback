# pventabase-clientes

Módulo de gestión de clientes para el sistema PVentabase.

## Descripción

Este módulo proporciona funcionalidades completas para la gestión de clientes en el sistema PVentabase, incluyendo operaciones CRUD (Crear, Leer, Actualizar, Eliminar) y funcionalidades adicionales como activación/desactivación y borrado lógico.

## Características

- Gestión completa de clientes con los siguientes campos:
  - Nombre
  - Apellido
  - Domicilio
  - Celular
  - Correo electrónico (único)
  - Crédito (numérico con 2 decimales)
  - Estado activo/inactivo
  - Marcado de eliminación (borrado lógico)
- Campos de auditoría heredados de `BaseEntity`:
  - Fecha de creación
  - Fecha de actualización
  - Usuario que creó
  - Usuario que actualizó
- Validación de datos usando Jakarta Validation
- Arquitectura en capas (Controller, Service, Repository, Entity, DTO, Mapper)
- Implementación con Spring Boot y Spring Data JPA
- Mapeo automático entre entidades y DTOs usando MapStruct
- Manejo de excepciones personalizadas
- Respuestas paginadas para listados grandes

## Estructura del módulo

```
pventabase-clientes/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── pventabase/
│                   └── clientes/
│                       ├── controller/   # REST controllers
│                       ├── dto/          # Data Transfer Objects
│                       ├── entity/       # JPA entities
│                       ├── mapper/       # MapStruct mappers
│                       ├── repository/   # Spring Data repositories
│                       └── service/      # Business logic services
└── pom.xml
```

## API Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/clientes` | Listar todos los clientes (con paginación) |
| GET | `/clientes/{id}` | Obtener un cliente por ID |
| GET | `/clientes/correo/{correo}` | Obtener un cliente por correo electrónico |
| POST | `/clientes` | Crear un nuevo cliente |
| PUT | `/clientes/{id}` | Actualizar un cliente existente |
| DELETE | `/clientes/{id}` | Eliminar físicamente un cliente |
| PATCH | `/clientes/{id}/toggle-activo` | Activar/desactivar un cliente |
| PATCH | `/clientes/{id}/marcar-eliminado` | Marcar un cliente como eliminado (borrado lógico) |

## Dependencias

- Spring Boot
- Spring Data JPA
- MapStruct
- Lombok
- Jakarta Validation
- Base común de PVentabase (`pventabase-common`)

## Configuración

El módulo se configura principalmente a través del archivo `application.properties` o `application.yml` del proyecto principal. Asegúrese de incluir este módulo como dependencia en su proyecto principal.

## Uso

1. Asegúrese de que la base de datos esté configurada correctamente
2. Inicie la aplicación Spring Boot
3. Acceda a los endpoints a través de un cliente HTTP o la interfaz web
4. Utilice los DTOs para enviar y recibir datos

## Notas importantes

- El campo `correo` tiene restricción de unicidad a nivel de base de datos
- El borrado físico elimina permanentemente el registro
- El borrado lógico (`marcar-eliminado`) solo marca el registro como eliminado sin eliminarlo físicamente
- Los clientes eliminados lógicamente no aparecen en consultas normales por defecto
- El campo `credito` soporta valores decimales con precisión de 2 lugares
