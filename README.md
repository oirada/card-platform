# Card Platform API

API REST para gestión de tarjetas y transacciones.

## Tabla de Contenidos
1. [Detalle de la prueba](#detalle-de-la-prueba)
2. [Consideraciones Iniciales](#consideraciones-iniciales)
3. [Tecnologías](#tecnologías)
4. [Ejecución de la Aplicación](#ejecución-de-la-aplicación)
   4.1. [Ejecución en Entorno Local](#ejecución-en-entorno-local)
   4.2. [Ejecución a través del IDE](#ejecución-a-través-del-ide)
   4.3. [Consumo directo de Servicios](#consumo-directo-de-servicios)
5. [Pruebas Unitarias / Integración](#pruebas-unitarias--integración)
6. [Endpoints y formatos](#endpoints-y-formatos)
7. [Política de Seguridad](#política-de-seguridad)

### Detalle de la Prueba
Se requiere crear una aplicación para administración de tarjetas de crédito y transacciones de
compra, para esto se debe crear un API Restful para la creación y actualización de objetos y una
interfaz gráfica para consultar los mismos.

### Consideraciones Iniciales
- Los mensajes de error expuestos por la API son genéricos por razones de seguridad. **No se filtra información sensible** como existencia de tarjetas o estados internos.
- El PAN nunca se persiste en texto plano. Se almacena un hash SHA-256 y un PAN enmascarado (se muestran los primeros 6 y los últimos 4 dígitos, el resto es ocultado con `*`).
- Se utiliza una arquitectura en capas: Controller (exposición REST), Service (lógica de negocio), Repository (acceso a datos).
- Cada tarjeta recibe un **identificador único** generado como SHA-256 del PAN + fecha actual. Este identificador se usa en lugar del PAN en endpoints posteriores (enrolamiento, transacciones, etc.).
- **Número de validación**: Al crear una tarjeta, se genera un número aleatorio entre 1-100 que debe ser proporcionado para enrolar la tarjeta. Solo después del enrolamiento exitoso la tarjeta puede usarse para transacciones.
- **Estados de tarjeta**: CREADA (inicial), ENROLADA (lista para transacciones), INACTIVE (borrado lógico).
- Se implementaron pruebas unitarias e integración cubriendo: creación de tarjeta, enrolamiento con validación correcta e incorrecta, creación de transacciones, anulación de transacciones (happy path y casos de error).
- Se implementó un manejador global de excepciones (`GlobalExceptionHandler`) que centraliza el mapeo de errores a respuestas genéricas, evitando revelar detalles internos.
- Se añadió logging/auditoría detallado (nivel WARN/ERROR) para eventos sensibles: intentos fallidos de acceso a recursos no existentes, validaciones fallidas, violaciones de constraints. Estos logs son accesibles solo a personal autorizado.
- Se añadió documentación OpenAPI/Swagger con anotaciones en controladores y DTOs para autodescubrimiento de la API.

### Tecnologías
* Java 17
* Spring Boot 4.0.3
* Spring Data JPA / Hibernate
* Lombok
* H2 (en memoria para tests)
* JUnit 5, Mockito
* OpenAPI / Swagger (springdoc)
* SLF4J / Logback (logging)

### Ejecución de la Aplicación
#### Ejecución en Entorno Local:

```bash
mvn clean spring-boot:run
```

La aplicación quedará escuchando en http://localhost:8080

#### Ejecución a través del IDE:
Importar el proyecto Maven y ejecutar la clase `com.oscar.cardplatform.CardPlatformApplication`.

#### Consumo directo de Servicios
Usar `curl` o Postman para consumir los endpoints descritos más abajo.

### Pruebas Unitarias / Integración
Para ejecutar las pruebas unitarias e integración:

```bash
mvn test
```

Los tests de integración incluyen el flujo completo: crear tarjeta -> enrolar -> crear transacción -> anular transacción (con casos de error).

Tests de seguridad verifican que las respuestas no exponen detalles sensibles (ej.: no revelan si una tarjeta existe).

### Endpoints y formatos

Nota: la API NO recibe el PAN en endpoints posteriores (enrolamiento, transacciones, etc.). Se trabaja exclusivamente con el `identificador` generado al crear la tarjeta.

**IMPORTANTE**: Por razones de seguridad, los mensajes de error devueltos son genéricos y no revelan información sobre la existencia de recursos. Los clientes deben usar los códigos de respuesta para interpretar el resultado (ver tabla de códigos abajo).

#### Códigos de Respuesta
- `00` = Operación exitosa
- `01` = Operación inválida (genérico para errores de validación, tarjeta no encontrada, etc.)
- `02` = Estado inválido (tarjeta no enrolada, transacción muy antigua, etc.)
- `03` = Referencia duplicada (en transacciones)

#### Endpoints

**POST /api/cards** - Crear tarjeta
  - Request JSON:
    ```json
    {
      "pan": "4111111111111111",
      "titular": "John Doe",
      "cedula": "1234567890",
      "tipo": "Credito",
      "telefono": "0987654321"
    }
    ```
  - Response 201 (éxito):
    ```json
    {
      "codigo": "00",
      "mensaje": "Éxito",
      "numeroValidacion": 42,
      "panEnmascarado": "411111****1111",
      "identificador": "a3f5e8d2c9b1..."
    }
    ```
  - Response 400 (error):
    ```json
    {
      "codigo": "01",
      "mensaje": "Operación inválida",
      "numeroValidacion": null,
      "panEnmascarado": null,
      "identificador": null
    }
    ```

**POST /api/cards/enrol** - Enrolar tarjeta
  - Request JSON:
    ```json
    {
      "identificador": "a3f5e8d2c9b1...",
      "numeroValidacion": 42
    }
    ```
  - Response 200 (éxito):
    ```json
    {
      "codigo": "00",
      "mensaje": "Éxito",
      "panEnmascarado": "411111****1111"
    }
    ```
  - Response 404 (identificador o validación incorrecta - no revela cual):
    ```json
    {
      "codigo": "01",
      "mensaje": "Operación inválida",
      "panEnmascarado": null
    }
    ```

**GET /api/cards/{identificador}** - Consultar tarjeta
  - Response 200:
    ```json
    {
      "panEnmascarado": "411111****1111",
      "titular": "John Doe",
      "cedula": "1234567890",
      "telefono": "0987654321",
      "estado": "ENROLADA"
    }
    ```
  - Response 404 (no revela si existe):
    ```json
    {
      "codigo": "01",
      "mensaje": "Operación inválida",
      ...
    }
    ```

**DELETE /api/cards/{identificador}** - Eliminar tarjeta (borrado lógico)
  - Response 200:
    ```json
    {
      "codigo": "00",
      "mensaje": "Se ha eliminado la tarjeta"
    }
    ```
  - Response 404 (no existe - respuesta genérica):
    ```json
    {
      "codigo": "01",
      "mensaje": "Operación inválida"
    }
    ```

**POST /api/transactions** - Crear transacción
  - Request JSON:
    ```json
    {
      "identificador": "a3f5e8d2c9b1...",
      "referencia": "123456",
      "total": 99.99,
      "direccion": "Av Principal 123, Quito"
    }
    ```
  - Response 200 (éxito):
    ```json
    {
      "codigo": "00",
      "mensaje": "Compra exitosa",
      "estadoTransaccion": "APPROVED",
      "referencia": "123456"
    }
    ```
  - Response 404 (tarjeta no existe - no lo revela):
    ```json
    {
      "codigo": "01",
      "mensaje": "Operación inválida",
      "estadoTransaccion": null,
      "referencia": null
    }
    ```
  - Response 409 (tarjeta no enrolada):
    ```json
    {
      "codigo": "02",
      "mensaje": "Operación inválida",
      "estadoTransaccion": null,
      "referencia": null
    }
    ```
  - Response 409 (referencia duplicada):
    ```json
    {
      "codigo": "03",
      "mensaje": "Operación inválida",
      "estadoTransaccion": null,
      "referencia": null
    }
    ```

**POST /api/transactions/annul** - Anular transacción
  - Request JSON:
    ```json
    {
      "identificador": "a3f5e8d2c9b1...",
      "referencia": "123456",
      "total": 99.99
    }
    ```
  - Response 200 (anulación exitosa):
    ```json
    {
      "codigo": "00",
      "mensaje": "Compra anulada",
      "referencia": "123456"
    }
    ```
  - Response 400 (referencia no encontrada):
    ```json
    {
      "codigo": "01",
      "mensaje": "Operación inválida",
      "referencia": null
    }
    ```
  - Response 409 (no se puede anular - pasaron >5 minutos u otro motivo):
    ```json
    {
      "codigo": "02",
      "mensaje": "Operación inválida",
      "referencia": null
    }
    ```

### OpenAPI / Swagger
Se añadió documentación OpenAPI (springdoc). Una vez la aplicación esté corriendo, la UI de Swagger estará disponible en:

- http://localhost:8080/swagger-ui/index.html

(la ruta puede variar ligeramente según versión, `springdoc-openapi-starter-webmvc-ui` expone la UI por defecto).

### Política de Seguridad

#### Mensajes Genéricos
Para proteger contra enumeración de recursos (ej.: intentos de adivinar identificadores válidos), la API retorna mensajes de error genéricos cuando un recurso no es encontrado o inaccesible. Esto previene que atacantes confirmen la existencia de tarjetas en el sistema.

#### Logging / Auditoría Interna
Cuando la API oculta detalles sensibles en la respuesta, se registra un log interno detallado (nivel WARN o ERROR según corresponda) para que el equipo de soporte pueda investigar:
- Intentos de acceso a recursos no existentes.
- Operaciones fallidas por motivos de seguridad (p.ej. número de validación incorrecto).
- Violaciones de constraints (p.ej. referencias duplicadas).

Los logs internos contienen información completa (identificador exacto, motivo de fallo) y están disponibles en `logs/` o en el sistema de logging centralizado, accesibles solo a personal autorizado.

### Notas Técnicas y recomendaciones
- `numeroValidacion` se guarda en la entidad `Card` para usar en el flujo de enrolamiento. Si prefieres un mecanismo no persistente, lo podemos cambiar.
- `referencia` de transacción es única (constraint DB + comprobación en servicio) para evitar duplicados.
- `tipo` de tarjeta se mapea a un enum interno (`CREDITO`|`DEBITO`) a partir de los valores recibidos en el request.
- Se recomienda añadir más tests y reglas de negocio (ratelimit, auditoría extendida, validaciones adicionales) según necesidades de producción.

---

Creado por: Oscar Ivan Rada Osorio
Fecha: 2025-02-19
Contacto: oirada@gmail.com
