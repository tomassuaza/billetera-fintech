import { useEffect, useMemo, useState } from 'react'
import Modal from './Modal'
import { transaccionesApi, billeterasApi, usuariosApi } from '../api/client'
import { formatMoney } from '../utils/format'

/**
 * Modal para transferir entre billeteras. Lista todas las billeteras
 * activas del sistema (excepto la actual), agrupadas por titular, para
 * que sea evidente cuales son del mismo usuario y cuales pertenecen a
 * otros (transferencia interna vs externa).
 */
export default function TransferirModal({ open, onClose, billetera, onSuccess }) {
  const [destinos, setDestinos] = useState([])
  const [usuarios, setUsuarios] = useState([])
  const [idDestino, setIdDestino] = useState('')
  const [monto, setMonto] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!open) return
    setError('')
    Promise.all([billeterasApi.listar(), usuariosApi.listar()])
      .then(([listaBilleteras, listaUsuarios]) => {
        setDestinos(listaBilleteras.filter(b => b.id !== billetera?.id && b.activa))
        setUsuarios(listaUsuarios)
      })
      .catch(e => setError(e.message))
  }, [open, billetera])

  const grupos = useMemo(() => {
    const nombrePorId = new Map(usuarios.map(u => [u.id, u.nombre]))
    const mias = []
    const otras = []
    destinos.forEach(b => {
      const item = { ...b, nombreTitular: nombrePorId.get(b.idUsuario) || b.idUsuario }
      if (b.idUsuario === billetera?.idUsuario) mias.push(item)
      else otras.push(item)
    })
    // Ordenar las de otros usuarios por nombre del titular
    otras.sort((a, b) => a.nombreTitular.localeCompare(b.nombreTitular))
    return { mias, otras }
  }, [destinos, usuarios, billetera])

  const seleccionada = destinos.find(d => d.id === idDestino)
  const esExterna = seleccionada && seleccionada.idUsuario !== billetera?.idUsuario

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
          className="w-full border border-slate-300 rounded px-3 py-2 mb-2">
          <option value="">— Selecciona una billetera —</option>
          {grupos.mias.length > 0 && (
            <optgroup label="Mis billeteras (transferencia interna)">
              {grupos.mias.map(d => (
                <option key={d.id} value={d.id}>
                  {d.nombre} — {formatMoney(d.saldo)}
                </option>
              ))}
            </optgroup>
          )}
          {grupos.otras.length > 0 && (
            <optgroup label="Billeteras de otros usuarios (transferencia externa)">
              {grupos.otras.map(d => (
                <option key={d.id} value={d.id}>
                  {d.nombreTitular} — {d.nombre} — {formatMoney(d.saldo)}
                </option>
              ))}
            </optgroup>
          )}
        </select>

        {seleccionada && (
          <p className={`text-xs mb-3 ${esExterna ? 'text-indigo-600' : 'text-slate-500'}`}>
            {esExterna
              ? `Transferencia externa hacia ${seleccionada.idUsuario && grupos.otras.find(o => o.id === seleccionada.id)?.nombreTitular}. Se registrara en la red de transferencias.`
              : 'Transferencia interna entre billeteras del mismo usuario.'}
          </p>
        )}

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
