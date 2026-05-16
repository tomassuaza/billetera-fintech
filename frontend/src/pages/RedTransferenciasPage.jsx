import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { usuariosApi, grafoApi } from '../api/client'
import { formatMoney } from '../utils/format'

/**
 * Vista de la red dirigida y ponderada de transferencias entre usuarios.
 * Apoya:
 *  - Lista de aristas globales con peso y conteo
 *  - Vecinos directos y amigos de amigos (BFS)
 *  - Camino mas corto entre dos usuarios
 *  - Top rutas frecuentes y ciclos detectados
 */
export default function RedTransferenciasPage() {
  const [usuarios, setUsuarios] = useState([])
  const [aristas, setAristas] = useState([])
  const [rutas, setRutas] = useState([])
  const [ciclos, setCiclos] = useState([])

  const [seleccionado, setSeleccionado] = useState('')
  const [vecinos, setVecinos] = useState(new Set())
  const [amigosDe2, setAmigosDe2] = useState(new Set())

  const [origenCamino, setOrigenCamino] = useState('')
  const [destinoCamino, setDestinoCamino] = useState('')
  const [camino, setCamino] = useState([])
  const [caminoMsg, setCaminoMsg] = useState('')

  const [error, setError] = useState('')

  const nombrePorId = useMemo(() => {
    const m = new Map()
    usuarios.forEach(u => m.set(u.id, u.nombre))
    return m
  }, [usuarios])

  const nombrar = (id) => nombrePorId.get(id) || id

  const cargarTodo = async () => {
    try {
      const [u, a, r, c] = await Promise.all([
        usuariosApi.listar(),
        grafoApi.aristas(),
        grafoApi.rutasFrecuentes(5),
        grafoApi.ciclos(),
      ])
      setUsuarios(u)
      setAristas(a)
      setRutas(r)
      setCiclos(c)
      if (u.length && !seleccionado) setSeleccionado(u[0].id)
      if (u.length && !origenCamino) setOrigenCamino(u[0].id)
      if (u.length > 1 && !destinoCamino) setDestinoCamino(u[1].id)
      setError('')
    } catch (e) { setError(e.message) }
  }

  useEffect(() => { cargarTodo() }, [])

  useEffect(() => {
    if (!seleccionado) return
    Promise.all([
      grafoApi.vecinos(seleccionado),
      grafoApi.amigosDeAmigos(seleccionado),
    ])
      .then(([v, aa]) => {
        setVecinos(new Set(v))
        setAmigosDe2(new Set(aa))
      })
      .catch(e => setError(e.message))
  }, [seleccionado])

  const buscarCamino = async () => {
    if (!origenCamino || !destinoCamino) return
    try {
      const c = await grafoApi.camino(origenCamino, destinoCamino)
      setCamino(c)
      setCaminoMsg(c.length === 0
        ? 'No hay camino dirigido entre los dos usuarios.'
        : `Camino de ${c.length - 1} salto${c.length - 1 === 1 ? '' : 's'}.`)
    } catch (e) { setError(e.message) }
  }

  return (
    <>
      <h1 className="text-2xl font-bold text-slate-800 mb-2">
        Red de transferencias
      </h1>
      <p className="text-slate-500 mb-6">
        Grafo dirigido y ponderado. Cada arista resume las transferencias
        externas entre dos usuarios (peso = monto total acumulado, conteo
        = numero de envios).
      </p>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      <div className="grid grid-cols-2 gap-4 mb-6">
        {/* Vecinos / amigos de amigos */}
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-semibold text-slate-800 mb-3">
            Vecinos y amigos de amigos
          </h2>
          <select
            className="w-full border border-slate-300 rounded px-3 py-2 mb-4"
            value={seleccionado}
            onChange={e => setSeleccionado(e.target.value)}
          >
            {usuarios.map(u => (
              <option key={u.id} value={u.id}>{u.nombre} ({u.id})</option>
            ))}
          </select>
          <div className="space-y-3 text-sm">
            <div>
              <p className="text-xs text-slate-500 uppercase mb-1">
                Vecinos directos ({vecinos.size})
              </p>
              {vecinos.size === 0
                ? <p className="text-slate-400">Aun no ha transferido a nadie.</p>
                : <ul className="flex flex-wrap gap-1">
                    {[...vecinos].map(id => (
                      <li key={id} className="bg-indigo-50 text-indigo-700 px-2 py-0.5 rounded text-xs">
                        {nombrar(id)}
                      </li>
                    ))}
                  </ul>}
            </div>
            <div>
              <p className="text-xs text-slate-500 uppercase mb-1">
                Amigos de amigos ({amigosDe2.size})
              </p>
              {amigosDe2.size === 0
                ? <p className="text-slate-400">Sin alcance a 2 saltos.</p>
                : <ul className="flex flex-wrap gap-1">
                    {[...amigosDe2].map(id => (
                      <li key={id} className="bg-purple-50 text-purple-700 px-2 py-0.5 rounded text-xs">
                        {nombrar(id)}
                      </li>
                    ))}
                  </ul>}
            </div>
          </div>
        </div>

        {/* Camino mas corto */}
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-semibold text-slate-800 mb-3">
            Camino mas corto (BFS)
          </h2>
          <div className="space-y-2">
            <select
              className="w-full border border-slate-300 rounded px-3 py-2"
              value={origenCamino}
              onChange={e => setOrigenCamino(e.target.value)}
            >
              <option value="">Origen...</option>
              {usuarios.map(u => (
                <option key={u.id} value={u.id}>{u.nombre} ({u.id})</option>
              ))}
            </select>
            <select
              className="w-full border border-slate-300 rounded px-3 py-2"
              value={destinoCamino}
              onChange={e => setDestinoCamino(e.target.value)}
            >
              <option value="">Destino...</option>
              {usuarios.map(u => (
                <option key={u.id} value={u.id}>{u.nombre} ({u.id})</option>
              ))}
            </select>
            <button
              onClick={buscarCamino}
              className="w-full bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded text-sm"
            >
              Buscar camino
            </button>
          </div>
          {caminoMsg && (
            <p className="text-xs text-slate-500 mt-3">{caminoMsg}</p>
          )}
          {camino.length > 0 && (
            <div className="mt-2 text-sm">
              <p className="text-slate-700">
                {camino.map((id, i) => (
                  <span key={id}>
                    <span className="font-medium">{nombrar(id)}</span>
                    {i < camino.length - 1 && <span className="text-slate-400 mx-1">→</span>}
                  </span>
                ))}
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Aristas globales */}
      <div className="bg-white p-6 rounded-lg shadow mb-6">
        <h2 className="text-lg font-semibold text-slate-800 mb-3">
          Aristas del grafo ({aristas.length})
        </h2>
        {aristas.length === 0 ? (
          <p className="text-slate-400 text-sm">
            Aun no hay transferencias entre usuarios distintos. Crea
            una en el detalle de cualquier billetera y la veras aqui.
          </p>
        ) : (
          <table className="w-full text-sm">
            <thead className="text-xs text-slate-500 uppercase">
              <tr>
                <th className="text-left py-2">Origen</th>
                <th className="text-left py-2">Destino</th>
                <th className="text-right py-2">Peso total</th>
                <th className="text-right py-2">Conteo</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {aristas.map(a => (
                <tr key={`${a.idOrigen}->${a.idDestino}`}>
                  <td className="py-2">{nombrar(a.idOrigen)}</td>
                  <td className="py-2">{nombrar(a.idDestino)}</td>
                  <td className="py-2 text-right font-medium">{formatMoney(a.pesoTotal)}</td>
                  <td className="py-2 text-right">{a.conteo}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Rutas frecuentes y ciclos */}
      <div className="grid grid-cols-2 gap-4">
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-semibold text-slate-800 mb-3">
            Top rutas frecuentes
          </h2>
          {rutas.length === 0 ? (
            <p className="text-slate-400 text-sm">Sin rutas aun.</p>
          ) : (
            <ol className="text-sm space-y-1">
              {rutas.map((a, i) => (
                <li key={`${a.idOrigen}->${a.idDestino}`} className="flex justify-between">
                  <span>
                    <span className="text-slate-400 mr-2">#{i + 1}</span>
                    {nombrar(a.idOrigen)} <span className="text-slate-400">→</span> {nombrar(a.idDestino)}
                  </span>
                  <span className="font-medium">{formatMoney(a.pesoTotal)} ({a.conteo})</span>
                </li>
              ))}
            </ol>
          )}
        </div>

        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-semibold text-slate-800 mb-3">
            Ciclos detectados (DFS)
          </h2>
          {ciclos.length === 0 ? (
            <p className="text-slate-400 text-sm">
              No hay ciclos. Si dos usuarios se transfieren mutuamente,
              aparecera aqui.
            </p>
          ) : (
            <ul className="text-sm space-y-2">
              {ciclos.map((ciclo, i) => (
                <li key={i} className="bg-rose-50 border border-rose-200 rounded px-3 py-2 text-rose-800">
                  {ciclo.map((id, j) => (
                    <span key={`${i}-${j}-${id}`}>
                      {nombrar(id)}
                      {j < ciclo.length - 1 && <span className="text-rose-400 mx-1">→</span>}
                    </span>
                  ))}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      <p className="mt-6 text-xs text-slate-400">
        <Link to="/" className="hover:underline">← Volver a usuarios</Link>
      </p>
    </>
  )
}
