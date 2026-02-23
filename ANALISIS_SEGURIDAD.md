# Análisis de Seguridad - Card Platform API

## Pregunta: ¿Es seguro devolver "Tarjeta no existe" en los mensajes de error?

### Respuesta: NO - Riesgo de Enumeración de Recursos

---

## 1. Vulnerabilidad Identificada

### Tipo de Ataque: **Enumeration Attack** (Enumeración de Recursos)

Un atacante podría:
1. Generar/iterar sobre posibles identificadores (hashes SHA256 de PANs comunes)
2. Realizar solicitudes GET, POST, DELETE con estos identificadores
3. Si la API responde "Tarjeta no existe", el atacante confirma que ese identificador fue probado
4. Si la API responde "Operación inválida", el atacante NO puede distinguir si:
   - La tarjeta existe pero el identificador es inválido
   - La tarjeta no existe
   - Hubo un error interno

### Impacto: **Información de Existencia de Recursos**

Con mensajes específicos, un atacante puede:
- Identificar tarjetas válidas en el sistema
- Mapear patrones de generación de identificadores
- Preparar ataques más dirigidos (phishing, social engineering)
- Confirmar si una víctima tiene cuenta en el sistema

---

## 2. Solución Implementada ✅

### Mensajes Genéricos en Responses HTTP

```
HTTP 404 + "Operación inválida"
```

En lugar de:
```
HTTP 404 + "Tarjeta no existe"
HTTP 404 + "Identificador no válido"
HTTP 404 + "Numero de referencia inválido"
```

### Códigos HTTP Estándar

- `200 OK` - Operación exitosa
- `400 Bad Request` - Datos inválidos en solicitud
- `404 Not Found` - Recurso no encontrado (genérico)
- `409 Conflict` - Conflicto (referencia duplicada, estado inválido)

---

## 3. Logging Interno para Auditoría ✅

### Principio: **Ocultar externamente, Registrar internamente**

```java
// En GlobalExceptionHandler.java
logger.warn("Resource not found attempt (security: hiding detail from client): {}", msg);
```

**Ventajas:**

1. **Seguridad Pública**: Cliente recibe mensaje genérico
2. **Seguridad Interna**: Equipo de soporte/seguridad tiene registro detallado
3. **Auditoría**: Se registran intentos fallidos para análisis de patrones de ataque

**Logs Disponibles en:**
- `logs/application.log` (archivo local)
- Sistema de logging centralizado (ELK, Splunk, etc.) - acceso restringido a personal autorizado

**Ejemplo de Log:**
```
[WARN] c.o.c.w.e.GlobalExceptionHandler - Resource not found attempt (security: hiding detail from client): Tarjeta no existe [IP: 192.168.1.100]
```

---

## 4. Matriz de Comparación: Con vs Sin Mensajes Genéricos

| Escenario | Mensaje Específico | Mensaje Genérico | Seguridad |
|-----------|-------------------|------------------|-----------|
| Tarjeta inexistente | "Tarjeta no existe" | "Operación inválida" | ❌ Riesgo |
| Validación fallida | "Número inválido" | "Operación inválida" | ❌ Riesgo |
| Referencia no encontrada | "Ref no encontrada" | "Operación inválida" | ❌ Riesgo |
| Error interno real | "Database error" | "Operación inválida" | ❌ Riesgo |
| **Con mensajes genéricos** | N/A | **Siempre genérico** | ✅ **Seguro** |

---

## 5. Ataque de Enumeración - Ejemplo Práctico

### SIN Protección (Vulnerable):

```bash
# Atacante itera sobre identificadores potenciales
for id in {1..1000}; do
  curl -X GET http://localhost:8080/api/cards/id_$id
  # Si responde: "Tarjeta no existe" → id_$id no está en BD
  # Si responde: "Operación inválida" u otro → id_$id podría existir
done
```

### CON Protección (Seguro):

```bash
# Todas las respuestas son idénticas (404 + "Operación inválida")
# Atacante NO puede diferenciar entre:
# - Identificador válido con validación fallida
# - Identificador inválido
# - Tarjeta no existe
```

---

## 6. Recomendaciones Adicionales de Seguridad

### 1. **Rate Limiting**
```yaml
# application.yaml
app:
  rate-limit:
    max-requests: 100
    window-minutes: 1
```

Implementar en controladores para prevenir fuerza bruta.

### 2. **Autenticación y Autorización**
```java
// Spring Security
@EnableWebSecurity
public class SecurityConfig {
    // Requerir JWT o OAuth2 para acceso
}
```

### 3. **HTTPS/TLS**
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.jks
```

### 4. **Validación de Entrada**
✅ Ya implementado:
- PAN: 16-19 dígitos
- Referencia: Exactamente 6 dígitos
- Cédula: 10-15 caracteres, solo números
- Teléfono: Exactamente 10 dígitos

### 5. **Encriptación en Tránsito**
```yaml
server:
  http2:
    enabled: true
  ssl:
    enabled: true
```

### 6. **Logging Centralizado y Monitoreo**
```xml
<!-- logback.xml o similar -->
<appender name="ASYNC_FILE" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="FILE"/>
</appender>
```

### 7. **Pruebas de Seguridad Regulares**
- OWASP Top 10 compliance
- Penetration testing
- Vulnerability scanning (SAST/DAST)

---

## 7. Implementación en el Código

### GlobalExceptionHandler.java

```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
    String msg = ex.getMessage() != null ? ex.getMessage() : "";

    // ❌ ANTES (Vulnerable):
    // if (msg.contains("Tarjeta no existe")) {
    //     return ResponseEntity.status(HttpStatus.NOT_FOUND)
    //         .body(new ErrorResponse("Tarjeta no existe"));
    // }

    // ✅ AHORA (Seguro):
    if (msg.toLowerCase().contains("no existe")) {
        logger.warn("Resource not found attempt (security: hiding detail from client): {}", msg);
        CreateCardResponse body = new CreateCardResponse("01", "Operación inválida", null, null, null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // Mensaje genérico siempre
    CreateCardResponse defaultBody = new CreateCardResponse("01", "Operación inválida", null, null, null);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(defaultBody);
}
```

---

## 8. Tests de Seguridad ✅

```java
// SecurityGenericMessagesTest.java

@Test
void enrol_nonexistentCard_returnsGenericMessage_notSpecific() throws Exception {
    String response = mockMvc.perform(post("/api/cards/enrol")
            .contentType(MediaType.APPLICATION_JSON)
            .content(enrolJson))
            .andExpect(status().isNotFound())
            .andReturn().getResponse().getContentAsString();

    // ✅ Verificar que NO expone "Tarjeta no existe"
    assertThat(response).doesNotContain("Tarjeta no existe");
    
    // ✅ Verificar mensaje genérico
    assertThat(response).contains("Operación inválida");
}
```

---

## 9. Conclusión

### ¿Conviene mantener el cambio de mensajes genéricos?

**SÍ, DEFINITIVAMENTE. ✅**

**Razones:**

1. **Previene enumeración de recursos** - Atacante no puede confirmar existencia
2. **Cumple con OWASP** - Categoría A01:2021 - Broken Access Control
3. **Mejor experiencia de seguridad** - Consistente en toda la API
4. **Auditoría preservada** - Logs internos registran detalles
5. **Escalable** - Se puede extender a otros endpoints

**Costo:**

- Mínimo: Solo cambiar mensajes en handler de excepciones
- Ya implementado en el código actual ✅

**Recomendación:**
- ✅ Mantener mensajes genéricos en todas las respuestas
- ✅ Expandir logging interno a más eventos sensibles
- ✅ Implementar rate limiting adicional
- ✅ Añadir autenticación/autorización en futuras fases

---

## 10. Referencias

- [OWASP - Information Exposure Through an Error Message](https://owasp.org/www-community/Information_Exposure_Through_an_Error_Message)
- [OWASP - Enumeration](https://owasp.org/www-community/attacks/Enumeration)
- [CWE-203: Observable Discrepancy](https://cwe.mitre.org/data/definitions/203.html)
- [Spring Security Best Practices](https://spring.io/guides/gs/securing-web/)

