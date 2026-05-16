import { useEffect, useState, useRef } from 'react'
import { notificacionesApi } from '../api/client'
import {
  colorPorTipoNotificacion,
  labelTipoNotificacion,
  formatDate,
} from '../utils/format'

/**
 * Campanita de notificaciones para el header / detalle de usuario.
 * Muestra el contador de pendientes y al hacer click abre un dropdown
 * con las ultimas notificaciones del usuario.
 */
export default function CampanaNotificaciones({ idUsuario }) {
  const [pendientes, setPendientes] = useState(0)
  const [abierto, setAbierto] = useState(false)
  const [items, setItems] = useState([])
  const ref = useRef(null)

  const cargarPendientes = async () => {
    if (!idUsuario) return
    try {
      const r = await notificacionesApi.pendientes(idUsuario)
      setPendientes(r.pendientes || 0)
    } catch {}
  }

  const cargarLista = async () => {
    if (!idUsuario) return
    try {
      const lista = await notificacionesApi.listarPorUsuario(idUsuario)
      // mostrar las mas recientes primero (la cola devuelve FIFO,
      // asi que invertimos para la vista de "ultimas")
      setItems([...lista].reverse().slice(0, 8))
    } catch {}
  }

  useEffect(() => {
    cargarPendientes()
    const i = setInterval(cargarPendientes, 8000)
    return () => clearInterval(i)
  }, [idUsuario])

  useEffect(() => {
    if (abierto) cargarLista()
  }, [abierto, idUsuario])

  useEffect(() => {
    const click = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setAbierto(false)
    }
    document.addEventListener('mousedown', click)
    return () => document.removeEventListener('mousedown', click)
  }, [])

  const marcarTodas = async () => {
    await notificacionesApi.marcarTodasLeidas(idUsuario)
    cargarPendientes()
    cargarLista()
  }

  const marcarUna = async (id) => {
    await notificacionesApi.marcarLeida(id)
    cargarPendientes()
    cargarLista()
  }

  if (!idUsuario) return null

  return (
    <div className="relative" ref={ref}>
      <button
        onClick={() => setAbierto(o => !o)}
        className="relative w-9 h-9 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center"
        title="Notificaciones"
      >
        <span className="text-lg">🔔</span>
        {pendientes > 0 && (
          <span className="absolute -top-1 -right-1 bg-red-600 text-white text-xs rounded-full min-w-[18px] h-[18px] px-1 flex items-center justify-center font-medium">
            {pendientes > 99 ? '99+' : pendientes}
          </span>
        )}
      </button>

      {abierto && (
        <div className="absolute right-0 top-11 w-96 bg-white border border-slate-200 rounded-lg shadow-xl z-20">
          <div className="flex justify-between items-center px-4 py-3 border-b border-slate-200">
            <h3 className="font-semibold text-slate-800">Notificaciones</h3>
            {pendientes > 0 && (
              <button
                onClick={marcarTodas}
                className="text-xs text-indigo-600 hover:underline"
              >
                Marcar todas leidas
              </button>
            )}
          </div>
          <div className="max-h-96 overflow-y-auto">
            {items.length === 0 ? (
              <p className="text-slate-400 text-sm text-center py-8">
                No hay notificaciones
              </p>
            ) : (
              items.map(n => (
                <div
                  key={n.id}
                  className={`px-4 py-3 border-b border-slate-100 ${
                    n.leida ? 'opacity-60' : 'bg-slate-50'
                  }`}
                >
                  <div className="flex justify-between items-start gap-2">
                    <span
                      className={`text-[10px] font-medium px-2 py-0.5 rounded border ${
                        colorPorTipoNotificacion[n.tipo] ||
                        'bg-slate-50 text-slate-700 border-slate-200'
                      }`}
                    >
                      {labelTipoNotificacion[n.tipo] || n.tipo}
                    </span>
                    {!n.leida && (
                      <button
                        onClick={() => marcarUna(n.id)}
                        className="text-[10px] text-indigo-600 hover:underline"
                      >
                        Marcar leida
                      </button>
                    )}
                  </div>
                  <p className="text-sm text-slate-700 mt-1">{n.mensaje}</p>
                  <p className="text-[10px] text-slate-400 mt-1">
                    {formatDate(n.fecha)}
                  </p>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  )
}
