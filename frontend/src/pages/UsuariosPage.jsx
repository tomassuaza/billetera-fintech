import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { usuariosApi } from '../api/client'
import { colorPorNivel } from '../utils/format'

export default function UsuariosPage() {
  const [usuarios, setUsuarios] = useState([])
  const [nombre, setNombre] = useState('')
  const [correo, setCorreo] = useState('')
  const [error, setError] = useState('')

  const cargar = async () => {
    try {
      setUsuarios(await usuariosApi.listar())
      setError('')
    } catch (e) {
      setError(e.message || 'No se pudo conectar al backend en :8081')
    }
  }

  useEffect(() => { cargar() }, [])

  const registrar = async (e) => {
    e.preventDefault()
    try {
      await usuariosApi.registrar({ nombre, correo })
      setNombre(''); setCorreo('')
      cargar()
    } catch (e) {
      setError(e.message)
    }
  }

  const eliminar = async (id, e) => {
    e.preventDefault()
    e.stopPropagation()
    if (!confirm('Eliminar este usuario?')) return
    try {
      await usuariosApi.eliminar(id)
      cargar()
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <>
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      <form onSubmit={registrar} className="bg-white p-6 rounded-lg shadow mb-6">
        <h2 className="text-xl font-semibold mb-4">Registrar usuario</h2>
        <div className="grid grid-cols-2 gap-4 mb-4">
          <input className="border border-slate-300 rounded px-3 py-2"
            placeholder="Nombre" value={nombre}
            onChange={e => setNombre(e.target.value)} required />
          <input className="border border-slate-300 rounded px-3 py-2"
            placeholder="Correo" type="email" value={correo}
            onChange={e => setCorreo(e.target.value)} required />
        </div>
        <button className="bg-indigo-600 text-white px-4 py-2 rounded hover:bg-indigo-700">
          Registrar
        </button>
      </form>

      <div className="bg-white p-6 rounded-lg shadow">
        <h2 className="text-xl font-semibold mb-4">Usuarios ({usuarios.length})</h2>
        {usuarios.length === 0 ? (
          <p className="text-slate-400 text-center py-6">Aun no hay usuarios</p>
        ) : (
          <ul className="divide-y">
            {usuarios.map(u => (
              <li key={u.id}>
                <Link to={`/usuarios/${u.id}`}
                  className="flex justify-between items-center hover:bg-slate-50 px-3 py-3 rounded transition -mx-3">
                  <div>
                    <div className="font-medium text-slate-800">{u.nombre}</div>
                    <div className="text-sm text-slate-500">{u.correo}</div>
                    <div className="text-xs text-slate-400 font-mono">ID: {u.id}</div>
                  </div>
                  <div className="text-right flex items-center gap-4">
                    <div>
                      <div className="text-sm font-semibold">{u.puntos} pts</div>
                      <span className={`inline-block text-xs px-2 py-0.5 rounded ${colorPorNivel[u.nivel] || ''}`}>
                        {u.nivel}
                      </span>
                      <div className="text-xs text-slate-400 mt-1">
                        {u.idsBilleteras?.length || 0} billeteras
                      </div>
                    </div>
                    <button onClick={(e) => eliminar(u.id, e)}
                      className="text-red-600 hover:text-red-800 text-sm">Eliminar</button>
                  </div>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </div>
    </>
  )
}
