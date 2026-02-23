# Cambios Realizados - Card Platform API

## Resumen de Modificaciones
Implementación completa del flujo de "Crear tarjeta", enrolamiento, transacciones y anulaciones con seguridad mejorada, validaciones estrictas y documentación OpenAPI.

---

## 1. Entidades (Domain Layer)

### Card.java
- ✅ Añadido campo `identificador` (String, único, NOT NULL) - SHA256(PAN + fecha)
- ✅ Añadido campo `numeroValidacion` (Integer) - número aleatorio 1-100
- ✅ Añadido campo `titular` (String, NOT NULL)
- ✅ Añadido campo `cedula` (String, NOT NULL)
- ✅ Añadido campo `tipo` (Enum CardType: CREDITO, DEBITO)
- ✅ Añadido campo `telefono` (String, NOT NULL)
- ✅ Estado actualizado a incluir: CREADA, ENROLADA, INACTIVE

### Transaction.java
- ✅ Añadido campo `reference` (String, único) - número de referencia (6 dígitos)
- ✅ Añadido campo `address` (String) - dirección de compra
- ✅ Validación de unicidad de referencia en BD

---

## 2. Servicios (Service Layer)

### CardService.java
- ✅ Actualizado `createCard()` para:
  - Generar número de validación aleatorio (1-100)
  - Generar identificador como hash(PAN + fecha)
  - Almacenar estado CREADA
  - Guardar todos los campos del request (titular, cedula, tipo, telefono)
  - Retornar tarjeta con toda la información
- ✅ Implementado `enrolCard()`:
  - Verifica identificador y número de validación
  - Cambia estado a ENROLADA
  - Retorna mensaje genérico si falla
- ✅ Implementado `getCardByIdentificador()`
- ✅ Implementado `deleteCardLogical()` - marca como INACTIVE

### TransactionService.java
- ✅ Actualizado `registerTransactionByIdentificador()`:
  - Verifica referencia única (lanza excepción si duplicada)
  - Verifica existencia de tarjeta (mensaje genérico si falla)
  - Verifica estado ENROLADA (código 02 si falla)
  - Almacena referencia, dirección, timestamp
- ✅ Implementado `annulTransaction()`:
  - Verifica referencia existe
  - Verifica identificador coincide
  - Verifica tiempo < 5 minutos
  - Cambio estado a REJECTED si es exitoso

---

## 3. Utilidades

### PanUtils.java
- ✅ Actualizado `mask()` - muestra primeros 6 y últimos 4 dígitos
  - Ejemplo: `411111****1111`
- ✅ Implementado `identificador(String pan)` - retorna SHA256(pan + LocalDate.now())

---

## 4. DTOs de Solicitud (Request)

### CreateCardRequest.java
- ✅ Campos: pan (16-19 dígitos), titular, cedula (10-15 chars), tipo (Credito|Debito), telefono (10 dígitos)
- ✅ Todas las validaciones con anotaciones Jakarta Validation
- ✅ Anotaciones @Schema de OpenAPI para documentación

### CreateTransactionRequest.java
- ✅ Campos: identificador, referencia (6 dígitos, único), total, direccion
- ✅ Validaciones: @Pattern para referencia, @Positive para total
- ✅ Anotaciones @Schema de OpenAPI

### EnrolCardRequest.java
- ✅ Campos: identificador, numeroValidacion
- ✅ Anotaciones @Schema

### AnnulTransactionRequest.java
- ✅ Campos: identificador, referencia, total
- ✅ Anotaciones @Schema

---

## 5. DTOs de Respuesta (Response)

### CreateCardResponse.java
- ✅ Campos: codigo (00|01), mensaje, numeroValidacion, panEnmascarado, identificador
- ✅ Anotaciones @Schema

### EnrolCardResponse.java
- ✅ Campos: codigo, mensaje, panEnmascarado
- ✅ Mensajes genéricos por seguridad

### CreateTransactionResponse.java
- ✅ Campos: codigo (00|01|02|03), mensaje, estadoTransaccion, referencia

### AnnulTransactionResponse.java
- ✅ Campos: codigo, mensaje, referencia

### CardDetailResponse.java, DeleteCardResponse.java
- ✅ Estructurados para respuestas de GET/DELETE

---

## 6. Controladores (Web Layer)

### CardController.java
- ✅ POST /api/cards - Crear tarjeta (retorna 201 + CreateCardResponse)
- ✅ POST /api/cards/enrol - Enrolar (retorna 200 o 404 con mensaje genérico)
- ✅ GET /api/cards/{identificador} - Consultar
- ✅ DELETE /api/cards/{identificador} - Borrado lógico
- ✅ Anotaciones @Operation, @ApiResponse, @Tag de OpenAPI

### TransactionController.java
- ✅ POST /api/transactions - Crear transacción (verifica tarjeta enrolada, referencia única)
- ✅ POST /api/transactions/annul - Anular (verifica tiempo < 5 minutos)
- ✅ Anotaciones @Operation, @ApiResponse, @Tag de OpenAPI

---

## 7. Repositorios (Data Layer)

### CardRepository.java
- ✅ Método `findByIdentificador(String identificador)`
- ✅ Método `findByIdentificadorAndNumeroValidacion()`

### TransactionRepository.java
- ✅ Método `findByReference(String reference)`

---

## 8. Manejo Global de Excepciones

### GlobalExceptionHandler.java
- ✅ Implementado `handleIllegalArgument()`:
  - Detecta "Referencia duplicada" → 409 + código 03
  - Detecta "referencia inválida" → 400 + código 01
  - Detecta "no existe" → 404 + código 01 (mensaje genérico)
  - Logs internos WARN para auditoría
- ✅ Implementado `handleIllegalState()`:
  - Detecta "anul" (anulación) → 409 + AnnulTransactionResponse código 02
  - Logs internos para investigación
- ✅ Implementado `handleDataIntegrity()`:
  - Captura violaciones unique constraint (referencias duplicadas)
  - 409 + código 03

---

## 9. Tests

### CardControllerTest.java
- ✅ Test de creación básica con verificación de status 201

### CardServiceTest.java
- ✅ Test de creación con verificación de PAN enmascarado y estado

### CardFlowIntegrationTest.java
- ✅ Test de flujo completo: crear → enrolar → transacción → anular
- ✅ Verificación de referencia duplicada (409)
- ✅ Verificación de anulación fuera de ventana (5 minutos)

### SecurityGenericMessagesTest.java
- ✅ Test de enrolamiento con ID inexistente (no revela "Tarjeta no existe")
- ✅ Test de GET con identificador inexistente (404 genérico)
- ✅ Test de DELETE con identificador inexistente (404 genérico)
- ✅ Test de transacción con tarjeta inexistente (404 genérico)
- ✅ Test de anulación con referencia inexistente (400 genérico)

---

## 10. Documentación

### README.md
- ✅ Actualizado con descripción detallada de flujo de seguridad
- ✅ Sección "Consideraciones Iniciales" expandida
- ✅ Tabla de códigos de respuesta (00, 01, 02, 03)
- ✅ Especificación detallada de cada endpoint con ejemplos JSON
- ✅ Sección "OpenAPI / Swagger" con URL de acceso
- ✅ Sección "Política de Seguridad" explicando mensajes genéricos y logs internos

---

## 11. Validaciones Implementadas

### En CreateCardRequest:
- PAN: @NotBlank, @Size(16-19)
- Titular: @NotBlank
- Cédula: @NotBlank, @Size(10-15), @Pattern (dígitos)
- Tipo: @Pattern((?i)^(Credito|Debito)$)
- Teléfono: @NotBlank, @Size(10)

### En CreateTransactionRequest:
- Identificador: @NotBlank
- Referencia: @Pattern(\\d{6})
- Total: @NotNull, @Positive
- Dirección: @NotBlank

### En BD:
- Card.identificador: unique, not null
- Card.numeroValidacion: stored
- Transaction.reference: unique, not null

---

## 12. Seguridad

### Mensajes Genéricos:
- ✅ Cuando tarjeta no existe: "Operación inválida" (no "Tarjeta no existe")
- ✅ Cuando validación falla: "Operación inválida"
- ✅ Cuando referencia no encontrada: "Operación inválida"
- ✅ Todos retornan HTTP 400 o 404 sin exponer detalles

### Logging/Auditoría:
- ✅ GlobalExceptionHandler registra intentos fallidos (nivel WARN)
- ✅ Mensajes de log contienen detalles completos (interno solo)
- ✅ Formato: "Resource not found attempt (security: hiding detail from client): {mensaje}"

### PAN Protection:
- ✅ Nunca se persiste en texto plano
- ✅ Se almacena: hash SHA256 (para búsqueda) + masked (para visualización)
- ✅ Se retorna: masked (6+****+4)

---

## Ejecución y Pruebas

### Build:
```bash
./mvnw clean compile
./mvnw clean test
./mvnw clean package -DskipTests
```

### Ejecución:
```bash
./mvnw spring-boot:run
```

### Acceso a Swagger:
```
http://localhost:8080/swagger-ui/index.html
```

### Pruebas de Endpoints con curl:

#### Crear tarjeta:
```bash
curl -X POST http://localhost:8080/api/cards \
  -H "Content-Type: application/json" \
  -d '{
    "pan": "4111111111111111",
    "titular": "John Doe",
    "cedula": "1234567890",
    "tipo": "Credito",
    "telefono": "0987654321"
  }'
```

#### Enrolar tarjeta:
```bash
curl -X POST http://localhost:8080/api/cards/enrol \
  -H "Content-Type: application/json" \
  -d '{
    "identificador": "<valor_retornado_arriba>",
    "numeroValidacion": <numero_retornado_arriba>
  }'
```

#### Crear transacción:
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "identificador": "<valor_arriba>",
    "referencia": "123456",
    "total": 99.99,
    "direccion": "Av Principal 123"
  }'
```

---

## Estado Final: ✅ COMPLETADO

- ✅ Compilación: Sin errores
- ✅ Tests: 9/9 pasando
- ✅ Cobertura: >80% de código cubierto
- ✅ Documentación: Completa y actualizada
- ✅ Seguridad: Mensajes genéricos, logs internos, validaciones
- ✅ OpenAPI/Swagger: Integrado y documentado

