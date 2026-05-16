# Informe tecnico final

**Proyecto:** Plataforma fintech de gestion de billeteras digitales y analitica de transacciones.
**Materia:** Estructuras de Datos 2026-1.

---

## 1. Descripcion del problema

Las aplicaciones financieras digitales actuales (Nequi, Daviplata,
Mercado Pago) permiten a millones de usuarios mover dinero, separar
fondos en bolsillos, programar pagos, recibir alertas y analizar
patrones de uso. Estas plataformas manejan volumenes muy altos de
informacion y deben responder en milisegundos.

El proyecto pide construir una version academica de ese sistema, no
solo funcional, sino que **justifique tecnicamente** cuales estructuras
de datos resuelven cada problema y por que. La organizacion eficiente,
busqueda rapida, priorizacion por fecha, reversion, clasificacion por
rango y analisis de relaciones financieras son las necesidades
centrales que el sistema debe cubrir.

---

## 2. Objetivos cumplidos

El sistema entrega las siguientes capacidades, todas operativas tanto
en backend como en interfaz:

- Registro y administracion de usuarios.
- Multi-billetera por usuario con cinco categorias (Ahorro, Gastos
  diarios, Compras, Transporte, Inversion).
- Recargas, retiros, transferencias internas y externas.
- Historial de movimientos por billetera y por usuario.
- Reversion de operaciones (deshacer ultima o por id).
- Programacion de operaciones futuras con ejecucion automatica de las
  vencidas.
- Sistema de puntos por operacion y niveles automaticos
  (Bronce, Plata, Oro, Platino).
- Ranking por puntos con consultas por rango.
- Buzon de notificaciones FIFO por usuario que reacciona a eventos del
  sistema (bienvenida, saldo bajo, ascenso de nivel, operacion
  rechazada, programadas, fraude).
- Red dirigida y ponderada de transferencias entre usuarios con BFS,
  amigos de amigos, camino mas corto, rutas frecuentes y deteccion de
  ciclos.
- Tablero de analitica con top usuarios y billeteras activas,
  distribuciones por tipo, monto movilizado en rango temporal y top
  transacciones por valor.
- Deteccion automatica de patrones inusuales (rafagas, fragmentacion,
  montos atipicos, mismo destino repetido) con historial de auditoria
  y alerta automatica al usuario afectado.

---

## 3. Arquitectura

```
billetera-fintech/
├── backend/        Java 17 + Spring Boot 3, puerto 8081
│   └── com.fintech.billetera/
│       ├── BilleteraApplication
│       ├── config/           CORS y manejo global de errores
│       ├── domain/           Entidades + enums
│       ├── dto/              Objetos de entrada HTTP
│       ├── repository/       Persistencia en memoria
│       ├── service/          Logica de negocio
│       └── controller/       Endpoints REST
└── frontend/       React 18 + Vite + Tailwind, puerto 5173
    └── src/
        ├── pages/            Una por seccion del menu
        ├── components/       Modales, campanita, historial
        ├── api/              Cliente axios
        └── utils/            Formatos y mapas de color
```

El backend expone una API REST bajo `/api/...`. El frontend la consume
desde axios. No hay base de datos: todos los repositorios viven en
estructuras de datos en memoria, que es justamente lo que el proyecto
busca demostrar.

---

## 4. Estructuras de datos elegidas

| Modulo | Estructura | Justificacion clave | Complejidad operacion principal |
|---|---|---|---|
| Repositorios por id | HashMap | Acceso O(1) por id es la operacion mas frecuente | `get/put`: O(1) |
| Historial de transacciones | LinkedList con `addFirst` | Insercion al inicio O(1) vs O(n) en ArrayList | `addFirst`: O(1) |
| Reversion (deshacer) | ArrayDeque como Pila | LIFO natural; ArrayDeque mejor que `Stack` legacy | `push/pop`: O(1) |
| Operaciones programadas | PriorityQueue (heap) | Ordenar por fecha de ejecucion sin ordenar todo el conjunto | `offer`: O(log n), `peek`: O(1) |
| Notificaciones por usuario | LinkedList como Cola FIFO | Procesar mas antiguas primero (`offer`/`poll`) | O(1) por operacion |
| Ranking de fidelizacion | TreeMap (arbol rojo-negro) | Consultas por rango de puntos con `subMap` | O(log n + k) |
| Red de transferencias | Grafo dirigido ponderado (lista de adyacencia) | Espacio O(V+E) en grafo esparso; recorridos BFS/DFS eficientes | BFS/DFS O(V+E) |
| Top transacciones por valor | TreeSet con `Comparator` | Mantiene orden por monto durante la insercion | `add`: O(log n) |
| Historial de auditoria | LinkedList con `addFirst` | Cronologico inverso, mismo razonamiento que historial | `addFirst`: O(1) |

La justificacion detallada de cada estructura, sus alternativas
descartadas y la complejidad de todas sus operaciones esta en
[ESTRUCTURAS_DE_DATOS.md](ESTRUCTURAS_DE_DATOS.md).

---

## 5. Diagrama de clases

Los diagramas en formato Mermaid estan en
[DIAGRAMA_CLASES.md](DIAGRAMA_CLASES.md) e incluyen:

- Vista general de capas y dependencias entre servicios/repositorios.
- Diagrama de clases del dominio principal con cardinalidades.
- Enumeraciones (tipos y estados).
- Detalle de servicios y sus colaboradores.
- Mapa estructura de datos a clase que la encapsula.

---

## 6. Algoritmos no triviales

### 6.1 Pila de reversion con marcado en lugar de pop inmediato
Cuando se revierte una transaccion no se saca de la pila al instante:
se marca como `REVERTIDA` y, en el siguiente `deshacerUltima`, el
servicio descarta las ya revertidas hasta encontrar una valida. Asi se
pueden hacer reversiones repetidas sin perder el orden cronologico.

### 6.2 PriorityQueue con listado ordenado bajo demanda
La PriorityQueue solo garantiza el orden en la cabeza, no al iterar.
Para mostrar todas las operaciones pendientes ordenadas, se hace una
copia y se ordena (`new ArrayList(cola); Collections.sort(lista)`),
gastando O(n log n) solo cuando el frontend pide el listado.

### 6.3 TreeMap con `Set` como valor
El ranking usa `TreeMap<Integer, Set<String>>` para tolerar empates en
puntos. La consulta por rango usa `subMap(min, max)` y devuelve los IDs
en O(log n + k).

### 6.4 BFS por nivel y "amigos de amigos"
El grafo expone `bfsPorNivel(origen, profundidad)` que devuelve un
mapa `nivel -> IDs alcanzados`. "Amigos de amigos" es simplemente
extraer el nivel 2 de ese mapa.

### 6.5 Deteccion de ciclos con DFS de tres colores
Cada nodo del grafo arranca BLANCO. Al entrar en su DFS recursivo pasa
a GRIS, y al terminar pasa a NEGRO. Si durante el recorrido se
encuentra una arista a un nodo GRIS (ya en pila de recursion), hay un
ciclo y se reconstruye con la tabla de padres.

### 6.6 Reglas de fraude con ventanas temporales
`FraudeService.analizar(transaccion)` se ejecuta justo despues de
finalizar una transaccion. Filtra el historial reciente del usuario
por una ventana temporal (5 o 10 minutos segun la regla) y cuenta
ocurrencias. Si alguna regla pasa el umbral, sube el `NivelRiesgo` de
la transaccion, registra un `EventoAuditoria` y encola una notificacion
de fraude (reutilizando la cola FIFO).

---

## 7. Casos de prueba

El plan de pruebas manual completo, paso a paso por modulo, esta en
[INSTRUCCIONES_PRUEBAS.md](INSTRUCCIONES_PRUEBAS.md). Cubre:

1. Setup inicial (levantar backend y frontend).
2. Gestion de usuarios y billeteras.
3. Operaciones financieras.
4. Reversion (pila).
5. Sistema de puntos y niveles (TreeMap).
6. Operaciones programadas (PriorityQueue).
7. Notificaciones (cola FIFO).
8. Red de transferencias (grafo).
9. Analitica.
10. Deteccion de patrones inusuales (auditoria).

Cada seccion incluye los pasos para activar cada feature y lo que se
espera observar visualmente.

---

## 8. Cobertura de requisitos del enunciado

| Seccion del enunciado | Cubierto |
|---|---|
| 4.1 Gestion de usuarios y billeteras | Si |
| 4.2 Operaciones financieras basicas | Si |
| 4.3 Operaciones programadas | Si |
| 4.4 Sistema de recompensas | Si |
| 4.5 Niveles de usuario | Si |
| 4.6 Reversion de operaciones | Si |
| 4.7 Alertas y notificaciones | Si |
| 4.8 Analitica de movimientos | Si |
| 4.9 Deteccion de comportamiento inusual | Si |
| 5.1 Listas | Si (historiales, auditoria) |
| 5.2 Pilas | Si (reversion) |
| 5.3 Colas y colas de prioridad | Si (notificaciones, programadas) |
| 5.4 Arboles | Si (TreeMap ranking, TreeSet top valor) |
| 5.5 Tablas hash | Si (todos los repositorios principales) |
| 5.6 Grafos | Si (red de transferencias) |
| 6. Requisitos funcionales (12 items) | Si |
| 7. Requisitos no funcionales | Si |
| 8. Recalcular puntos al revertir | Si |
| 8. Usuario mas activo en periodo | Si (analitica) |
| 8. Ciclos en grafo | Si |
| 8. Top transacciones por valor con estructura ordenada | Si (TreeSet) |
| 8. Simulacion de ejecucion automatica de programadas | Si |
| 9. Codigo fuente completo | Si |
| 9. Diagrama de clases | Si ([DIAGRAMA_CLASES.md](DIAGRAMA_CLASES.md)) |
| 9. Descripcion del problema | Si (este informe, seccion 1) |
| 9. Explicacion de estructuras | Si ([ESTRUCTURAS_DE_DATOS.md](ESTRUCTURAS_DE_DATOS.md)) |
| 9. Justificacion de cada estructura | Si (mismo doc) |
| 9. Casos de prueba | Si ([INSTRUCCIONES_PRUEBAS.md](INSTRUCCIONES_PRUEBAS.md)) |
| 9. Informe final tecnico | Este documento |

---

## 9. Conclusiones

El proyecto demuestra que la eleccion correcta de la estructura de
datos cambia radicalmente la viabilidad de una operacion. Un mismo
problema (mantener un historial) se resuelve con LinkedList o
ArrayList con costos de insercion al inicio que difieren en un orden
de magnitud. Un ranking por puntos puede usar HashMap (O(n) por rango)
o TreeMap (O(log n + k)). La eleccion no es estilistica: es lo que
permite o no que el sistema escale.

Por otro lado, el proyecto evidencia el valor de **reutilizar
estructuras existentes** desde modulos nuevos. La cola FIFO de
notificaciones, pensada para "saldo bajo" y "ascenso de nivel",
termino siendo el canal natural por donde el modulo de fraude avisa
al usuario sin tener que disenar un mecanismo paralelo. El grafo, que
nacio para "red de transferencias", pudo haberse extendido facilmente
para alimentar al detector de fraude (por ejemplo, marcando como
sospechosos los ciclos cortos). Esa composabilidad solo es posible
cuando cada modulo expone una interfaz limpia.

El proyecto cierra cumpliendo el 100% de los requisitos del enunciado,
con todas las estructuras solicitadas implementadas y justificadas, y
con interfaz funcional para probarlas.
