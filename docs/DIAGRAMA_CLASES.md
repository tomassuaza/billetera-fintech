# Diagrama de clases

Diagramas en formato Mermaid. GitHub los renderiza automaticamente al
abrir este archivo desde la interfaz web del repositorio.

---

## 1. Vista general por capas

Muestra como se organizan las capas del backend y la relacion gruesa
entre cada servicio, su repositorio y los objetos de dominio que toca.

```mermaid
flowchart LR
    subgraph CTRL[Controllers REST]
        UC[UsuarioController]
        BC[BilleteraController]
        TC[TransaccionController]
        PC[ProgramacionController]
        FC[FidelizacionController]
        NC[NotificacionController]
        GC[GrafoController]
        AC[AnaliticaController]
        AUC[AuditoriaController]
    end

    subgraph SVC[Services]
        US[UsuarioService]
        BS[BilleteraService]
        TS[TransaccionService]
        PS[ProgramacionService]
        RS[ReversionService]
        FS[FidelizacionService]
        NS[NotificacionService]
        GS[GrafoService]
        ANS[AnaliticaService]
        FRS[FraudeService]
        PP[PoliticaPuntos]
    end

    subgraph REPO[Repositorios en memoria]
        UR[(UsuarioRepository - HashMap)]
        BR[(BilleteraRepository - HashMap)]
        TR[(TransaccionRepository - HashMap + LinkedList)]
        PR[(ProgramacionRepository - PriorityQueue)]
        RR[(ReversionRepository - ArrayDeque)]
        FR[(FidelizacionRepository - TreeMap)]
        NR[(NotificacionRepository - LinkedList FIFO)]
        GR[(GrafoTransferenciasRepository - Lista de adyacencia)]
        AR[(AuditoriaRepository - LinkedList)]
    end

    UC --> US --> UR
    BC --> BS --> BR
    TC --> TS --> TR
    TS --> RS
    TS --> FS
    TS --> NS
    TS --> GS
    TS --> FRS
    PC --> PS --> PR
    PS --> TS
    FC --> FS --> FR
    NC --> NS --> NR
    GC --> GS --> GR
    AC --> ANS --> TR
    AUC --> FRS --> AR
    RS --> RR
    RS --> FS
    RS --> GS
    TS -.usa.-> PP
```

---

## 2. Dominio principal

Clases de negocio y los enums que las clasifican.

```mermaid
classDiagram
    class Usuario {
        +String id
        +String nombre
        +String correo
        +int puntos
        +NivelUsuario nivel
        +LocalDateTime fechaRegistro
        +List~String~ idsBilleteras
        +sumarPuntos(int)
        +restarPuntos(int)
    }

    class Billetera {
        +String id
        +String idUsuario
        +String nombre
        +TipoBilletera tipo
        +BigDecimal saldo
        +boolean activa
        +LocalDateTime fechaCreacion
        +acreditar(BigDecimal)
        +debitar(BigDecimal) boolean
    }

    class Transaccion {
        +String id
        +TipoTransaccion tipo
        +BigDecimal monto
        +String idBilleteraOrigen
        +String idBilleteraDestino
        +String idUsuarioGenerador
        +LocalDateTime fecha
        +EstadoTransaccion estado
        +int puntosGenerados
        +boolean reversible
        +NivelRiesgo nivelRiesgo
        +String motivoRiesgo
    }

    class OperacionProgramada {
        +String id
        +TipoTransaccion tipo
        +BigDecimal monto
        +String idBilleteraOrigen
        +String idBilleteraDestino
        +String idUsuarioGenerador
        +LocalDateTime fechaEjecucion
        +EstadoProgramada estado
        +String idTransaccionGenerada
        +compareTo(OperacionProgramada)
    }

    class Notificacion {
        +String id
        +String idUsuario
        +TipoNotificacion tipo
        +String mensaje
        +String idReferencia
        +LocalDateTime fecha
        +boolean leida
    }

    class Arista {
        +String idOrigen
        +String idDestino
        +BigDecimal pesoTotal
        +int conteo
        +LocalDateTime primera
        +LocalDateTime ultima
        +acumular(BigDecimal)
        +restar(BigDecimal) boolean
    }

    class EventoAuditoria {
        +String id
        +String idTransaccion
        +String idUsuario
        +String regla
        +NivelRiesgo nivel
        +String detalle
        +LocalDateTime fecha
    }

    Usuario "1" --> "*" Billetera : posee
    Billetera "1" --> "*" Transaccion : participa
    Usuario "1" --> "*" Transaccion : genera
    Usuario "1" --> "*" Notificacion : recibe
    Usuario "1" --> "*" OperacionProgramada : programa
    Transaccion "1" --> "0..1" EventoAuditoria : dispara
    Arista "*" --> "2" Usuario : conecta
```

---

## 3. Enums

```mermaid
classDiagram
    class TipoBilletera {
        <<enum>>
        AHORRO
        GASTOS_DIARIOS
        COMPRAS
        TRANSPORTE
        INVERSION
    }

    class TipoTransaccion {
        <<enum>>
        RECARGA
        RETIRO
        TRANSFERENCIA_INTERNA
        TRANSFERENCIA_EXTERNA
        REVERSION
    }

    class EstadoTransaccion {
        <<enum>>
        PENDIENTE
        EXITOSA
        FALLIDA
        REVERTIDA
    }

    class EstadoProgramada {
        <<enum>>
        PENDIENTE
        EJECUTADA
        FALLIDA
        CANCELADA
    }

    class NivelUsuario {
        <<enum>>
        BRONCE
        PLATA
        ORO
        PLATINO
        +calcular(int) NivelUsuario
    }

    class TipoNotificacion {
        <<enum>>
        BIENVENIDA
        SALDO_BAJO
        ASCENSO_NIVEL
        OPERACION_RECHAZADA
        PROGRAMADA_EJECUTADA
        PROGRAMADA_FALLIDA
        FRAUDE_DETECTADO
    }

    class NivelRiesgo {
        <<enum>>
        NINGUNO
        BAJO
        MEDIO
        ALTO
    }
```

---

## 4. Servicios y sus colaboradores

Detalle de quien llama a quien en la capa de logica. Util para
entender el flujo de una transaccion completa.

```mermaid
classDiagram
    class TransaccionService {
        +recargar(idBilletera, monto)
        +retirar(idBilletera, monto)
        +transferir(idOrigen, idDestino, monto)
        +historialBilletera(id)
        +historialUsuario(id)
    }

    class ReversionService {
        +deshacerUltima(idUsuario)
        +revertir(idTransaccion)
        +operacionesReversibles(idUsuario)
    }

    class ProgramacionService {
        +programar(...)
        +ejecutarPendientes()
        +ejecutarPorId(id)
        +cancelar(id)
        +listarPendientes()
    }

    class FidelizacionService {
        +actualizarRanking(id, antes, despues)
        +topN(n)
        +usuariosEnRango(min, max)
        +conteoPorNivel()
    }

    class NotificacionService {
        +emitirBienvenida(...)
        +emitirSaldoBajo(...)
        +emitirAscensoNivel(...)
        +emitirOperacionRechazada(...)
        +emitirFraudeDetectado(...)
        +despachar(idUsuario)
    }

    class GrafoService {
        +registrarTransferencia(o, d, m)
        +revertirTransferencia(o, d, m)
        +vecinosDirectos(id)
        +amigosDeAmigos(id)
        +caminoEntre(o, d)
        +rutasFrecuentes(n)
        +ciclos()
    }

    class AnaliticaService {
        +topUsuariosActivos(n)
        +topBilleterasActivas(n)
        +frecuenciaPorTipo()
        +montoMovilizadoEnRango(d, h)
        +topTransaccionesPorValor(n)
    }

    class FraudeService {
        +analizar(transaccion)
        +historial()
        +historialDe(idUsuario)
    }

    TransaccionService --> FidelizacionService
    TransaccionService --> NotificacionService
    TransaccionService --> GrafoService
    TransaccionService --> FraudeService
    ReversionService --> FidelizacionService
    ReversionService --> GrafoService
    ProgramacionService --> TransaccionService
    ProgramacionService --> NotificacionService
    FraudeService --> NotificacionService
```

---

## 5. Mapa estructura de datos / clase

Resumen visual de cual estructura clasica vive en cada repositorio.

```mermaid
flowchart TB
    HM[HashMap] --> UsuarioRepository
    HM --> BilleteraRepository
    HM --> TransaccionRepository
    HM --> NotificacionRepository
    LL[LinkedList con addFirst] --> TransaccionRepository
    LL --> AuditoriaRepository
    LLQ[LinkedList como Cola FIFO] --> NotificacionRepository
    AD[ArrayDeque como Pila] --> ReversionRepository
    PQ[PriorityQueue heap] --> ProgramacionRepository
    TM[TreeMap arbol rojo-negro] --> FidelizacionRepository
    TS[TreeSet con Comparator] --> AnaliticaService
    GR[Grafo lista de adyacencia] --> GrafoTransferenciasRepository
```
