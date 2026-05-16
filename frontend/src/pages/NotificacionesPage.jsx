import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { usuariosApi, notificacionesApi } from '../api/client'
import {
  colorPorTipoNotificacion,
  labelTipoNotificacion,
  formatDate,
} from '../utils/format'

const TIPOS = [
  'BIENVENIDA',
  'SALDO_BAJO',
  'ASCENSO_NIVEL',
  'OPERACION_RECHAZADA',
  'PROGRAMADA_EJECUTADA',
  'PROGRAMADA_FALLIDA',
  'FRAUDE_DETECTADO',
]

export default function NotificacionesPage() {
  const [usuarios, setUsuarios] = useState([])
  const [idUsuario, setIdUsuario] = useState('')
  const [filtroTipo, setFiltroTipo] = useState('TODAS')
  const [lista, setLista] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    usuariosApi.listar().then(u => {
      setUsuarios(u)
      if (u.length && !idUsuario) setIdUsuario(u[0].id)
    }).catch(e => setError(e.message))
  }, [])

  const cargar = async () => {
    if (!idUsuario) return
    try {
      const data = await notificacionesApi.listarPorUsuario(idUsuario)
      // mostrar mas recientes arriba
      setLista([...data].reverse())
      setError('')
    } catch (e) { setError(e.message) }
  }

  useEffect(() => { cargar() }, [idUsuario])

  const marcarTodas = async () => {
    await notificacionesApi.marcarTodasLeidas(idUsuario)
    cargar()
  }

  const marcarUna = async (id) => {
    await notificacionesApi.marcarLeida(id)
    cargar()
  }

  const despachar = async () => {
    const drenadas = await notificacionesApi.despachar(idUsuario)
    alert(`Despachadas ${drenadas.length} notificaciones (todas marcadas como leidas en orden FIFO)`)
    cargar()
  }

  const filtradas = filtroTipo === 'TODAS'
    ? lista
    : lista.filter(n => n.tipo === filtroTipo)

  const pendientes = lista.filter(n => !n.leida).length

  return (
    <>
      <h1 className="text-2xl font-bold text-slate-800 mb-2">Notificaciones</h1>
      <p className="text-slate-500 mb-6">
        Buzon FIFO por usuario. Las nuevas se encolan al final;
        al "despachar" se procesan de la mas antigua a la mas reciente.
      </p>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      <div className="bg-white p-6 rounded-lg shadow mb-6">
        <div className="grid grid-cols-3 gap-4 items-end">
          <div>
            <label className="block text-xs text-slate-500 mb-1">Usuario</label>
            <select
              className="w-full border border-slate-300 rounded px-3 py-2"
              value={idUsuario}
              onChange={e => setIdUsuario(e.target.value)}
            >
              {usuarios.map(u => (
                <option key={u.id} value={u.id}>
                  {u.nombre} ({u.id})
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs text-slate-500 mb-1">Filtrar por tipo</label>
            <select
              className="w-full border border-slate-300 rounded px-3 py-2"
              value={filtroTipo}
              onChange={e => setFiltroTipo(e.target.value)}
            >
              <option value="TODAS">Todas</option>
              {TIPOS.map(t => (
                <option key={t} value={t}>
                  {labelTipoNotificacion[t] || t}
                </option>
              ))}
            </select>
          </div>
          <div className="flex gap-2">
            <button
              onClick={marcarTodas}
              disabled={pendientes === 0}
              className="flex-1 bg-slate-100 hover:bg-slate-200 disabled:opacity-50 disabled:cursor-not-allowed text-slate-700 px-3 py-2 rounded text-sm"
            >
              Marcar todas leidas
            </button>
            <button
              onClick={despachar}
              disabled={pendientes === 0}
              className="flex-1 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed text-white px-3 py-2 rounded text-sm"
            >
              Despachar FIFO ({pendientes})
            </button>
          </div>
        </div>
      </div>

      <div className="bg-white p-6 rounded-lg shadow">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-semibold text-slate-800">
            Buzon ({filtradas.length}{filtroTipo !== 'TODAS' && ` de ${lista.length}`})
          </h2>
          {idUsuario && (
            <Link
              to={`/usuarios/${idUsuario}`}
              className="text-sm text-indigo-600 hover:underline"
            >
              Ver perfil del usuario →
            </Link>
          )}
        </div>

        {filtradas.length === 0 ? (
          <p className="text-slate-400 text-center py-8">
            {lista.length === 0
              ? 'Este usuario no tiene notificaciones aun'
              : 'Ninguna notificacion coincide con el filtro'}
          </p>
        ) : (
          <ul className="divide-y divide-slate-100">
            {filtradas.map(n => (
              <li
                key={n.id}
                className={`py-3 ${n.leida ? 'opacity-60' : ''}`}
              >
                <div className="flex justify-between items-start gap-3">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <span
                        className={`text-[11px] font-medium px-2 py-0.5 rounded border ${
                          colorPorTipoNotificacion[n.tipo] ||
                          'bg-slate-50 text-slate-700 border-slate-200'
                        }`}
                      >
                        {labelTipoNotificacion[n.tipo] || n.tipo}
                      </span>
                      {!n.leida && (
                        <span className="text-[10px] bg-red-100 text-red-700 px-1.5 py-0.5 rounded">
                          NUEVA
                        </span>
                      )}
                      <span className="text-[11px] text-slate-400 font-mono">
                        {n.id}
                      </span>
                    </div>
                    <p className="text-sm text-slate-700">{n.mensaje}</p>
                    <p className="text-[11px] text-slate-400 mt-1">
                      {formatDate(n.fecha)}
                      {n.idReferencia && (
                        <> · ref: <span className="font-mono">{n.idReferencia}</span></>
                      )}
                    </p>
                  </div>
                  {!n.leida && (
                    <button
                      onClick={() => marcarUna(n.id)}
                      className="text-xs text-indigo-600 hover:underline"
                    >
                      Marcar leida
                    </button>
                  )}
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </>
  )
}
