# 🎯 REFERENCIA RÁPIDA - Card Platform API

## ⚡ Comandos Esenciales

```bash
# Build y Tests
cd /Users/oirada/IdeaProjects/card-platform
./mvnw clean test              # Compilar y ejecutar tests
./mvnw clean compile           # Solo compilar
./mvnw clean package           # Generar JAR

# Ejecutar
./mvnw spring-boot:run         # Inicia en http://localhost:8080

# Específico
./mvnw -Dtest=CardServiceTest test  # Test específico
```

---

## 📡 Endpoints Rápidos

### 1️⃣ Crear Tarjeta
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

**Response**: `201 Created`
```json
{
  "codigo": "00",
  "numeroValidacion": 42,
  "panEnmascarado": "411111****1111",
  "identificador": "abc123..."
}
```

### 2️⃣ Enrolar Tarjeta
```bash
curl -X POST http://localhost:8080/api/cards/enrol \
  -H "Content-Type: application/json" \
  -d '{
    "identificador": "abc123...",
    "numeroValidacion": 42
  }'
```

**Response**: `200 OK` o `404 Not Found` (genérico)

### 3️⃣ Crear Transacción
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "identificador": "abc123...",
    "referencia": "123456",
    "total": 99.99,
    "direccion": "Av Principal 123"
  }'
```

**Response**: `200 OK`, `404 Not Found`, o `409 Conflict`

### 4️⃣ Anular Transacción
```bash
curl -X POST http://localhost:8080/api/transactions/annul \
  -H "Content-Type: application/json" \
  -d '{
    "identificador": "abc123...",
    "referencia": "123456",
    "total": 99.99
  }'
```

**Response**: `200 OK`, `400 Bad Request`, o `409 Conflict`

### 5️⃣ Consultar Tarjeta
```bash
curl -X GET http://localhost:8080/api/cards/abc123...
```

**Response**: `200 OK` con detalles o `404 Not Found`

### 6️⃣ Eliminar Tarjeta
```bash
curl -X DELETE http://localhost:8080/api/cards/abc123...
```

**Response**: `200 OK` o `404 Not Found`

---

## 🔢 Códigos de Respuesta

| Código | HTTP | Significado |
|--------|------|-------------|
| 00 | 200/201 | ✅ Operación exitosa |
| 01 | 400/404 | ❌ Operación inválida (genérico) |
| 02 | 409 | ❌ Estado inválido (no enrolada, old tx) |
| 03 | 409 | ❌ Referencia duplicada |

---

## 📊 Flujo Completo

```
1. POST /api/cards
   └─ Crear tarjeta (estado: CREADA)
   └─ Retorna: identificador, numeroValidacion

2. POST /api/cards/enrol
   └─ Enrolar con numero validación
   └─ Estado cambia a: ENROLADA

3. POST /api/transactions
   └─ Crear transacción (solo si ENROLADA)
   └─ Retorna: referencia, estado

4. POST /api/transactions/annul
   └─ Anular si < 5 minutos
   └─ Estado cambia a: REJECTED

5. GET /api/cards/{id}
   └─ Consultar estado y datos

6. DELETE /api/cards/{id}
   └─ Borrado lógico (estado: INACTIVE)
```

---

## 🔐 Seguridad Clave

### Mensajes Genéricos
- Siempre: `"Operación inválida"` cuando no encuentra recurso
- NO devuelve: `"Tarjeta no existe"`, `"Referencia no encontrada"`
- **Razón**: Previene enumeración de recursos (OWASP CWE-203)

### Logs Internos
- Se registran detalles completos en logs (WARN level)
- Solo personal autorizado puede acceder
- Formato: `"security: hiding detail from client"`

### PAN Protection
- **Nunca** en texto plano
- Almacenado: `hash SHA256` + `masked`
- Retornado: `123456****1111` (6 + **** + 4)

---

## 📚 Documentación

| Archivo | Propósito |
|---------|-----------|
| **README.md** | Especificación principal |
| **QUICK_START.md** | Guía de inicio rápido |
| **CAMBIOS_REALIZADOS.md** | Registro de cambios |
| **ANALISIS_SEGURIDAD.md** | Análisis de seguridad ⚠️ |
| **RESUMEN_FINAL.md** | Estado del proyecto |
| **INDICE_DOCUMENTACION.md** | Navegación de docs |

---

## ✅ Validaciones

### PAN
- Longitud: 16-19 dígitos
- Obligatorio

### Titular
- Obligatorio
- Mínimo: 1 carácter

### Cédula
- Longitud: 10-15 caracteres
- Patrón: solo números

### Tipo
- Valores: "Credito" o "Debito"
- Case-insensitive: (?i)^(Credito|Debito)$

### Teléfono
- Longitud: 10 dígitos exactos

### Referencia (Transacción)
- Longitud: 6 dígitos exactos
- **Único** en la BD (constraint)

---

## 🎯 Test Rápido

```bash
# 1. Iniciar aplicación
./mvnw spring-boot:run &

# 2. Crear tarjeta
PAN="4111111111111111"
ID=$(curl -s -X POST http://localhost:8080/api/cards \
  -H "Content-Type: application/json" \
  -d "{
    \"pan\": \"$PAN\",
    \"titular\": \"Test User\",
    \"cedula\": \"1234567890\",
    \"tipo\": \"Credito\",
    \"telefono\": \"0987654321\"
  }" | grep -o '"identificador":"[^"]*' | cut -d'"' -f4)

echo "ID: $ID"

# 3. Consultar tarjeta
curl -s http://localhost:8080/api/cards/$ID | jq .
```

---

## 🛑 Errores Comunes

### Error: "PAN inválido"
- ✅ Verificar: 16-19 dígitos
- ✅ No espacios ni caracteres especiales

### Error: "Operación inválida" al enrolar
- ✅ Verificar identificador es correcto
- ✅ Verificar número de validación coincide
- ✅ (No devuelve detalle por seguridad)

### Error: "Operación inválida" al crear transacción
- ✅ Verificar tarjeta está ENROLADA
- ✅ Verificar identificador existe
- ✅ Verificar referencia no está duplicada

### Error: "Operación inválida" al anular
- ✅ Verificar referencia existe
- ✅ Verificar hace < 5 minutos (si > 5 min: 409)
- ✅ Verificar identificador es correcto

---

## 🔗 Acceso

| Servicio | URL |
|----------|-----|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| H2 Console | http://localhost:8080/h2-console |
| Health | http://localhost:8080/actuator/health |

---

## 📞 Quick FAQ

**¿Dónde está el PAN?**
→ Nunca en respuestas. Solo panEnmascarado (6+****+4)

**¿Cómo se genera identificador?**
→ SHA256(PAN + fecha actual)

**¿Cómo enrolar?**
→ POST /api/cards/enrol con identificador + numeroValidacion

**¿Máximo de transacciones?**
→ Sin límite. Solo: enrolada + referencia única

**¿Anular después de 5 minutos?**
→ NO. Retorna 409 "Operación inválida"

**¿Cómo sé si tarjeta existe?**
→ No puedes. Respuesta genérica 404 (por seguridad)

---

## 🎓 Conceptos Clave

1. **Identificador único**: SHA256(PAN + fecha) - se usa en lugar de PAN
2. **Número de validación**: Aleatorio 1-100, solo válido al crear
3. **Estados**: CREADA → ENROLADA → (transacciones) → INACTIVE (borrado)
4. **Seguridad**: Mensajes genéricos + logs internos detallados
5. **Referencia única**: 6 dígitos, no se repite en la BD

---
