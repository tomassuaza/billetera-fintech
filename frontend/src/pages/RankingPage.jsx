import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fidelizacionApi } from '../api/client'
import { colorPorNivel } from '../utils/format'

const NIVELES = ['BRONCE', 'PLATA', 'ORO', 'PLATINO']

/**
 * Pagina de ranking de usuarios por puntos de fidelizacion.
 *
 * Los datos se obtienen del TreeMap<Integer, Set<String>> mantenido en
 * FidelizacionRepository, que permite consultas por rango en O(log n + k)
 * y devuelve usuarios ordenados por puntos de manera natural.
 */
export default function RankingPage() {
  const [top, setTop] = useState([])
  const [conteo, setConteo] = useState({})
  const [n, setN] = useState(10)
  const [min, setMin] = useState('')
  const [max, setMax] = useState('')
  const [enRango, setEnRango] = useState(null)
  const [error, setError] = useState('')

  const cargar = async () => {
    try {
      const [t, c] = await Promise.all([
        fidelizacionApi.topN(n),
        fidelizacionApi.conteo()
      ])
      setTop(t)
      setConteo(c)
      setError('')
    } catch (e) { setError(e.message) }
  }

  useEffect(() => { cargar() }, [n])

  const consultarRango = async (e) => {
    e.preventDefault()
    try {
      const r = await fidelizacionApi.enRango(parseInt(min), parseInt(max))
      setEnRango(r)
    } catch (e) { setError(e.message) }
  }

  const limpiarRango = () => {
    setMin(''); setMax(''); setEnRango(null)
  }

  return (
    <>
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {/* Conteo por nivel */}
      <div className="grid grid-cols-4 gap-3 mb-6">
        {NIVELES.map(nivel => (
          <div key={nivel} className="bg-white p-4 rounded-lg shadow text-center">
            <div className="text-3xl font-bold text-slate-800">
              {conteo[nivel] ?? 0}
            </div>
            <span className={`inline-block text-xs px-2 py-0.5 rounded mt-1 ${colorPorNivel[nivel]}`}>
              {nivel}
            </span>
          </div>
        ))}
      </div>

      {/* Consulta por rango */}
      <form onSubmit={consultarRango} className="bg-white p-6 rounded-lg shadow mb-6">
        <h2 className="text-xl font-semibold mb-1">Consulta por rango de puntos</h2>
        <p className="text-xs text-slate-500 mb-4">
          Usa subMap del TreeMap — O(log n + k)
        </p>
        <div className="flex gap-3 items-end">
          <div>
            <label className="block text-xs text-slate-600 mb-1">Minimo</label>
            <input type="number" min="0" required value={min}
              onChange={e => setMin(e.target.value)}
              className="border border-slate-300 rounded px-3 py-2 w-32" />
          </div>
          <div>
            <label className="block text-xs text-slate-600 mb-1">Maximo</label>
            <input type="number" min="0" required value={max}
              onChange={e => setMax(e.target.value)}
              className="border border-slate-300 rounded px-3 py-2 w-32" />
          </div>
          <button type="submit"
            className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded text-sm">
            Buscar
          </button>
          {enRango && (
            <button type="button" onClick={limpiarRango}
              className="text-slate-600 hover:text-slate-800 text-sm">
              Limpiar
            </button>
          )}
        </div>

        {enRango && (
          <div className="mt-4">
            <p className="text-sm font-medium text-slate-700 mb-2">
              {enRango.length} usuarios en el rango [{min}, {max}]:
            </p>
            <ul className="text-sm space-y-1">
              {enRango.map(u => (
                <li key={u.id}>
                  <Link to={`/usuarios/${u.id}`} className="text-indigo-600 hover:underline">
                    {u.nombre}
                  </Link>
                  <span className="text-slate-500"> — {u.puntos} pts ({u.nivel})</span>
                </li>
              ))}
            </ul>
          </div>
        )}
      </form>

      {/* Top N */}
      <div className="bg-white p-6 rounded-lg shadow">
        <div className="flex justify-between items-center mb-4">
          <div>
            <h2 className="text-xl font-semibold">Top {n} usuarios</h2>
            <p className="text-xs text-slate-500 mt-1">
              Recorrido descendente del TreeMap
            </p>
          </div>
          <select value={n} onChange={e => setN(parseInt(e.target.value))}
            className="border border-slate-300 rounded px-3 py-2 text-sm">
            {[5, 10, 20, 50].map(v => (
              <option key={v} value={v}>Top {v}</option>
            ))}
          </select>
        </div>

        {top.length === 0 ? (
          <p className="text-slate-400 text-center py-6">Aun no hay usuarios con puntos</p>
        ) : (
          <ol className="divide-y divide-slate-100">
            {top.map((u, i) => (
              <li key={u.id} className="py-3">
                <Link to={`/usuarios/${u.id}`}
                  className="flex justify-between items-center hover:bg-slate-50 -mx-3 px-3 py-1 rounded transition">
                  <div className="flex items-center gap-3">
                    <span className="text-2xl font-bold text-slate-300 w-8">
                      {i + 1}
                    </span>
                    <div>
                      <div className="font-medium text-slate-800">{u.nombre}</div>
                      <div className="text-xs text-slate-500">{u.correo}</div>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="font-semibold">{u.puntos} pts</div>
                    <span className={`inline-block text-xs px-2 py-0.5 rounded mt-0.5 ${colorPorNivel[u.nivel]}`}>
                      {u.nivel}
                    </span>
                  </div>
                </Link>
              </li>
            ))}
          </ol>
        )}
      </div>
    </>
  )
}
