#!/bin/bash

# Script de prueba para Card Platform API
# Uso: ./test_api.sh

set -e

echo "╔════════════════════════════════════════════════════════╗"
echo "║         Card Platform API - Test Script                ║"
echo "╚════════════════════════════════════════════════════════╝"

# Detener cualquier instancia anterior
echo "🛑 Deteniendo instancias previas..."
pkill -f "spring-boot:run" 2>/dev/null || true
pkill -f "java.*card-platform" 2>/dev/null || true
sleep 3

# Iniciar la aplicación
echo ""
echo "🚀 Iniciando aplicación..."
cd /Users/oirada/IdeaProjects/card-platform
timeout 120 ./mvnw spring-boot:run -q > /tmp/card-platform.log 2>&1 &
APP_PID=$!
echo "   PID: $APP_PID"

# Esperar a que inicie
echo "⏳ Esperando que la aplicación inicie (20 segundos)..."
sleep 20

# Verificar que está corriendo
if ! curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "❌ ERROR: La aplicación no inició correctamente"
    echo "   Últimos logs:"
    tail -20 /tmp/card-platform.log
    exit 1
fi

echo "✅ Aplicación iniciada correctamente"
echo ""

# Test 1: Crear tarjeta
echo "📋 Test 1: Crear tarjeta"
echo "   POST /api/cards"

RESPONSE=$(curl -s -X POST http://localhost:8080/api/cards \
  -H "Content-Type: application/json" \
  -d '{
    "pan":"4111111111111113",
    "titular":"Juan Perez",
    "cedula":"1712345678",
    "tipo":"Debito",
    "telefono":"0987654321"
  }')

echo "   Response: $RESPONSE"

# Extraer valores
CODIGO=$(echo $RESPONSE | grep -o '"codigo":"[^"]*' | cut -d'"' -f4)
IDENTIFICADOR=$(echo $RESPONSE | grep -o '"identificador":"[^"]*' | cut -d'"' -f4)
NUMERO_VALIDACION=$(echo $RESPONSE | grep -o '"numeroValidacion":[0-9]*' | cut -d':' -f2)

if [ "$CODIGO" = "00" ]; then
    echo "   ✅ Status: ÉXITO"
    echo "   📍 Identificador: $IDENTIFICADOR"
    echo "   🔐 Número de validación: $NUMERO_VALIDACION"
else
    echo "   ❌ Status: FALLÓ (código: $CODIGO)"
    kill $APP_PID 2>/dev/null || true
    exit 1
fi

echo ""

# Test 2: Enrolar tarjeta
echo "📋 Test 2: Enrolar tarjeta"
echo "   POST /api/cards/enrol"

ENROL_RESPONSE=$(curl -s -X POST http://localhost:8080/api/cards/enrol \
  -H "Content-Type: application/json" \
  -d "{
    \"identificador\":\"$IDENTIFICADOR\",
    \"numeroValidacion\":$NUMERO_VALIDACION
  }")

echo "   Response: $ENROL_RESPONSE"

ENROL_CODIGO=$(echo $ENROL_RESPONSE | grep -o '"codigo":"[^"]*' | cut -d'"' -f4)

if [ "$ENROL_CODIGO" = "00" ]; then
    echo "   ✅ Status: ÉXITO"
else
    echo "   ❌ Status: FALLÓ (código: $ENROL_CODIGO)"
fi

echo ""

# Test 3: Crear transacción
echo "📋 Test 3: Crear transacción"
echo "   POST /api/transactions"

TX_RESPONSE=$(curl -s -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d "{
    \"identificador\":\"$IDENTIFICADOR\",
    \"referencia\":\"123456\",
    \"total\":99.99,
    \"direccion\":\"Av Principal 123\"
  }")

echo "   Response: $TX_RESPONSE"

TX_CODIGO=$(echo $TX_RESPONSE | grep -o '"codigo":"[^"]*' | cut -d'"' -f4)

if [ "$TX_CODIGO" = "00" ]; then
    echo "   ✅ Status: ÉXITO"
else
    echo "   ❌ Status: FALLÓ (código: $TX_CODIGO)"
fi

echo ""

# Test 4: Consultar tarjeta
echo "📋 Test 4: Consultar tarjeta"
echo "   GET /api/cards/$IDENTIFICADOR"

GET_RESPONSE=$(curl -s -X GET http://localhost:8080/api/cards/$IDENTIFICADOR)

echo "   Response: $GET_RESPONSE"

if echo $GET_RESPONSE | grep -q "panEnmascarado"; then
    echo "   ✅ Status: ÉXITO"
else
    echo "   ❌ Status: FALLÓ"
fi

echo ""
echo "🧹 Limpiando..."
kill $APP_PID 2>/dev/null || true

echo ""
echo "╔════════════════════════════════════════════════════════╗"
echo "║              ✅ PRUEBAS COMPLETADAS                     ║"
echo "╚════════════════════════════════════════════════════════╝"

