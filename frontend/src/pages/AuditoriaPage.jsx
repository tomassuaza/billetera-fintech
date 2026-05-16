import { useEffect, useMemo, useState } from 'react'
import { usuariosApi, auditoriaApi } from '../api/client'
import {
  formatDate,
  colorPorNivelRiesgo,
} from '../utils/format'

/**
 * Panel de auditoria. Lista los eventos registrados por el modulo de
 * deteccion de patrones inusuales: transaccion sospechosa, regla
 * activada, nivel asignado y motivo.
 */
export default function AuditoriaPage() {
  const [usuarios, setUsuarios] = useState([])
  const [filtroUsuario, setFiltroUsuario] = useState('TODOS')
  const [filtroNivel, setFiltroNivel] = useState('TODOS')
  const [eventos, setEventos] = useState([])
  const [error, setError] = useState('')

  const nombrePorId = useMemo(() => {
    const m = new Map()
    usuarios.forEach(u => m.set(u.id, u.nombre))
    return m
  }, [usuarios])

  const cargar = async () => {
    try {
      const u = await usuariosApi.listar()
      setUsuarios(u)
      const ev = filtroUsuario === 'TODOS'
        ? await auditoriaApi.eventos()
        : await auditoriaApi.porUsuario(filtroUsuario)
      setEventos(ev)
      setError('')
    } catch (e) { setError(e.message) }
  }

  useEffect(() => { cargar() }, [filtroUsuario])

  const filtrados = filtroNivel === 'TODOS'
    ? eventos
    : eventos.filter(e => e.nivel === filtroNivel)

  const conteoNivel = eventos.reduce((acc, ev) => {
    acc[ev.nivel] = (acc[ev.nivel] || 0) + 1
    return acc
  }, {})

  return (
    <>
      <h1 className="text-2xl font-bold text-slate-800 mb-2">Auditoria</h1>
      <p className="text-slate-500 mb-6">
        Eventos de seguridad detectados automaticamente: rafagas de
        transferencias, montos atipicos, envios repetidos al mismo
        destino y fragmentacion entre billeteras del mismo usuario.
      </p>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      <div className="grid grid-cols-4 gap-3 mb-6">
        {['BAJO', 'MEDIO', 'ALTO'].map(nivel => (
          <div key={nivel} className={`p-4 rounded border ${colorPorNivelRiesgo[nivel]}`}>
            <p className="text-xs uppercase opacity-75">{nivel}</p>
            <p className="text-2xl font-bold">{conteoNivel[nivel] || 0}</p>
          </div>
        ))}
        <div className="p-4 rounded border bg-slate-100 text-slate-700 border-slate-200">
          <p className="text-xs uppercase opacity-75">Total</p>
          <p className="text-2xl font-bold">{eventos.length}</p>
        </div>
      </div>

      <div className="bg-white p-6 rounded-lg shadow mb-4">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs text-slate-500 mb-1">Usuario</label>
            <select
              className="w-full border border-slate-300 rounded px-3 py-2 text-sm"
              value={filtroUsuario}
              onChange={e => setFiltroUsuario(e.target.value)}
            >
              <option value="TODOS">Todos los usuarios</option>
              {usuarios.map(u => (
                <option key={u.id} value={u.id}>{u.nombre} ({u.id})</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs text-slate-500 mb-1">Nivel</label>
            <select
              className="w-full border border-slate-300 rounded px-3 py-2 text-sm"
              value={filtroNivel}
              onChange={e => setFiltroNivel(e.target.value)}
            >
              <option value="TODOS">Todos los niveles</option>
              <option value="BAJO">Bajo</option>
              <option value="MEDIO">Medio</option>
              <option value="ALTO">Alto</option>
            </select>
          </div>
        </div>
      </div>

      <div className="bg-white p-6 rounded-lg shadow">
        <h2 className="text-lg font-semibold text-slate-800 mb-3">
          Eventos ({filtrados.length}
          {filtroNivel !== 'TODOS' && ` de ${eventos.length}`})
        </h2>
        {filtrados.length === 0 ? (
          <p className="text-slate-400 text-sm py-6 text-center">
            No hay eventos detectados con esos filtros. Para generar
            alertas haz varias transferencias seguidas, una transferencia
            mucho mayor al promedio o envios desde varias billeteras
            propias hacia el mismo destino.
          </p>
        ) : (
          <ul className="divide-y divide-slate-100">
            {filtrados.map(ev => (
              <li key={ev.id} className="py-3">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <span className={`text-[11px] font-medium px-2 py-0.5 rounded border ${
                        colorPorNivelRiesgo[ev.nivel] || ''
                      }`}>
                        {ev.nivel}
                      </span>
                      <span className="text-xs text-slate-500">{ev.regla}</span>
                      <span className="text-[11px] text-slate-400 font-mono">
                        {ev.id}
                      </span>
                    </div>
                    <p className="text-sm text-slate-700">{ev.detalle}</p>
                    <p className="text-[11px] text-slate-400 mt-1">
                      {formatDate(ev.fecha)} ·{' '}
                      <span className="font-mono">trx {ev.idTransaccion}</span> ·{' '}
                      usuario {nombrePorId.get(ev.idUsuario) || ev.idUsuario}
                    </p>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </>
  )
}
