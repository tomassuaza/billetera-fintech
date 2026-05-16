import { Outlet, Link, NavLink } from 'react-router-dom'

export default function App() {
  const linkClass = ({ isActive }) =>
    `text-sm font-medium transition ${
      isActive ? 'text-indigo-600' : 'text-slate-600 hover:text-indigo-600'
    }`

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b border-slate-200 sticky top-0 z-10">
        <div className="max-w-6xl mx-auto px-8 py-4 flex justify-between items-center">
          <Link to="/" className="text-2xl font-bold text-slate-800">
            Billetera Fintech
          </Link>
          <nav className="flex gap-6">
            <NavLink to="/" className={linkClass} end>Usuarios</NavLink>
            <NavLink to="/programadas" className={linkClass}>Programadas</NavLink>
            <NavLink to="/ranking" className={linkClass}>Ranking</NavLink>
            <NavLink to="/notificaciones" className={linkClass}>Notificaciones</NavLink>
          </nav>
        </div>
      </header>
      <main className="max-w-6xl mx-auto px-8 py-8">
        <Outlet />
      </main>
    </div>
  )
}
