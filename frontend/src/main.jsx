import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import App from './App.jsx'
import UsuariosPage from './pages/UsuariosPage.jsx'
import UsuarioDetallePage from './pages/UsuarioDetallePage.jsx'
import BilleteraDetallePage from './pages/BilleteraDetallePage.jsx'
import ProgramadasPage from './pages/ProgramadasPage.jsx'
import RankingPage from './pages/RankingPage.jsx'
import NotificacionesPage from './pages/NotificacionesPage.jsx'
import RedTransferenciasPage from './pages/RedTransferenciasPage.jsx'
import AnaliticaPage from './pages/AnaliticaPage.jsx'
import AuditoriaPage from './pages/AuditoriaPage.jsx'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<App />}>
          <Route index element={<UsuariosPage />} />
          <Route path="usuarios/:id" element={<UsuarioDetallePage />} />
          <Route path="billeteras/:id" element={<BilleteraDetallePage />} />
          <Route path="programadas" element={<ProgramadasPage />} />
          <Route path="ranking" element={<RankingPage />} />
          <Route path="notificaciones" element={<NotificacionesPage />} />
          <Route path="red" element={<RedTransferenciasPage />} />
          <Route path="analitica" element={<AnaliticaPage />} />
          <Route path="auditoria" element={<AuditoriaPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  </React.StrictMode>
)
