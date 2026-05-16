import { useEffect, useState } from 'react'
import { analiticaApi } from '../api/client'
import {
  formatMoney,
  formatDate,
  colorPorTipoTransaccion,
  labelTipoTransaccion,
  colorPorTipoBilletera,
} from '../utils/format'

/**
 * Tablero de analitica. Agrupa los reportes que el backend ofrece sin
 * mutar estado: top usuarios, top billeteras, distribuciones por tipo,
 * monto movilizado en un rango temporal y top transacciones por valor.
 */
export default function AnaliticaPage() {
  const [usuarios, setUsuarios] = useState([])
  const [billeteras, setBilleteras] = useState([])
  const [porTipo, setPorTipo] = useState({})
  const [categorias, setCategorias] = useState({})
  const [topValor, setTopValor] = useState([])
  const [resumenRango, setResumenRango] = useState(null)
  const [error, setError] = useState('')

  // Rango por defecto: ultimos 30 dias
  const hoy = new Date()
  const haceUnMes = new Date(hoy.getTime() - 30 * 24 * 60 * 60 * 1000)
  const isoLocal = (d) => {
    const tz = d.getTimezoneOffset() * 60000
    return new Date(d.getTime() - tz).toISOString().slice(0, 19)
  }
  const [desde, setDesde] = useState(isoLocal(haceUnMes))
  const [hasta, setHasta] = useState(isoLocal(hoy))

  const cargarTodo = async () => {
    try {
      const [u, b, t, c, tv] = await Promise.all([
        analiticaApi.usuariosActivos(5),
        analiticaApi.billeterasActivas(5),
        analiticaApi.frecuenciaPorTipo(),
        analiticaApi.categoriasBilletera(),
        analiticaApi.topPorValor(5),
      ])
      setUsuarios(u)
      setBilleteras(b)
      setPorTipo(t)
      setCategorias(c)
      setTopValor(tv)
      setError('')
    } catch (e) { setError(e.message) }
  }

  const calcularRango = async () => {
    try {
      const r = await analiticaApi.montoEnRango(desde, hasta)
      setResumenRango(r)
      setError('')
    } catch (e) { setError(e.message) }
  }

  useEffect(() => {
    cargarTodo()
    calcularRango()
  }, [])

  const totalTransacciones = Object.values(porTipo).reduce((a, b) => a + b, 0)

  return (
    <>
      <h1 className="text-2xl font-bold text-slate-800 mb-2">Analitica</h1>
      <p className="text-slate-500 mb-6">
        Reportes y metricas agregadas sobre la actividad del sistema.
        Todas las consultas son de solo lectura y reutilizan las
        estructuras de datos existentes (HashMap principal, historiales,
        TreeSet para ordenar por valor).
      </p>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {/* Cards de distribuciones */}
      <div className="grid grid-cols-2 gap-4 mb-6">
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-semibold text-slate-800 mb-3">
            Frecuencia por tipo de transaccion
          </h2>
          <p className="text-xs text-slate-500 mb-3">
            Total: {totalTransacciones} transacciones exitosas
          </p>
          <ul className="text-sm space-y-2">
            {Object.entries(porTipo).map(([tipo, cant]) => {
              const pct = totalTransacciones > 0
                ? Math.round((cant / totalTransacciones) * 100)
                : 0
              return (
                <li key={tipo}>
                  <div className="flex justify-between mb-1">
                    <span className={colorPorTipoTransaccion[tipo] || 'text-slate-600'}>
                      {labelTipoTransaccion[tipo] || tipo}
                    </span>
                    <span className="text-slate-500">{cant} ({pct}%)</span>
                  </div>
                  <div className="w-full bg-slate-100 rounded h-1.5">
                    <div className="bg-indigo-500 h-1.5 rounded"
                         style={{ width: `${pct}%` }} />
                  </div>
                </li>
              )
            })}
          </ul>
        </div>

        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-semibold text-slate-800 mb-3">
            Categorias de billetera
          </h2>
          <p className="text-xs text-slate-500 mb-3">
            Distribucion de billeteras creadas por tipo
          </p>
          <ul className="text-sm space-y-2">
            {Object.entries(categorias).map(([cat, cant]) => (
              <li key={cat} className="flex justify-between items-center">
                <span className={`px-2 py-0.5 rounded text-xs ${
                  colorPorTipoBilletera[cat] || 'bg-slate-100 text-slate-800'
                }`}>
                  {cat.replace('_', ' ')}
                </span>
                <span className="font-medium text-slate-700">{cant}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>

      {/* Top usuarios y billeteras */}
      <div className="grid grid-cols-2 gap-4 mb-6">
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-semibold text-slate-800 mb-3">
            Top usuarios mas activos
          </h2>
          {usuarios.length === 0 ? (
            <p className="text-slate-400 text-sm">Aun no hay actividad.</p>
          ) : (
            <ol className="text-sm divide-y divide-slate-100">
              {usuarios.map((u, i) => (
                <li key={u.id} className="flex justify-between py-2">
                  <span>
                    <span className="text-slate-400 mr-2">#{i + 1}</span>
                    {u.nombre}{' '}
                    <span className="text-xs text-slate-400 font-mono">({u.id})</span>
                  </span>
                  <span className="font-medium">{u.cantidad}</span>
                </li>
              ))}
            </ol>
          )}
        </div>

        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-semibold text-slate-800 mb-3">
            Top billeteras mas activas
          </h2>
          {billeteras.length === 0 ? (
            <p className="text-slate-400 text-sm">Aun no hay actividad.</p>
          ) : (
            <ol className="text-sm divide-y divide-slate-100">
              {billeteras.map((b, i) => (
                <li key={b.id} className="flex justify-between py-2">
                  <span>
                    <span className="text-slate-400 mr-2">#{i + 1}</span>
                    {b.nombre}{' '}
                    <span className="text-xs text-slate-400 font-mono">({b.id})</span>
                  </span>
                  <span className="font-medium">{b.cantidad}</span>
                </li>
              ))}
            </ol>
          )}
        </div>
      </div>

      {/* Monto movilizado en rango */}
      <div className="bg-white p-6 rounded-lg shadow mb-6">
        <h2 className="text-lg font-semibold text-slate-800 mb-3">
          Monto movilizado en un rango de tiempo
        </h2>
        <div className="grid grid-cols-3 gap-3 items-end mb-4">
          <div>
            <label className="block text-xs text-slate-500 mb-1">Desde</label>
            <input
              type="datetime-local"
              className="w-full border border-slate-300 rounded px-3 py-2 text-sm"
              value={desde}
              onChange={e => setDesde(e.target.value)}
            />
          </div>
          <div>
            <label className="block text-xs text-slate-500 mb-1">Hasta</label>
            <input
              type="datetime-local"
              className="w-full border border-slate-300 rounded px-3 py-2 text-sm"
              value={hasta}
              onChange={e => setHasta(e.target.value)}
            />
          </div>
          <button
            onClick={calcularRango}
            className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded text-sm"
          >
            Recalcular
          </button>
        </div>

        {resumenRango && (
          <div className="grid grid-cols-3 gap-4 text-sm">
            <div className="bg-slate-50 rounded p-4">
              <p className="text-xs text-slate-500">Transacciones</p>
              <p className="text-2xl font-semibold text-slate-800">
                {resumenRango.totalTransacciones}
              </p>
            </div>
            <div className="bg-slate-50 rounded p-4">
              <p className="text-xs text-slate-500">Monto total</p>
              <p className="text-2xl font-semibold text-slate-800">
                {formatMoney(resumenRango.montoTotal)}
              </p>
            </div>
            <div className="bg-slate-50 rounded p-4">
              <p className="text-xs text-slate-500 mb-1">Por tipo</p>
              <ul className="space-y-0.5">
                {Object.entries(resumenRango.porTipo || {}).map(([tipo, monto]) => (
                  <li key={tipo} className="flex justify-between text-xs">
                    <span className={colorPorTipoTransaccion[tipo] || 'text-slate-600'}>
                      {labelTipoTransaccion[tipo] || tipo}
                    </span>
                    <span>{formatMoney(monto)}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}
      </div>

      {/* Top transacciones por valor */}
      <div className="bg-white p-6 rounded-lg shadow">
        <h2 className="text-lg font-semibold text-slate-800 mb-1">
          Top transacciones por valor
        </h2>
        <p className="text-xs text-slate-500 mb-4">
          Ordenadas con un TreeSet con comparador descendente por monto
        </p>
        {topValor.length === 0 ? (
          <p className="text-slate-400 text-sm">Aun no hay transacciones.</p>
        ) : (
          <table className="w-full text-sm">
            <thead className="text-xs text-slate-500 uppercase">
              <tr>
                <th className="text-left py-2">#</th>
                <th className="text-left py-2">Tipo</th>
                <th className="text-left py-2">Fecha</th>
                <th className="text-right py-2">Monto</th>
                <th className="text-right py-2">Puntos</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {topValor.map((t, i) => (
                <tr key={t.id}>
                  <td className="py-2 text-slate-400">#{i + 1}</td>
                  <td className={`py-2 ${colorPorTipoTransaccion[t.tipo] || 'text-slate-600'}`}>
                    {labelTipoTransaccion[t.tipo] || t.tipo}
                  </td>
                  <td className="py-2 text-slate-500 text-xs">{formatDate(t.fecha)}</td>
                  <td className="py-2 text-right font-medium">{formatMoney(t.monto)}</td>
                  <td className="py-2 text-right">{t.puntosGenerados}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  )
}
