/**
 * Utilidades de formato compartidas en el frontend.
 */

export const formatMoney = (v) =>
  new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0
  }).format(v || 0)

export const formatDate = (s) => {
  if (!s) return ''
  const d = new Date(s)
  return d.toLocaleString('es-CO', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

export const colorPorTipoBilletera = {
  AHORRO: 'bg-green-100 text-green-800',
  GASTOS_DIARIOS: 'bg-blue-100 text-blue-800',
  COMPRAS: 'bg-purple-100 text-purple-800',
  TRANSPORTE: 'bg-amber-100 text-amber-800',
  INVERSION: 'bg-pink-100 text-pink-800',
}

export const colorPorTipoTransaccion = {
  RECARGA: 'text-green-600',
  RETIRO: 'text-red-600',
  TRANSFERENCIA_INTERNA: 'text-blue-600',
  TRANSFERENCIA_EXTERNA: 'text-indigo-600',
  REVERSION: 'text-amber-600',
}

export const labelTipoTransaccion = {
  RECARGA: 'Recarga',
  RETIRO: 'Retiro',
  TRANSFERENCIA_INTERNA: 'Transferencia interna',
  TRANSFERENCIA_EXTERNA: 'Transferencia externa',
  REVERSION: 'Reversion',
}

export const colorPorNivel = {
  BRONCE: 'bg-amber-100 text-amber-800',
  PLATA: 'bg-slate-200 text-slate-800',
  ORO: 'bg-yellow-100 text-yellow-800',
  PLATINO: 'bg-indigo-100 text-indigo-800',
}

export const colorPorTipoNotificacion = {
  BIENVENIDA: 'bg-indigo-50 text-indigo-700 border-indigo-200',
  SALDO_BAJO: 'bg-amber-50 text-amber-700 border-amber-200',
  ASCENSO_NIVEL: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  OPERACION_RECHAZADA: 'bg-red-50 text-red-700 border-red-200',
  PROGRAMADA_EJECUTADA: 'bg-blue-50 text-blue-700 border-blue-200',
  PROGRAMADA_FALLIDA: 'bg-orange-50 text-orange-700 border-orange-200',
  FRAUDE_DETECTADO: 'bg-rose-50 text-rose-700 border-rose-200',
}

export const labelTipoNotificacion = {
  BIENVENIDA: 'Bienvenida',
  SALDO_BAJO: 'Saldo bajo',
  ASCENSO_NIVEL: 'Ascenso de nivel',
  OPERACION_RECHAZADA: 'Operacion rechazada',
  PROGRAMADA_EJECUTADA: 'Programada ejecutada',
  PROGRAMADA_FALLIDA: 'Programada fallida',
  FRAUDE_DETECTADO: 'Fraude detectado',
}
