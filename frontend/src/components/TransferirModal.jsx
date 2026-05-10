import { useEffect, useState } from 'react'
import Modal from './Modal'
import { transaccionesApi, billeterasApi } from '../api/client'
import { formatMoney } from '../utils/format'

/**
 * Modal para transferir entre billeteras.
 * Lista todas las billeteras del sistema (excepto la actual) como destino.
 */
export default function TransferirModal({ open, onClose, billetera, onSuccess }) {
  const [destinos, setDestinos] = useState([])
  const [idDestino, setIdDestino] = useState('')
  const [monto, setMonto] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!open) return
    billeterasApi.listar().then(lista => {
      setDestinos(lista.filter(b => b.id !== billetera?.id && b.activa))
    }).catch(e => setError(e.message))
  }, [open, billetera])

  const submit = async (e) => {
    e.preventDefault()
    setError(''); setLoading(true)
    try {
      const t = await transaccionesApi.transferir({
        idBilleteraOrigen: billetera.id,
        idBilleteraDestino: idDestino,
        monto: parseFloat(monto)
      })
      const tipo = t.tipo === 'TRANSFERENCIA_INTERNA' ? 'interna' : 'externa'
      onSuccess(`Transferencia ${tipo} exitosa. Puntos ganados: ${t.puntosGenerados}`)
      setMonto(''); setIdDestino('')
      onClose()
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal open={open} title="Transferir saldo" onClose={onClose}>
      <form onSubmit={submit}>
        <p className="text-sm text-slate-600 mb-1">
          Origen: <span className="font-medium">{billetera?.nombre}</span>
        </p>
        <p className="text-sm text-slate-600 mb-4">
          Saldo disponible: <span className="font-medium">{formatMoney(billetera?.saldo)}</span>
        </p>

        <label className="block text-sm font-medium text-slate-700 mb-1">
          Billetera destino
        </label>
        <select required value={idDestino}
          onChange={e => setIdDestino(e.target.value)}
          className="w-full border border-slate-300 rounded px-3 py-2 mb-4">
          <option value="">— Selecciona una billetera —</option>
          {destinos.map(d => (
            <option key={d.id} value={d.id}>
              {d.nombre} ({d.id}) — {formatMoney(d.saldo)}
            </option>
          ))}
        </select>

        <label className="block text-sm font-medium text-slate-700 mb-1">
          Monto
        </label>
        <input
          type="number"
          step="any"
          min="0.01"
          required
          value={monto}
          onChange={e => setMonto(e.target.value)}
          className="w-full border border-slate-300 rounded px-3 py-2 mb-4"
          placeholder="Ej: 30000" />

        <p className="text-xs text-slate-500 mb-4">
          Cada $100 transferidos generan 3 puntos al usuario origen.
          Si la billetera destino pertenece al mismo usuario sera transferencia interna.
        </p>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-3 py-2 rounded text-sm mb-4">
            {error}
          </div>
        )}

        <div className="flex gap-2 justify-end">
          <button type="button" onClick={onClose}
            className="px-4 py-2 text-slate-600 hover:text-slate-800">
            Cancelar
          </button>
          <button type="submit" disabled={loading}
            className="bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-300 text-white px-4 py-2 rounded">
            {loading ? 'Procesando...' : 'Transferir'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
