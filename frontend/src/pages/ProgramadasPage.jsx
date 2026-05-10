import { useEffect, useState } from 'react'
import {
  programadasApi, usuariosApi, billeterasApi
} from '../api/client'
import {
  formatMoney, formatDate,
  colorPorTipoTransaccion, labelTipoTransaccion
} from '../utils/format'

const TIPOS = ['RECARGA', 'RETIRO', 'TRANSFERENCIA_INTERNA', 'TRANSFERENCIA_EXTERNA']

/**
 * Pagina para programar operaciones futuras y ver las pendientes.
 *
 * Las operaciones programadas se almacenan en una PriorityQueue ordenada
 * por fecha de ejecucion. La cola garantiza que al ejecutar pendientes
 * salgan en orden de fecha (la mas cercana primero).
 */
export default function ProgramadasPage() {
  const [pendientes, setPendientes] = useState([])
  const [usuarios, setUsuarios] = useState([])
  const [billeteras, setBilleteras] = useState([])
  const [error, setError] = useState('')
  const [info, setInfo] = useState('')

  // Form
  const [tipo, setTipo] = useState('RECARGA')
  const [monto, setMonto] = useState('')
  const [idUsuario, setIdUsuario] = useState('')
  const [idOrigen, setIdOrigen] = useState('')
  const [idDestino, setIdDestino] = useState('')
  const [fechaEjecucion, setFechaEjecucion] = useState('')

  const cargar = async () => {
    try {
      const [p, u, b] = await Promise.all([
        programadasApi.listarPendientes(),
        usuariosApi.listar(),
        billeterasApi.listar()
      ])
      setPendientes(p)
      setUsuarios(u)
      setBilleteras(b)
      setError('')
    } catch (e) {
      setError(e.message)
    }
  }

  useEffect(() => { cargar() }, [])

  const programar = async (e) => {
    e.preventDefault()
    try {
      await programadasApi.programar({
        tipo,
        monto: parseFloat(monto),
        idBilleteraOrigen: idOrigen || null,
        idBilleteraDestino: idDestino || null,
        idUsuarioGenerador: idUsuario,
        fechaEjecucion
      })
      setInfo('Operacion programada correctamente')
      setTimeout(() => setInfo(''), 4000)
      setMonto(''); setIdOrigen(''); setIdDestino(''); setFechaEjecucion('')
      cargar()
    } catch (e) {
      setError(e.message)
    }
  }

  const ejecutarVencidas = async () => {
    try {
      const ejecutadas = await programadasApi.ejecutarVencidas()
      setInfo(`Ejecutadas ${ejecutadas.length} operaciones vencidas`)
      setTimeout(() => setInfo(''), 4000)
      cargar()
    } catch (e) { setError(e.message) }
  }

  const ejecutarUna = async (id) => {
    try {
      await programadasApi.ejecutar(id)
      setInfo('Operacion ejecutada')
      setTimeout(() => setInfo(''), 3000)
      cargar()
    } catch (e) { setError(e.message) }
  }

  const cancelar = async (id) => {
    if (!confirm('Cancelar esta operacion programada?')) return
    try {
      await programadasApi.cancelar(id)
      cargar()
    } catch (e) { setError(e.message) }
  }

  const necesitaOrigen = tipo === 'RETIRO' || tipo.startsWith('TRANSFERENCIA')
  const necesitaDestino = tipo === 'RECARGA' || tipo.startsWith('TRANSFERENCIA')

  return (
    <>
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}
      {info && (
        <div className="bg-green-50 border border-green-200 text-green-800 px-4 py-3 rounded mb-4">
          {info}
        </div>
      )}

      {/* Form de programacion */}
      <form onSubmit={programar} className="bg-white p-6 rounded-lg shadow mb-6">
        <h2 className="text-xl font-semibold mb-4">Programar nueva operacion</h2>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Tipo</label>
            <select value={tipo} onChange={e => setTipo(e.target.value)}
              className="w-full border border-slate-300 rounded px-3 py-2">
              {TIPOS.map(t => (
                <option key={t} value={t}>{labelTipoTransaccion[t]}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Monto</label>
            <input type="number" step="any" min="0.01" required
              value={monto} onChange={e => setMonto(e.target.value)}
              className="w-full border border-slate-300 rounded px-3 py-2" />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Usuario generador
            </label>
            <select required value={idUsuario}
              onChange={e => setIdUsuario(e.target.value)}
              className="w-full border border-slate-300 rounded px-3 py-2">
              <option value="">— Selecciona —</option>
              {usuarios.map(u => (
                <option key={u.id} value={u.id}>{u.nombre}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Fecha de ejecucion
            </label>
            <input type="datetime-local" required
              value={fechaEjecucion}
              onChange={e => setFechaEjecucion(e.target.value)}
              className="w-full border border-slate-300 rounded px-3 py-2" />
          </div>

          {necesitaOrigen && (
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                Billetera origen
              </label>
              <select required value={idOrigen}
                onChange={e => setIdOrigen(e.target.value)}
                className="w-full border border-slate-300 rounded px-3 py-2">
                <option value="">— Selecciona —</option>
                {billeteras.filter(b => b.activa).map(b => (
                  <option key={b.id} value={b.id}>{b.nombre} ({b.id})</option>
                ))}
              </select>
            </div>
          )}

          {necesitaDestino && (
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                Billetera destino
              </label>
              <select required value={idDestino}
                onChange={e => setIdDestino(e.target.value)}
                className="w-full border border-slate-300 rounded px-3 py-2">
                <option value="">— Selecciona —</option>
                {billeteras.filter(b => b.activa).map(b => (
                  <option key={b.id} value={b.id}>{b.nombre} ({b.id})</option>
                ))}
              </select>
            </div>
          )}
        </div>
        <p className="text-xs text-slate-500 mt-3">
          Pago programado ejecutado exitosamente otorga +10 puntos extra al usuario.
        </p>
        <button type="submit"
          className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded mt-4">
          Programar
        </button>
      </form>

      {/* Listado */}
      <div className="bg-white p-6 rounded-lg shadow">
        <div className="flex justify-between items-center mb-4">
          <div>
            <h2 className="text-xl font-semibold">Operaciones pendientes ({pendientes.length})</h2>
            <p className="text-xs text-slate-500 mt-1">
              Ordenadas por fecha de ejecucion (PriorityQueue)
            </p>
          </div>
          <button onClick={ejecutarVencidas}
            className="bg-amber-600 hover:bg-amber-700 text-white px-4 py-2 rounded text-sm">
            ▶ Ejecutar vencidas
          </button>
        </div>

        {pendientes.length === 0 ? (
          <p className="text-slate-400 text-center py-6">No hay operaciones programadas</p>
        ) : (
          <ul className="divide-y divide-slate-100">
            {pendientes.map(op => {
              const yaPaso = new Date(op.fechaEjecucion) < new Date()
              return (
                <li key={op.id} className="py-3 flex justify-between items-start">
                  <div>
                    <div className={`text-sm font-medium ${colorPorTipoTransaccion[op.tipo]}`}>
                      {labelTipoTransaccion[op.tipo] || op.tipo} — {formatMoney(op.monto)}
                    </div>
                    <div className="text-xs text-slate-500 mt-1">
                      Ejecuta: <span className={yaPaso ? 'text-red-600 font-medium' : ''}>
                        {formatDate(op.fechaEjecucion)}
                      </span>
                    </div>
                    <div className="text-xs text-slate-400 mt-0.5">
                      {op.idBilleteraOrigen && `De: ${op.idBilleteraOrigen} `}
                      {op.idBilleteraDestino && `→ ${op.idBilleteraDestino}`}
                    </div>
                    <div className="text-xs text-slate-400 font-mono mt-0.5">{op.id}</div>
                  </div>
                  <div className="flex gap-2">
                    <button onClick={() => ejecutarUna(op.id)}
                      className="text-xs bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded">
                      Ejecutar
                    </button>
                    <button onClick={() => cancelar(op.id)}
                      className="text-xs text-red-600 hover:text-red-800 px-2">
                      Cancelar
                    </button>
                  </div>
                </li>
              )
            })}
          </ul>
        )}
      </div>
    </>
  )
}
