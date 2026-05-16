# Billetera Fintech

Plataforma fintech de billeteras digitales para el proyecto final de
**Estructuras de Datos 2026-1**. Simula una aplicacion tipo Nequi o
Daviplata: cada usuario administra varias billeteras, hace recargas,
retiros y transferencias, programa operaciones futuras, gana puntos,
recibe notificaciones y entra en un sistema de auditoria que detecta
patrones inusuales.

## Modulos implementados

- Gestion de usuarios (registro, listado, eliminacion).
- Multiples billeteras por usuario con tipos categoricos.
- Operaciones financieras: recarga, retiro, transferencia interna y
  externa.
- Reversion de operaciones con pila por usuario.
- Operaciones programadas con ejecucion automatica de vencidas.
- Sistema de puntos y niveles de fidelizacion (Bronce, Plata, Oro,
  Platino) con ranking por TreeMap.
- Notificaciones en cola FIFO por usuario (bienvenida, saldo bajo,
  ascenso de nivel, operacion rechazada, programadas, fraude).
- Red dirigida y ponderada de transferencias entre usuarios con BFS,
  amigos de amigos, camino mas corto, rutas frecuentes y deteccion de
  ciclos.
- Analitica de movimientos: top usuarios y billeteras activas,
  distribuciones por tipo y categoria, monto movilizado en rango
  temporal, top transacciones por valor.
- Deteccion automatica de patrones inusuales (rafagas,
  fragmentacion, montos atipicos, mismo destino repetido) con
  historial de auditoria y alerta automatica al usuario.

## Stack

- **Backend:** Java 17 + Spring Boot 3.2.5 (puerto 8081)
- **Frontend:** React 18 + Vite + Tailwind CSS (puerto 5173)
- **Persistencia:** en memoria. HashMap, LinkedList (LIFO y FIFO),
  ArrayDeque, PriorityQueue, TreeMap, TreeSet, lista de adyacencia.

## Como correr

### Backend
```bash
cd backend
mvn spring-boot:run
```
Queda en `http://localhost:8081`.

### Frontend (en otra terminal)
```bash
cd frontend
npm install
npm run dev
```
Queda en `http://localhost:5173`.

## Documentacion

- [docs/INFORME_TECNICO.md](docs/INFORME_TECNICO.md) — informe final
  con descripcion del problema, arquitectura, justificacion de
  decisiones y cobertura del enunciado.
- [docs/ESTRUCTURAS_DE_DATOS.md](docs/ESTRUCTURAS_DE_DATOS.md) —
  explicacion y justificacion tecnica de cada estructura.
- [docs/DIAGRAMA_CLASES.md](docs/DIAGRAMA_CLASES.md) — diagramas de
  clases y de capas en formato Mermaid.
- [docs/INSTRUCCIONES_PRUEBAS.md](docs/INSTRUCCIONES_PRUEBAS.md) —
  manual de pruebas paso a paso por modulo.
