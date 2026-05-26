import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { billeterasApi, usuariosApi } from '../api/client'
import { formatMoney, colorPorTipoBilletera } from '../utils/format'
import RecargarModal from '../components/RecargarModal'
import RetirarModal from '../components/RetirarModal'
import TransferirModal from '../components/TransferirModal'
import HistorialBilletera from '../components/HistorialBilletera'

/**
 * Pagina de detalle de billetera con operaciones (recargar, retirar,
 * transferir) y el historial de transacciones (LinkedList).
 */
export default function BilleteraDetallePage() {
  const { id } = useParams()
  const [billetera, setBilletera] = useState(null)
  const [usuario, setUsuario] = useState(null)
  const [error, setError] = useState('')
  const [info, setInfo] = useState('')
  const [refreshKey, setRefreshKey] = useState(0)

  // Estados de los modales
  const [showRecargar, setShowRecargar] = useState(false)
  const [showRetirar, setShowRetirar] = useState(false)
  const [showTransferir, setShowTransferir] = useState(false)

  const cargar = async () => {
    try {
      const b = await billeterasApi.obtener(id)
      setBilletera(b)
      const u = await usuariosApi.obtener(b.idUsuario)
      setUsuario(u)
      setError('')
    } catch (e) {
      setError(e.message)
    }
  }

  useEffect(() => { cargar() }, [id])

  const onOperacionExitosa = (mensaje) => {
    setInfo(mensaje)
    setTimeout(() => setInfo(''), 5000)
    cargar()
    setRefreshKey(k => k + 1)
  }

  if (!billetera && !error) return <p className="text-slate-500">Cargando...</p>

  return (
    <>
      {usuario && (
        <Link to={`/usuarios/${usuario.id}`} className="text-indigo-600 hover:underline text-sm">
          ← Volver a {usuario.nombre}
        </Link>
      )}

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

      {billetera && (
        <>
          {/* Tarjeta principal de la billetera */}
          <div className="bg-gradient-to-br from-indigo-600 to-indigo-800 text-white rounded-lg shadow-lg p-8 my-4">
            <div className="flex justify-between items-start mb-4">
              <div>
                <p className="text-indigo-200 text-sm">Billetera</p>
                <h1 className="text-2xl font-bold">{billetera.nombre}</h1>
                <p className="text-indigo-200 text-xs font-mono mt-1">{billetera.id}</p>
              </div>
              <span className={`px-3 py-1 rounded-full text-xs font-medium ${colorPorTipoBilletera[billetera.tipo] || 'bg-white/20 text-white'}`}>
                {billetera.tipo.replace('_', ' ')}
              </span>
            </div>
            <div className="mt-6">
              <p className="text-indigo-200 text-sm">Saldo actual</p>
              <p className="text-4xl font-bold mt-1">{formatMoney(billetera.saldo)}</p>
            </div>
            <div className="mt-4 flex justify-between items-center text-sm">
              <span className={billetera.activa ? 'text-green-300' : 'text-red-300'}>
                {billetera.activa ? '● Activa' : '○ Inactiva'}
              </span>
              {usuario && (
                <span className="text-indigo-200">Titular: {usuario.nombre}</span>
              )}
            </div>
          </div>

          {/* Botones de operaciones */}
          {billetera.activa ? (
            <div className="grid grid-cols-3 gap-4 mb-6">
              <button onClick={() => setShowRecargar(true)}
                className="bg-white border border-slate-200 hover:border-green-400 hover:bg-green-50 rounded-lg p-4 transition shadow-sm">
                <div className="text-3xl mb-1">＋</div>
                <div className="font-medium text-green-700">Recargar</div>
                <div className="text-xs text-slate-500 mt-1">Sumar saldo</div>
              </button>
              <button onClick={() => setShowRetirar(true)}
                className="bg-white border border-slate-200 hover:border-red-400 hover:bg-red-50 rounded-lg p-4 transition shadow-sm">
                <div className="text-3xl mb-1">－</div>
                <div className="font-medium text-red-700">Retirar</div>
                <div className="text-xs text-slate-500 mt-1">Sacar dinero</div>
              </button>
              <button onClick={() => setShowTransferir(true)}
                className="bg-white border border-slate-200 hover:border-indigo-400 hover:bg-indigo-50 rounded-lg p-4 transition shadow-sm">
                <div className="text-3xl mb-1">⇄</div>
                <div className="font-medium text-indigo-700">Transferir</div>
                <div className="text-xs text-slate-500 mt-1">Mismo o a otro usuario</div>
              </button>
            </div>
          ) : (
            <div className="bg-amber-50 border border-amber-200 text-amber-800 px-4 py-3 rounded mb-6">
              Esta billetera esta inactiva. Activala desde el detalle del usuario para operar.
            </div>
          )}

          {/* Historial */}
          <div className="bg-white p-6 rounded-lg shadow">
            <h2 className="text-xl font-semibold mb-4">Historial de movimientos</h2>
            <p className="text-xs text-slate-500 mb-4">
              Almacenado en una LinkedList — las transacciones mas recientes aparecen primero.
            </p>
            <HistorialBilletera idBilletera={id} refreshKey={refreshKey} />
          </div>

          {/* Modales */}
          <RecargarModal
            open={showRecargar}
            onClose={() => setShowRecargar(false)}
            billetera={billetera}
            onSuccess={onOperacionExitosa} />
          <RetirarModal
            open={showRetirar}
            onClose={() => setShowRetirar(false)}
            billetera={billetera}
            onSuccess={onOperacionExitosa} />
          <TransferirModal
            open={showTransferir}
            onClose={() => setShowTransferir(false)}
            billetera={billetera}
            onSuccess={onOperacionExitosa} />
        </>
      )}
    </>
  )
}
