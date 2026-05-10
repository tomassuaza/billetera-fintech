import axios from 'axios'

export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' }
})

// Interceptor para extraer mensajes de error del backend
api.interceptors.response.use(
  r => r,
  err => {
    const msg = err.response?.data?.error || err.message || 'Error desconocido'
    return Promise.reject(new Error(msg))
  }
)

export const usuariosApi = {
  listar: () => api.get('/usuarios').then(r => r.data),
  obtener: (id) => api.get(`/usuarios/${id}`).then(r => r.data),
  registrar: (data) => api.post('/usuarios', data).then(r => r.data),
  eliminar: (id) => api.delete(`/usuarios/${id}`)
}

export const billeterasApi = {
  listar: () => api.get('/billeteras').then(r => r.data),
  obtener: (id) => api.get(`/billeteras/${id}`).then(r => r.data),
  listarPorUsuario: (idUsuario) => api.get(`/billeteras/usuario/${idUsuario}`).then(r => r.data),
  crear: (data) => api.post('/billeteras', data).then(r => r.data),
  desactivar: (id) => api.post(`/billeteras/${id}/desactivar`).then(r => r.data),
  activar: (id) => api.post(`/billeteras/${id}/activar`).then(r => r.data),
}

export const transaccionesApi = {
  recargar: (data) => api.post('/transacciones/recarga', data).then(r => r.data),
  retirar: (data) => api.post('/transacciones/retiro', data).then(r => r.data),
  transferir: (data) => api.post('/transacciones/transferencia', data).then(r => r.data),
  obtener: (id) => api.get(`/transacciones/${id}`).then(r => r.data),
  historialBilletera: (idBilletera) => api.get(`/transacciones/billetera/${idBilletera}`).then(r => r.data),
  historialUsuario: (idUsuario) => api.get(`/transacciones/usuario/${idUsuario}`).then(r => r.data),
  revertir: (id) => api.post(`/transacciones/${id}/reversion`).then(r => r.data),
  deshacerUltima: (idUsuario) => api.post(`/transacciones/usuario/${idUsuario}/deshacer`).then(r => r.data),
  contarReversibles: (idUsuario) => api.get(`/transacciones/usuario/${idUsuario}/reversibles`).then(r => r.data),
}

export const programadasApi = {
  programar: (data) => api.post('/programadas', data).then(r => r.data),
  listarPendientes: () => api.get('/programadas').then(r => r.data),
  listarPorUsuario: (idUsuario) => api.get(`/programadas/usuario/${idUsuario}`).then(r => r.data),
  ejecutarVencidas: () => api.post('/programadas/ejecutar-vencidas').then(r => r.data),
  ejecutar: (id) => api.post(`/programadas/${id}/ejecutar`).then(r => r.data),
  cancelar: (id) => api.post(`/programadas/${id}/cancelar`).then(r => r.data),
}

export const fidelizacionApi = {
  topN: (n) => api.get(`/fidelizacion/top/${n}`).then(r => r.data),
  enRango: (min, max) => api.get(`/fidelizacion/rango?min=${min}&max=${max}`).then(r => r.data),
  porNivel: (nivel) => api.get(`/fidelizacion/nivel/${nivel}`).then(r => r.data),
  conteo: () => api.get('/fidelizacion/conteo').then(r => r.data),
}
