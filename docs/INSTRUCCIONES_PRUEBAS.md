# Manual de pruebas del sistema

Documento de referencia para verificar manualmente el funcionamiento de
todos los modulos de la plataforma. Cada seccion cubre una capacidad
del sistema con un plan de pruebas paso a paso.

---

## 1. Setup inicial

### 1.1 Estructura del proyecto

```
billetera-fintech/
├── backend/        Spring Boot 3 + Java 17 + Maven
├── frontend/       React + Vite + Tailwind
└── docs/           Documentacion tecnica y manual de pruebas
```

### 1.2 Levantar el backend

```bash
cd backend
mvn spring-boot:run
```

Espera el mensaje:

```
Started BilleteraApplication in X.XXX seconds
Tomcat started on port 8081
```

Queda escuchando en `http://localhost:8081`.

### 1.3 Levantar el frontend

En otra terminal:

```bash
cd frontend
npm install   # solo la primera vez
npm run dev
```

Queda en `http://localhost:5173`. El frontend tiene **hot reload**: al
modificar un archivo `.jsx` el navegador se refresca solo.

### 1.4 Notas de macOS

Si descomprimes el proyecto desde un zip en `~/Downloads`, macOS puede
crear una carpeta `billetera-fintech 2` si ya existia una. Trabajar
siempre sobre la version mas reciente y borrar las duplicadas.

---

## 2. Gestion de usuarios y billeteras

### Crear datos base
1. Abre `http://localhost:5173`
2. Registra dos usuarios (ej. "Juan Lopez", "Maria Garcia")
3. Entra al detalle de Juan, crea dos billeteras: una de **Ahorro** y
   una de **Gastos diarios**
4. Entra al detalle de Maria, crea una billetera de **Compras**

### Activar / desactivar billeteras
5. Desde el detalle del usuario, click en "Desactivar" en una billetera
6. Intenta operar contra ella → debe rechazar y generar notificacion
   `OPERACION_RECHAZADA`
7. Vuelve a activarla y reintenta

---

## 3. Operaciones financieras

### Recarga, retiro y transferencia
1. Abre el detalle de la billetera de Ahorro de Juan
2. **Recarga** $50.000 → veras el saldo y la transaccion en el historial
3. Recarga otra vez $30.000 → 2 transacciones en historial
4. Intenta **retirar** mas de lo que hay → debe fallar y generar
   notificacion de operacion rechazada
5. **Transfiere** desde Ahorro de Juan a Gastos diarios de Juan
   ($20.000) → debe quedar como `TRANSFERENCIA_INTERNA`
6. **Transfiere** desde Ahorro de Juan a Compras de Maria ($15.000) →
   debe quedar como `TRANSFERENCIA_EXTERNA`

### Verificacion visual
- El historial siempre muestra mas reciente arriba (LinkedList con
  `addFirst`).
- Cada operacion exitosa aumenta los puntos del usuario que la genera.

---

## 4. Reversion de operaciones (pila)

1. Vuelve al detalle de Juan. Veras "Deshacer ultima (N)" donde N es
   la cantidad de operaciones reversibles
2. Click en deshacer → la ultima transaccion se revierte y se crea un
   movimiento de tipo `REVERSION` en el historial. El saldo se ajusta.
3. Las operaciones revertidas aparecen tachadas en el historial.
4. Verifica que los puntos del usuario disminuyen al revertir (la pila
   de reversion es LIFO sobre `ArrayDeque`).

---

## 5. Sistema de puntos y niveles (TreeMap)

1. Mira el contador de puntos del usuario tras varias operaciones
   - Recarga: 1pt / 100 unidades
   - Retiro: 2pt / 100 unidades
   - Transferencia: 3pt / 100 unidades
   - Bono por programada ejecutada: +10 pts
2. Ve a la pagina **Ranking** desde el menu — el usuario con mas puntos
   aparece arriba
3. Prueba el filtro por rango (ej. min=0, max=500) — usa `subMap` del
   TreeMap por debajo: O(log n + k)
4. Verifica el **conteo por nivel** (Bronce 0-500, Plata 501-1000, Oro
   1001-5000, Platino +5000)
5. Confirma que el nivel del usuario cambia al cruzar cada umbral —
   genera notificacion automatica `ASCENSO_NIVEL`

---

## 6. Operaciones programadas (PriorityQueue)

1. Ve a **Programadas** desde el menu
2. Programa una **RECARGA** para fecha **pasada** (ej. ayer)
3. Programa otra **RECARGA** para fecha **futura** (ej. dentro de 1 hora)
4. Click en **Ejecutar vencidas** — solo se ejecuta la que ya paso. El
   PriorityQueue mantiene el orden por fecha en la cabeza.
5. La que esta en futuro queda pendiente. Pruebala con el boton
   **Ejecutar** individual
6. Cada ejecucion exitosa:
   - Genera el movimiento real en el historial
   - Suma el bono de puntos al usuario
   - Encola una notificacion `PROGRAMADA_EJECUTADA`
7. Si la ejecucion falla (por ej. saldo insuficiente), se marca como
   `FALLIDA` y se encola `PROGRAMADA_FALLIDA`

### Orden en pantalla
Las pendientes aparecen ordenadas por fecha. El PriorityQueue solo
garantiza la cabeza ordenada, asi que el listado completo se obtiene
copiando y ordenando (`Collections.sort`).

---

## 7. Notificaciones (Cola FIFO)

### Bienvenida
1. Registra un usuario nuevo (ej. "Andres Test")
2. Ve a su detalle → veras la **campanita** con un "1" rojo arriba
3. Click en la campanita → aparece la notificacion `BIENVENIDA`

### Saldo bajo
4. Haz un retiro grande que deje la billetera con menos de $10.000
5. Aparece notificacion `SALDO_BAJO` referenciando esa billetera

### Operacion rechazada
6. Intenta retirar mas del saldo disponible o usar una billetera
   inactiva
7. La operacion falla y se encola `OPERACION_RECHAZADA`

### Ascenso de nivel
8. Haz recargas y/o transferencias hasta que el usuario supere los 500
   puntos (BRONCE → PLATA)
9. Aparece notificacion `ASCENSO_NIVEL`

### Pagina global de notificaciones
10. Click en **Notificaciones** en el menu superior
11. Selecciona el usuario en el dropdown → ves todas sus notificaciones
12. Filtra por tipo (ej. solo "Saldo bajo")
13. Click en **Despachar FIFO** → drena todas las no leidas en orden
    de llegada (de la mas antigua a la mas reciente)
14. Las marcadas quedan tenues (opacidad reducida)

### Verificacion del comportamiento FIFO
En la pagina **Notificaciones**, las notificaciones se muestran con la
mas reciente arriba para comodidad visual. La cola interna almacena de
mas antigua a mas reciente: al despachar, se procesan **en orden
inverso al que ves en pantalla**, confirmando el FIFO. El alert
indica cuantas se despacharon.

---

## 8. Red de transferencias (grafo dirigido y ponderado)

La pagina **Red** del menu muestra el grafo dirigido entre usuarios.
Las aristas representan transferencias **externas** (entre usuarios
distintos); las internas no entran al grafo porque mueven dinero
dentro de la misma persona.

### Construir un grafo de prueba
1. Asegurate de tener al menos tres usuarios con billeteras y saldo:
   ej. Juan, Maria, Andres.
2. Desde el detalle de una billetera de Juan, transfiere a una
   billetera de Maria (ej. $20.000). Crea otra transferencia mas
   pequena (ej. $5.000).
3. Desde una billetera de Maria, transfiere a Andres ($10.000).
4. Para crear un **ciclo**, desde Andres transfiere de vuelta a Juan.

### Que verificar en la pagina Red
5. En **Aristas del grafo** veras tres filas (Juan→Maria, Maria→Andres,
   Andres→Juan). La fila Juan→Maria debe tener peso $25.000 y conteo 2
   (las dos transferencias se acumularon en una sola arista).
6. En **Vecinos y amigos de amigos**, selecciona a Juan:
   - Vecinos directos: Maria
   - Amigos de amigos (BFS nivel 2): Andres
7. Cambia el selector a Andres: vecinos directos = Juan, amigos de
   amigos = Maria.
8. En **Camino mas corto**, elige origen=Juan, destino=Andres y click
   en "Buscar camino" — debe devolver `Juan → Maria → Andres` (2 saltos).
9. En **Top rutas frecuentes** veras las aristas ordenadas por peso
   descendente; Juan→Maria debe estar primero.
10. En **Ciclos detectados** debe aparecer `Juan → Maria → Andres → Juan`
    (o equivalente segun el orden del DFS).

### Reversion y consistencia con el grafo
11. Vuelve al detalle de Juan y deshace su ultima transferencia externa.
12. Recarga la pagina Red: el peso y el conteo de la arista
    correspondiente bajan; si el conteo llega a 0, la arista desaparece
    del grafo.

---

## 9. Analitica

La pagina **Analitica** del menu agrupa los reportes que el sistema
genera sobre el conjunto de transacciones. Es un panel de solo lectura.

### Que verificar
1. Crea actividad ejecutando varias recargas, retiros y transferencias
   entre tus usuarios.
2. Abre **Analitica** y revisa:
   - **Frecuencia por tipo**: las barras reflejan cuantas RECARGAS,
     RETIROS y TRANSFERENCIAS hubo. La suma cuadra con el total.
   - **Categorias de billetera**: conteo por tipo (AHORRO,
     GASTOS_DIARIOS, etc).
   - **Top usuarios mas activos** y **Top billeteras mas activas**:
     ranking descendente por numero de transacciones.
   - **Monto movilizado en un rango**: ajusta las fechas y dale
     "Recalcular" — el panel actualiza total, conteo y desglose por tipo.
   - **Top transacciones por valor**: tabla ordenada con un `TreeSet`
     descendente por monto. Tras revertir una transaccion grande
     deberia bajar del top.

---

## 10. Deteccion de patrones inusuales (auditoria)

El modulo de fraude se ejecuta automaticamente cada vez que se crea
una transaccion exitosa. Si alguna regla se activa, marca la
transaccion con un nivel de riesgo, registra un evento en el historial
de auditoria y encola una notificacion `FRAUDE_DETECTADO` al usuario.

### Reglas y como activarlas
1. **Rafaga (NIVEL MEDIO):** haz 4 o mas transferencias del mismo
   usuario en menos de 5 minutos. La cuarta se marca como riesgo MEDIO.
2. **Monto atipico (NIVEL BAJO):** haz primero varias transacciones
   con montos pequenos (ej. $5.000 cada una) y despues una grande
   (ej. $100.000). La grande supera el promedio por mas de 5x.
3. **Mismo destino repetido (NIVEL MEDIO):** envia tres o mas
   transferencias hacia la misma billetera destino en menos de 10
   minutos.
4. **Fragmentacion (NIVEL ALTO):** desde dos billeteras propias
   distintas, envia transferencias hacia la misma billetera destino en
   menos de 10 minutos.

### Que verificar
5. Despues de activar una regla, abre el detalle de la billetera
   involucrada — la transaccion sospechosa aparece con una etiqueta
   `riesgo MEDIO/ALTO` (color amarillo/naranja/rojo). Pasar el mouse
   muestra el motivo.
6. Abre la pagina **Auditoria** desde el menu: el evento aparece en el
   historial con regla, nivel y detalle. Los contadores en la parte
   superior muestran cuantos eventos hay por nivel.
7. Abre la campanita de notificaciones del usuario afectado — debe
   tener un `FRAUDE_DETECTADO` correspondiente.
8. Filtra por usuario o por nivel en la pagina Auditoria para acotar
   la vista.

---

## 11. Recuperacion ante errores

- Mira la consola del navegador (F12) para errores de red
- Mira la consola del backend para excepciones de Java
- Si `npm install` falla, borra `node_modules/` y reintenta
- Si el frontend no encuentra `/api/...`, asegurate de que el backend
  esta corriendo en `:8081`
- Si despues de un cambio en backend no ves el comportamiento esperado,
  detenlo con `Ctrl+C` y vuelve a correr `mvn spring-boot:run` — el
  hot reload solo aplica al frontend
