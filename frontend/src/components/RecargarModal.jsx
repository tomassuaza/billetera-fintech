import { useState } from 'react'
import Modal from './Modal'
import { transaccionesApi } from '../api/client'

/**
 * Modal para recargar saldo a una billetera.
 * Llama al endpoint POST /api/transacciones/recarga
 */
export default function RecargarModal({ open, onClose, billetera, onSuccess }) {
  const [monto, setMonto] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setError(''); setLoading(true)
    try {
      const t = await transaccionesApi.recargar({
        idBilletera: billetera.id,
        monto: parseFloat(monto)
      })
      onSuccess(`Recarga exitosa: +${monto}. Puntos ganados: ${t.puntosGenerados}`)
      setMonto('')
      onClose()
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal open={open} title="Recargar saldo" onClose={onClose}>
      <form onSubmit={submit}>
        <p className="text-sm text-slate-600 mb-4">
          Billetera: <span className="font-medium">{billetera?.nombre}</span>
        </p>
        <label className="block text-sm font-medium text-slate-700 mb-1">
          Monto a recargar
        </label>
        <input
          type="number"
          step="any"
          min="0.01"
          required
          autoFocus
          value={monto}
          onChange={e => setMonto(e.target.value)}
          className="w-full border border-slate-300 rounded px-3 py-2 mb-4"
          placeholder="Ej: 50000" />

        <p className="text-xs text-slate-500 mb-4">
          Cada $100 recargados generan 1 punto al usuario.
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
            className="bg-green-600 hover:bg-green-700 disabled:bg-slate-300 text-white px-4 py-2 rounded">
            {loading ? 'Procesando...' : 'Recargar'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
