# Estructuras de datos del sistema

Este documento explica que estructura se uso en cada modulo, por que se
eligio, y la complejidad de las operaciones criticas.

---

## 1. HashMap — Repositorios principales

**Donde:** `UsuarioRepository`, `BilleteraRepository`,
`TransaccionRepository`, `ProgramacionRepository` (indice secundario).

**Que es:** Tabla hash que mapea claves a valores usando una funcion de
hash. En Java, `HashMap<K, V>` ofrece acceso O(1) promedio para get/put/remove.

**Por que aqui:** El acceso por ID es la operacion mas frecuente del
sistema. Cada vez que se hace una transaccion necesitamos resolver el
usuario y las billeteras involucradas. Un `ArrayList` daria O(n) por
busqueda — inviable cuando el volumen crezca.

**Alternativas que descartamos:**
- `ArrayList<Usuario>`: O(n) para buscar por id.
- `TreeMap<String, Usuario>`: O(log n), pero no necesitamos orden por id.

**Complejidad:**
- `guardar`, `buscarPorId`, `eliminar`, `existe`: O(1) promedio
- `listar` (recorrer todos): O(n)

---

## 2. LinkedList — Historial de transacciones

**Donde:** `TransaccionRepository`, mapas
`historialPorBilletera: Map<String, LinkedList<String>>` y
`historialPorUsuario: Map<String, LinkedList<String>>`.

**Que es:** Lista doblemente enlazada donde cada nodo apunta al anterior y
al siguiente. Permite insercion en O(1) en cualquier extremo si se tiene
referencia al nodo.

**Por que aqui:** Las transacciones se agregan SIEMPRE al inicio (las
mas recientes primero). Esto se hace con `addFirst()` que es O(1) en
LinkedList. En `ArrayList`, `add(0, x)` es O(n) porque tiene que
desplazar todos los elementos un lugar a la derecha.

**Alternativas que descartamos:**
- `ArrayList`: O(n) por insercion al inicio.
- `Deque` con `addFirst`: tambien funcionaria, pero LinkedList expresa
  mas explicitamente que es un historial encadenado.

**Decision de diseno:** En la LinkedList guardamos solo los IDs de las
transacciones, no los objetos completos. Asi evitamos duplicacion en
memoria y mantenemos una sola fuente de verdad (el HashMap principal
`transacciones`). Al consultar el historial, resolvemos los IDs contra el
HashMap.

**Complejidad:**
- `guardar` (registrar nueva transaccion): O(1) — actualiza HashMap +
  addFirst en 1 a 3 LinkedLists.
- `historialBilletera`, `historialUsuario`: O(k) — donde k es el tamano
  del historial filtrado.

---

## 3. ArrayDeque como Pila — Reversion de operaciones

**Donde:** `ReversionRepository`, mapa
`pilasPorUsuario: Map<String, Deque<Transaccion>>`.

**Que es:** Pila (Stack) implementada sobre array dinamico. Las
operaciones `push`, `pop` y `peek` son O(1). Ofrecemos LIFO
(Last-In-First-Out).

**Por que aqui:** Cuando un usuario pide "deshacer mi ultima operacion",
la operacion mas reciente debe ser la primera en revertirse. Esto es
exactamente el comportamiento de una pila.

**Por que ArrayDeque y no Stack:** La clase `java.util.Stack` es legacy.
La documentacion oficial de Java recomienda usar `ArrayDeque` en su lugar
porque (a) es mas rapida — Stack hereda de Vector y tiene overhead de
sincronizacion innecesaria — y (b) tiene una API mas consistente.

**Detalle importante:** Cuando una transaccion se revierte, NO la
sacamos de la pila inmediatamente. La marcamos como `REVERTIDA` y, en
el siguiente `deshacerUltima`, el bucle del servicio la descarta y sigue
buscando hacia abajo hasta encontrar una reversible no revertida. Esto
permite reversiones repetidas sin perder el orden cronologico.

**Complejidad:**
- `apilar`, `peek`, `pop`, `tamano`: O(1).

---

## 4. PriorityQueue — Operaciones programadas

**Donde:** `ProgramacionRepository`, atributo `cola: PriorityQueue<OperacionProgramada>`.

**Que es:** Cola de prioridad implementada como heap binario. El
elemento de mayor prioridad esta siempre en la cabeza, accesible en O(1)
con `peek`. Insertar es O(log n) (heapify-up). Sacar de la cabeza es
O(log n) (heapify-down).

**Por que aqui:** Las operaciones programadas DEBEN ejecutarse en orden
de fecha, no en orden de creacion. Si programo una operacion para
manana y luego programo otra para hoy, la segunda debe ejecutarse
primero al barrer pendientes. La PriorityQueue garantiza esto en
estructura.

**Como definimos la prioridad:** `OperacionProgramada` implementa
`Comparable<OperacionProgramada>` comparando `fechaEjecucion`
ascendentemente. Asi la operacion con la fecha mas cercana es la
"menor", y por lo tanto sale primero del heap.

**Atencion al iterar:** La `PriorityQueue` solo garantiza el orden en la
cabeza. Si la iteramos directamente, el orden interno no es ordenado.
Por eso, para listar TODAS las operaciones programadas en orden, hacemos
una copia y la ordenamos: `List sorted = new ArrayList(cola);
Collections.sort(sorted);`. Esto es O(n log n) pero solo se hace cuando
se pide el listado completo.

**Complejidad:**
- `programar`: O(log n)
- `peekProxima`: O(1)
- `extraerProxima`, `extraerVencidas`: O(log n) por elemento extraido
- `cancelar`: O(n) — `remove` busca linealmente en la cola
- `listarOrdenadasPorFecha`: O(n log n)

---

## 5. TreeMap — Ranking de fidelizacion

**Donde:** `FidelizacionRepository`, atributo
`ranking: TreeMap<Integer, Set<String>>`.

**Que es:** Mapa ordenado implementado como arbol rojo-negro
(self-balancing). Las claves se mantienen en orden segun su Comparable
natural. Acceso, insercion y borrado son O(log n).

**Por que aqui:** El sistema necesita responder consultas como "dame
todos los usuarios entre 1000 y 5000 puntos" eficientemente. Un HashMap
NO sirve aqui porque no mantiene orden — tendriamos que iterar todas
las claves y filtrar (O(n)). Con TreeMap usamos `subMap(min, max)` para
saltar directamente al rango relevante en O(log n + k), donde k es el
numero de claves dentro del rango.

**Diseno del TreeMap:**
- Clave: cantidad de puntos (`Integer`)
- Valor: `Set<String>` con los IDs de usuarios que tienen exactamente
  esa cantidad de puntos.

Usamos un `Set` como valor porque puede haber multiples usuarios con la
misma cantidad de puntos.

**Por que no SortedSet de Usuario:** Funcionaria, pero perderiamos
acceso O(log n) a "todos los usuarios con N puntos" que es util cuando
varios empatan.

**Sincronizacion con cambios:** Cada vez que un usuario gana o pierde
puntos (transaccion exitosa, reversion), llamamos
`actualizarRanking(idUsuario, puntosAntes, puntosDespues)` que saca al
usuario del bucket viejo y lo mete en el nuevo. Eso es O(log n) porque
involucra dos operaciones logaritmicas en el arbol.

**Complejidad:**
- `actualizarRanking`: O(log n)
- `usuariosEnRango(min, max)`: O(log n + k) — k claves dentro del rango
- `topN(n)`: O(n) — iterando descendingMap hasta tomar n usuarios

---

## 6. LinkedList como Cola FIFO — Buzon de notificaciones

**Donde:** `NotificacionRepository`, atributo
`buzones: Map<String, LinkedList<Notificacion>>` (un buzon por usuario)
e indice global `porId: Map<String, Notificacion>` para acceso O(1) al
marcar leidas.

**Que es:** Una Cola (Queue) FIFO mantiene el orden de llegada: el
primer elemento en entrar es el primero en salir. Java implementa la
interfaz `Queue` con metodos `offer` (encolar al final) y `poll` (sacar
del frente), ambos O(1) sobre `LinkedList`.

**Por que aqui:** Las notificaciones se procesan en el orden en que
ocurrieron (las mas antiguas primero), no en el orden inverso al
historial. Cuando el frontend pide "despachar" el buzon, drena de la
cabeza hacia la cola — comportamiento exacto de una cola FIFO.

**Por que LinkedList y no ArrayDeque:** Ambas implementan `Queue` y
ambas dan O(1) en `offer/poll`. ArrayDeque es ligeramente mas eficiente
en memoria, pero LinkedList expresa mejor el concepto academico de
"cola encadenada" y permite iterar snapshots ordenados (`new
ArrayList(cola)`) sin perder el orden FIFO. La diferencia practica es
despreciable para el volumen del proyecto.

**Diferencia clave con la LinkedList de historial (seccion 2):** En el
historial usamos `addFirst()` porque el orden deseado es LIFO visual
(mas reciente arriba). En notificaciones usamos `offer()` (que es
`addLast`) porque el orden deseado es FIFO (procesar primero las que
llevan mas tiempo esperando). La misma clase Java, dos contratos
distintos segun la operacion que invoquemos.

**Emisores del sistema:** El servicio facade `NotificacionService` es
llamado por:
- `UsuarioService.registrar` → BIENVENIDA
- `TransaccionService.finalizarYRegistrar` → SALDO_BAJO (si la
  billetera origen quedo bajo umbral) y ASCENSO_NIVEL (si el usuario
  cambio de nivel)
- `TransaccionService` validaciones → OPERACION_RECHAZADA (saldo
  insuficiente, monto invalido, billetera inactiva)
- `ProgramacionService.ejecutarUna` → PROGRAMADA_EJECUTADA o
  PROGRAMADA_FALLIDA segun resultado

**Complejidad:**
- `encolar` (offer): O(1) — `HashMap.get` + `LinkedList.addLast`
- `desencolarSiguiente` (poll): O(1)
- `pico` (peek): O(1)
- `pendientes`: O(k) — recorre la cola contando no leidas
- `marcarLeida(id)`: O(1) — usa el indice `porId`
- `drenarPendientes` (despachar todo): O(k)
- `eliminar(id)`: O(k) — `LinkedList.remove(Object)` recorre

---

## 7. Grafo dirigido y ponderado — Red de transferencias

**Donde:** `GrafoTransferenciasRepository`, atributo
`adyacencia: Map<String, Map<String, Arista>>`.

**Que es:** Un grafo dirigido (la transferencia va de A hacia B, no
viceversa) y ponderado (cada arista guarda el monto acumulado y el
numero de envios). Los nodos son los IDs de usuario; las aristas
representan que existe al menos una transferencia externa de un
usuario a otro.

**Por que lista de adyacencia y no matriz:** En cualquier sistema real
la red de transferencias es **esparsa** — la mayoria de usuarios solo
se relaciona con unos pocos. Una matriz `V x V` gastaria O(V²) memoria
casi vacia. La lista de adyacencia gasta O(V + E) y permite recorrer
"vecinos de X" en tiempo proporcional a su grado, no a V.

**Diseño con doble HashMap:** El nivel exterior mapea cada origen a
sus aristas salientes. El nivel interior usa un `HashMap<idDestino,
Arista>` (no una `List<Arista>`) porque al registrar una nueva
transferencia necesitamos saber en O(1) si ya existia una arista para
ese par origen-destino y, si existia, acumular el peso en lugar de
crear una nueva.

**La clase Arista:** guarda `pesoTotal` (monto BigDecimal acumulado),
`conteo` (numero de transferencias), y las fechas de la primera y
ultima. Tiene `acumular(monto)` y `restar(monto)` para los flujos de
transferencia y reversion.

**Sincronizacion con el resto del sistema:**
- `TransaccionService.transferir` llama a
  `grafoService.registrarTransferencia` solo cuando la transferencia
  es `EXTERNA` (entre usuarios distintos).
- `ReversionService.revertir` llama a
  `grafoService.revertirTransferencia` cuando la transaccion original
  era externa, para que la red refleje el estado actualizado.

**Algoritmos implementados:**
- **BFS por nivel** desde un usuario hasta una profundidad dada
  (cola FIFO con `ArrayDeque.offer/poll`).
- **Amigos de amigos**: BFS de profundidad 2, devuelve solo el
  segundo nivel (excluye al propio usuario y a sus vecinos directos).
- **Camino mas corto** entre dos usuarios: BFS con tabla de padres
  para reconstruir la ruta.
- **Top rutas frecuentes**: ordena todas las aristas por peso total
  descendente, desempata por numero de transferencias.
- **Deteccion de ciclos**: DFS con tres colores
  (BLANCO/GRIS/NEGRO). Si durante el recorrido se encuentra una
  arista a un nodo GRIS (en pila de recursion), hay ciclo y se
  reconstruye usando la tabla de padres.

**Complejidad:**
- `registrarTransferencia(o, d, m)`: O(1) promedio
- `revertirTransferencia(o, d, m)`: O(1) promedio
- `vecinos(idUsuario)`: O(grado(u))
- `bfsPorNivel(origen, prof)`: O(V + E) en el peor caso
- `amigosDeAmigos(usuario)`: O(V + E)
- `caminoMasCorto(o, d)`: O(V + E) (BFS sin pesos)
- `rutasFrecuentes(topN)`: O(E log E) — ordena todas las aristas
- `detectarCiclos()`: O(V + E) — DFS unico con coloreo

---

## Resumen visual de la asignacion

| Modulo                       | Estructura     | Justificacion clave             |
|------------------------------|----------------|---------------------------------|
| Repositorios por id          | HashMap        | Acceso O(1)                     |
| Historial de transacciones   | LinkedList     | addFirst O(1)                   |
| Reversion (deshacer)         | ArrayDeque     | LIFO O(1)                       |
| Operaciones programadas      | PriorityQueue  | Orden por fecha (heap)          |
| Notificaciones por usuario   | LinkedList Q   | FIFO offer/poll O(1)            |
| Ranking de fidelizacion      | TreeMap        | Consultas por rango O(log n+k)  |
| Red de transferencias        | Grafo (lista)  | BFS/DFS O(V+E), espacio O(V+E)  |
