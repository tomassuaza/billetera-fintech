/**
 * Modal generico reutilizable.
 * Recibe un titulo, contenido (children) y callback onClose.
 */
export default function Modal({ open, title, onClose, children }) {
  if (!open) return null

  return (
    <div className="fixed inset-0 bg-slate-900/50 flex items-center justify-center z-50 p-4"
         onClick={onClose}>
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full"
           onClick={e => e.stopPropagation()}>
        <div className="flex justify-between items-center px-6 py-4 border-b border-slate-200">
          <h3 className="text-lg font-semibold text-slate-800">{title}</h3>
          <button onClick={onClose}
            className="text-slate-400 hover:text-slate-600 text-2xl leading-none">
            ×
          </button>
        </div>
        <div className="p-6">
          {children}
        </div>
      </div>
    </div>
  )
}
