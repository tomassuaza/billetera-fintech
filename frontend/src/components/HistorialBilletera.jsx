import { useEffect, useState } from 'react'
import { transaccionesApi } from '../api/client'
import {
  formatMoney, formatDate,
  colorPorTipoTransaccion, labelTipoTransaccion
} from '../utils/format'

/**
 * Componente que muestra el historial de transacciones de una billetera.
 * Lee de la LinkedList<Transaccion> mantenida por TransaccionRepository.
 *
 * El backend devuelve las transacciones MAS RECIENTES PRIMERO (LIFO en
 * la LinkedList porque siempre se hace addFirst() al insertar).
 */
export default function HistorialBilletera({ idBilletera, refreshKey }) {
  const [historial, setHistorial] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!idBilletera) return
    setLoading(true)
    transaccionesApi.historialBilletera(idBilletera)
      .then(setHistorial)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [idBilletera, refreshKey])

  if (loading) return <p className="text-slate-400 text-sm">Cargando historial...</p>
  if (error) return <p className="text-red-600 text-sm">{error}</p>
  if (historial.length === 0) {
    return <p className="text-slate-400 text-center py-6">Sin transacciones todavia</p>
  }

  /**
   * Determina el signo y monto a mostrar segun si la billetera actual
   * es origen, destino o ambos en la transaccion.
   */
  const calcularSigno = (t) => {
    const esOrigen = t.idBilleteraOrigen === idBilletera
    const esDestino = t.idBilleteraDestino === idBilletera
    if (esDestino && !esOrigen) return { signo: '+', color: 'text-green-600' }
    if (esOrigen && !esDestino) return { signo: '-', color: 'text-red-600' }
    return { signo: '', color: 'text-slate-600' }
  }

  return (
    <ul className="divide-y divide-slate-100">
      {historial.map(t => {
        const { signo, color } = calcularSigno(t)
        const revertida = t.estado === 'REVERTIDA'
        return (
          <li key={t.id} className={`py-3 ${revertida ? 'opacity-50 line-through' : ''}`}>
            <div className="flex justify-between items-start">
              <div>
                <div className={`text-sm font-medium ${colorPorTipoTransaccion[t.tipo]}`}>
                  {labelTipoTransaccion[t.tipo] || t.tipo}
                </div>
                <div className="text-xs text-slate-500 mt-1">
                  {formatDate(t.fecha)}
                </div>
                <div className="text-xs text-slate-400 font-mono mt-0.5">
                  {t.id}
                </div>
                {t.estado !== 'EXITOSA' && t.estado !== 'PENDIENTE' && (
                  <span className="inline-block text-xs px-2 py-0.5 rounded bg-amber-100 text-amber-800 mt-1">
                    {t.estado}
                  </span>
                )}
              </div>
              <div className="text-right">
                <div className={`text-lg font-semibold ${color}`}>
                  {signo}{formatMoney(t.monto)}
                </div>
                {t.puntosGenerados > 0 && (
                  <div className="text-xs text-slate-500">+{t.puntosGenerados} pts</div>
                )}
              </div>
            </div>
          </li>
        )
      })}
    </ul>
  )
}
