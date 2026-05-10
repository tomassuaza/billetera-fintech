import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { usuariosApi, billeterasApi, transaccionesApi } from '../api/client'
import { colorPorTipoBilletera, colorPorNivel, formatMoney } from '../utils/format'

const TIPOS = ['AHORRO', 'GASTOS_DIARIOS', 'COMPRAS', 'TRANSPORTE', 'INVERSION']

export default function UsuarioDetallePage() {
  const { id } = useParams()
  const [usuario, setUsuario] = useState(null)
  const [billeteras, setBilleteras] = useState([])
  const [reversibles, setReversibles] = useState(0)
  const [nombre, setNombre] = useState('')
  const [tipo, setTipo] = useState('GASTOS_DIARIOS')
  const [error, setError] = useState('')
  const [info, setInfo] = useState('')

  const cargar = async () => {
    try {
      const [u, b, r] = await Promise.all([
        usuariosApi.obtener(id),
        billeterasApi.listarPorUsuario(id),
        transaccionesApi.contarReversibles(id)
      ])
      setUsuario(u)
      setBilleteras(b)
      setReversibles(r.count || 0)
      setError('')
    } catch (e) {
      setError(e.message || 'No se pudo cargar el usuario')
    }
  }

  useEffect(() => { cargar() }, [id])

  const crear = async (e) => {
    e.preventDefault()
    try {
      await billeterasApi.crear({ idUsuario: id, nombre, tipo })
      setNombre('')
      cargar()
    } catch (e) { setError(e.message) }
  }

  const desactivar = async (idBilletera) => {
    if (!confirm('Desactivar esta billetera?')) return
    try { await billeterasApi.desactivar(idBilletera); cargar() }
    catch (e) { setError(e.message) }
  }

  const activar = async (idBilletera) => {
    try { await billeterasApi.activar(idBilletera); cargar() }
    catch (e) { setError(e.message) }
  }

  const deshacerUltima = async () => {
    if (!confirm('Deshacer la ultima operacion del usuario?')) return
    try {
      const t = await transaccionesApi.deshacerUltima(id)
      setInfo(`Operacion revertida: ${t.id}`)
      setTimeout(() => setInfo(''), 4000)
      cargar()
    } catch (e) { setError(e.message) }
  }

  if (!usuario && !error) return <p className="text-slate-500">Cargando...</p>

  return (
    <>
      <Link to="/" className="text-indigo-600 hover:underline text-sm">
        ← Volver a usuarios
      </Link>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mt-4">
          {error}
        </div>
      )}
      {info && (
        <div className="bg-green-50 border border-green-200 text-green-800 px-4 py-3 rounded mt-4">
          {info}
        </div>
      )}

      {usuario && (
        <>
          <div className="bg-white p-6 rounded-lg shadow my-4">
            <div className="flex justify-between items-start">
              <div>
                <h1 className="text-2xl font-bold text-slate-800">{usuario.nombre}</h1>
                <p className="text-slate-500">{usuario.correo}</p>
                <div className="flex gap-4 mt-3 text-sm items-center">
                  <span className="text-slate-600">
                    ID: <span className="font-mono">{usuario.id}</span>
                  </span>
                  <span className="text-slate-600">{usuario.puntos} puntos</span>
                  <span className={`px-2 py-0.5 rounded text-xs font-medium ${colorPorNivel[usuario.nivel]}`}>
                    {usuario.nivel}
                  </span>
                </div>
              </div>
              <div className="text-right">
                <button
                  onClick={deshacerUltima}
                  disabled={reversibles === 0}
                  className="bg-amber-600 hover:bg-amber-700 disabled:bg-slate-300 disabled:cursor-not-allowed text-white px-3 py-2 rounded text-sm">
                  ⤺ Deshacer ultima ({reversibles})
                </button>
                <p className="text-xs text-slate-400 mt-1">Pila de reversion</p>
              </div>
            </div>
          </div>

          <form onSubmit={crear} className="bg-white p-6 rounded-lg shadow mb-6">
            <h2 className="text-xl font-semibold mb-4">Crear nueva billetera</h2>
            <div className="grid grid-cols-2 gap-4 mb-4">
              <input className="border border-slate-300 rounded px-3 py-2"
                placeholder="Nombre (ej. Ahorro vacaciones)" value={nombre}
                onChange={e => setNombre(e.target.value)} required />
              <select className="border border-slate-300 rounded px-3 py-2"
                value={tipo} onChange={e => setTipo(e.target.value)}>
                {TIPOS.map(t => (
                  <option key={t} value={t}>{t.replace('_', ' ')}</option>
                ))}
              </select>
            </div>
            <button className="bg-indigo-600 text-white px-4 py-2 rounded hover:bg-indigo-700">
              Crear billetera
            </button>
          </form>

          <div className="bg-white p-6 rounded-lg shadow">
            <h2 className="text-xl font-semibold mb-4">
              Billeteras ({billeteras.length})
            </h2>
            {billeteras.length === 0 ? (
              <p className="text-slate-400 text-center py-6">No tiene billeteras</p>
            ) : (
              <div className="grid grid-cols-2 gap-4">
                {billeteras.map(b => (
                  <Link to={`/billeteras/${b.id}`} key={b.id}
                    className={`border rounded-lg p-4 transition hover:shadow-md hover:border-indigo-300 ${b.activa ? 'border-slate-200 bg-white' : 'border-slate-200 bg-slate-50 opacity-60'}`}>
                    <div className="flex justify-between items-start mb-2">
                      <div>
                        <div className="font-medium text-slate-800">{b.nombre}</div>
                        <div className="text-xs text-slate-400 font-mono">{b.id}</div>
                      </div>
                      <span className={`text-xs px-2 py-0.5 rounded ${colorPorTipoBilletera[b.tipo] || 'bg-slate-100 text-slate-800'}`}>
                        {b.tipo.replace('_', ' ')}
                      </span>
                    </div>
                    <div className="text-2xl font-semibold text-slate-800 my-2">
                      {formatMoney(b.saldo)}
                    </div>
                    <div className="flex justify-between items-center mt-3 pt-3 border-t border-slate-100">
                      <span className={`text-xs ${b.activa ? 'text-green-600' : 'text-slate-400'}`}>
                        {b.activa ? '● Activa' : '○ Inactiva'}
                      </span>
                      <div className="flex gap-2">
                        {b.activa ? (
                          <button onClick={(e) => { e.preventDefault(); desactivar(b.id) }}
                            className="text-xs text-red-600 hover:text-red-800">
                            Desactivar
                          </button>
                        ) : (
                          <button onClick={(e) => { e.preventDefault(); activar(b.id) }}
                            className="text-xs text-green-600 hover:text-green-800">
                            Activar
                          </button>
                        )}
                        <span className="text-xs text-indigo-600">Ver detalle →</span>
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </div>
        </>
      )}
    </>
  )
}
