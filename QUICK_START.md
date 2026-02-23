# Quick Start - Card Platform API

## Ejecución Rápida

### 1. Build y Tests
```bash
cd /Users/oirada/IdeaProjects/card-platform
./mvnw clean test
```

**Resultado esperado:**
- ✅ BUILD SUCCESS
- ✅ 9 tests passing
- ✅ 0 failures

### 2. Ejecutar la Aplicación
```bash
./mvnw spring-boot:run
```

**Output:**
```
... Started CardPlatformApplication in X.XXX seconds
```

La aplicación estará disponible en: **http://localhost:8080**

### 3. Acceder a Swagger/OpenAPI
```
http://localhost:8080/swagger-ui/index.html
```

---

## Pruebas de Endpoints

### A. Crear Tarjeta

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

**Respuesta:**
```json
{
  "codigo": "00",
  "mensaje": "Éxito",
  "numeroValidacion": 42,
  "panEnmascarado": "411111****1111",
  "identificador": "abc123def456..."
}
```

**Guardar:** `identificador` y `numeroValidacion` para los siguientes pasos

---

### B. Enrolar Tarjeta

```bash
curl -X POST http://localhost:8080/api/cards/enrol \
  -H "Content-Type: application/json" \
  -d '{
    "identificador": "abc123def456...",
    "numeroValidacion": 42
  }'
```

**Respuesta (éxito):**
```json
{
  "codigo": "00",
  "mensaje": "Éxito",
  "panEnmascarado": "411111****1111"
}
```

**Respuesta (error - validación incorrecta):**
```json
{
  "codigo": "01",
  "mensaje": "Operación inválida",
  "panEnmascarado": null
}
```
*Nota: No revela si tarjeta existe o número es inválido (seguridad)*

---

### C. Crear Transacción

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "identificador": "abc123def456...",
    "referencia": "123456",
    "total": 99.99,
    "direccion": "Av Principal 123, Quito"
  }'
```

**Respuesta:**
```json
{
  "codigo": "00",
  "mensaje": "Compra exitosa",
  "estadoTransaccion": "APPROVED",
  "referencia": "123456"
}
```

---

### D. Anular Transacción

```bash
curl -X POST http://localhost:8080/api/transactions/annul \
  -H "Content-Type: application/json" \
  -d '{
    "identificador": "abc123def456...",
    "referencia": "123456",
    "total": 99.99
  }'
```

**Respuesta:**
```json
{
  "codigo": "00",
  "mensaje": "Compra anulada",
  "referencia": "123456"
}
```

---

### E. Consultar Tarjeta

```bash
curl -X GET http://localhost:8080/api/cards/abc123def456...
```

**Respuesta:**
```json
{
  "panEnmascarado": "411111****1111",
  "titular": "John Doe",
  "cedula": "1234567890",
  "telefono": "0987654321",
  "estado": "ENROLADA"
}
```

---

### F. Eliminar Tarjeta

```bash
curl -X DELETE http://localhost:8080/api/cards/abc123def456...
```

**Respuesta:**
```json
{
  "codigo": "00",
  "mensaje": "Se ha eliminado la tarjeta"
}
```

---

## Casos de Error (Seguridad)

### Enrolar con ID inexistente

```bash
curl -X POST http://localhost:8080/api/cards/enrol \
  -H "Content-Type: application/json" \
  -d '{
    "identificador": "nonexistent-id",
    "numeroValidacion": 50
  }'
```

**Respuesta (404):**
```json
{
  "codigo": "01",
  "mensaje": "Operación inválida",
  "panEnmascarado": null
}
```

*✅ Seguridad: No revela que la tarjeta no existe*

---

### Crear Transacción con Referencia Duplicada

```bash
# Primera vez - OK
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "identificador": "abc123...",
    "referencia": "111111",
    "total": 50.0,
    "direccion": "Av Test"
  }'

# Segunda vez - Error (409 CONFLICT)
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "identificador": "abc123...",
    "referencia": "111111",
    "total": 50.0,
    "direccion": "Av Test"
  }'
```

**Respuesta (409):**
```json
{
  "codigo": "03",
  "mensaje": "Operación inválida",
  "estadoTransaccion": null,
  "referencia": null
}
```

---

### Anular Transacción con Tiempo > 5 minutos

```bash
curl -X POST http://localhost:8080/api/transactions/annul \
  -H "Content-Type: application/json" \
  -d '{
    "identificador": "abc123...",
    "referencia": "111111",
    "total": 50.0
  }'
```

**Respuesta (409):**
```json
{
  "codigo": "02",
  "mensaje": "Operación inválida",
  "referencia": null
}
```

*Si la transacción fue creada hace > 5 minutos*

---

## Estructura de Carpetas

```
card-platform/
├── src/
│   ├── main/
│   │   ├── java/com/oscar/cardplatform/
│   │   │   ├── CardPlatformApplication.java
│   │   │   ├── domain/entity/
│   │   │   │   ├── Card.java
│   │   │   │   ├── CardStatus.java (CREADA, ENROLADA, INACTIVE)
│   │   │   │   ├── CardType.java (CREDITO, DEBITO)
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── TransactionStatus.java (APPROVED, REJECTED)
│   │   │   │   └── AuditLog.java
│   │   │   ├── repository/
│   │   │   │   ├── CardRepository.java
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   └── AuditLogRepository.java
│   │   │   ├── service/
│   │   │   │   ├── CardService.java
│   │   │   │   ├── TransactionService.java
│   │   │   │   └── util/
│   │   │   │       └── PanUtils.java
│   │   │   ├── web/
│   │   │   │   ├── controller/
│   │   │   │   │   ├── CardController.java
│   │   │   │   │   └── TransactionController.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── CreateCardRequest.java
│   │   │   │   │   ├── CreateCardResponse.java
│   │   │   │   │   ├── EnrolCardRequest.java
│   │   │   │   │   ├── EnrolCardResponse.java
│   │   │   │   │   ├── CreateTransactionRequest.java
│   │   │   │   │   ├── CreateTransactionResponse.java
│   │   │   │   │   ├── AnnulTransactionRequest.java
│   │   │   │   │   ├── AnnulTransactionResponse.java
│   │   │   │   │   └── (otros DTOs)
│   │   │   │   └── exception/
│   │   │   │       └── GlobalExceptionHandler.java
│   │   ├── resources/
│   │   │   └── application.yaml
│   ├── test/
│   │   ├── java/com/oscar/cardplatform/
│   │   │   ├── CardPlatformApplicationTests.java
│   │   │   ├── service/
│   │   │   │   └── CardServiceTest.java
│   │   │   ├── web/controller/
│   │   │   │   └── CardControllerTest.java
│   │   │   ├── integration/
│   │   │   │   └── CardFlowIntegrationTest.java
│   │   │   └── security/
│   │   │       └── SecurityGenericMessagesTest.java
├── pom.xml
├── README.md
├── CAMBIOS_REALIZADOS.md
└── ANALISIS_SEGURIDAD.md
```

---

## Verificación de Componentes

### ✅ Validaciones Implementadas

- [ ] PAN: 16-19 dígitos
- [ ] Titular: No vacío
- [ ] Cédula: 10-15 caracteres
- [ ] Tipo: "Credito" o "Debito"
- [ ] Teléfono: 10 dígitos
- [ ] Referencia: 6 dígitos, única
- [ ] Total: Positivo y no nulo

### ✅ Seguridad

- [ ] Mensajes genéricos (no revela existencia)
- [ ] Logs internos de intentos fallidos
- [ ] PAN enmascarado en respuestas
- [ ] Identificador único por tarjeta
- [ ] Número de validación aleatorio (1-100)
- [ ] Constraints únicos en BD (referencia, identificador)

### ✅ Tests

- [ ] 9/9 tests pasando
- [ ] Cobertura > 80%
- [ ] Tests de seguridad (no expone detalles)
- [ ] Tests de flujo completo
- [ ] Tests de errores

### ✅ Documentación

- [ ] README.md actualizado
- [ ] Swagger/OpenAPI disponible
- [ ] Anotaciones @Operation en controladores
- [ ] Anotaciones @Schema en DTOs
- [ ] Descripción de códigos de respuesta

---

## Comandos Útiles

### Ver logs de aplicación
```bash
tail -f /var/log/card-platform/application.log
```

### Ver logs de intentos fallidos
```bash
grep "security: hiding detail" /var/log/card-platform/application.log
```

### Detener la aplicación
```bash
# Ctrl+C en terminal donde se ejecuta ./mvnw spring-boot:run
```

### Limpiar caché de Maven
```bash
./mvnw clean
```

### Generar reporte de cobertura
```bash
./mvnw jacoco:report
# Ver en: target/site/jacoco/index.html
```

---

## Contacto y Soporte

**Repositorio:** `/Users/oirada/IdeaProjects/card-platform`
**Java:** 17
**Spring Boot:** 4.0.3
**Base de Datos:** H2 (en memoria para tests)

---
