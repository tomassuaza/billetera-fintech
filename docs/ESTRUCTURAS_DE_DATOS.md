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

## Resumen visual de la asignacion

| Modulo                       | Estructura     | Justificacion clave             |
|------------------------------|----------------|---------------------------------|
| Repositorios por id          | HashMap        | Acceso O(1)                     |
| Historial de transacciones   | LinkedList     | addFirst O(1)                   |
| Reversion (deshacer)         | ArrayDeque     | LIFO O(1)                       |
| Operaciones programadas      | PriorityQueue  | Orden por fecha (heap)          |
| Ranking de fidelizacion      | TreeMap        | Consultas por rango O(log n+k)  |

---

## Pendientes para los proximos dias (8-15)

- **Cola (`ArrayDeque` como Queue) + Notificaciones** (dia 9) — buzon
  por usuario con `offer` y `poll`.
- **Grafo dirigido ponderado** (dia 10) — red de transferencias entre
  usuarios. Lista de adyacencia con `HashMap<Usuario, List<Arista>>`.
  Operaciones: BFS para "amigos de amigos", deteccion de ciclos.
- **Analitica** (dia 11) — top de usuarios mas activos, billeteras con
  mas transacciones, etc.
- **Deteccion de fraude** (dia 11) — reglas tipo "demasiadas
  transferencias en corto tiempo" usando ventanas temporales sobre los
  historiales.
