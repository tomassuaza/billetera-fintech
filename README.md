# Billetera Fintech

Plataforma fintech de billeteras digitales para el proyecto final de
**Estructuras de Datos 2026-1**.

## Estado actual: Dia 7 completo

### Modulos implementados
- ✅ Gestion de usuarios (registro, listado, eliminacion)
- ✅ Multi-billetera por usuario con tipos categoricos
- ✅ Transacciones: recargar, retirar, transferir (interna y externa)
- ✅ Reversion de operaciones (deshacer ultima)
- ✅ Operaciones programadas (con ejecucion automatica de vencidas)
- ✅ Sistema de puntos y niveles de fidelizacion
- ✅ Ranking con consultas por rango de puntos

### Modulos pendientes (dias 8-15)
- ⏳ Sistema de notificaciones (Cola)
- ⏳ Grafo de transferencias entre usuarios
- ⏳ Analitica avanzada (top usuarios, billeteras, etc.)
- ⏳ Deteccion de fraude por reglas y patrones
- ⏳ Tests unitarios y benchmarks
- ⏳ Informe tecnico final

## Stack
- **Backend:** Java 17 + Spring Boot 3.2.5 (puerto 8080)
- **Frontend:** React 18 + Vite + Tailwind CSS (puerto 5173)
- **Persistencia:** En memoria (HashMap, LinkedList, ArrayDeque, PriorityQueue, TreeMap)

## Como correr

### 1. Backend
```bash
cd backend
mvn spring-boot:run
```
Quedara en http://localhost:8080

### 2. Frontend
```bash
cd frontend
npm install
npm run dev
```
Quedara en http://localhost:5173

## Documentacion
- `docs/ESTRUCTURAS_DE_DATOS.md` — explicacion de cada estructura usada
- `docs/INSTRUCCIONES_DIA_7.md` — guia de pruebas para esta entrega
