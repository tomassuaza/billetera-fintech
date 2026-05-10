# Instrucciones para probar la entrega del dia 7

Esta entrega cubre los dias 1 al 7. Reemplaza completamente la version
anterior — usa una carpeta limpia para evitar el problema de macOS que
renombra la carpeta como "billetera-fintech 2".

## 1. Setup limpio (importante)

1. Si tienes una version anterior, borrala o renombrala:
   ```bash
   mv ~/Downloads/billetera-fintech ~/Downloads/billetera-fintech-old
   ```
2. Descomprime el zip nuevo.
3. Abrela en VS Code: `code billetera-fintech`

## 2. Levantar backend

```bash
cd backend
mvn spring-boot:run
```

Espera el mensaje "Started BilleteraApplication". Quedara en :8080.

## 3. Levantar frontend (en otra terminal)

```bash
cd frontend
npm install   # solo la primera vez
npm run dev
```

Quedara en :5173.

## 4. Plan de pruebas

### A. Crear datos
1. Abre http://localhost:5173
2. Registra dos usuarios (ej. "Juan Lopez", "Maria Garcia")
3. Entra al detalle de Juan, crea dos billeteras: una de Ahorro y una de Gastos diarios
4. Entra al detalle de Maria, crea una billetera de Compras

### B. Probar transacciones
5. Abre el detalle de la billetera de Ahorro de Juan
6. Recarga $50.000 → veras el saldo y la transaccion en el historial
7. Recarga otra vez $30.000 → veras 2 transacciones en historial
8. Retira $10.000 → falla porque la billetera tiene poco saldo? Pruebalo.
9. Transfiere desde Ahorro de Juan a Gastos diarios de Juan ($20.000) →
   debe ser TRANSFERENCIA_INTERNA en el historial
10. Transfiere desde Ahorro de Juan a Compras de Maria ($15.000) →
    debe ser TRANSFERENCIA_EXTERNA

### C. Probar reversion
11. Vuelve al detalle de Juan. Veras "Deshacer ultima (N)" donde N es la
    cantidad de operaciones reversibles
12. Click en deshacer → la ultima transaccion se revierte y se crea un
    movimiento "Reversion" en el historial. El saldo se ajusta.

### D. Probar puntos
13. Mira el contador de puntos de Juan tras varias operaciones (recarga
    da 1pt/100, retiro 2pt/100, transferencia 3pt/100)
14. Ve a la pagina **Ranking** desde el menu — Juan deberia aparecer arriba
15. Prueba el filtro por rango (ej. min=0, max=500)

### E. Probar programadas
16. Ve a **Programadas** desde el menu
17. Programa una RECARGA para fecha pasada (ej. ayer)
18. Programa otra RECARGA para fecha futura (ej. dentro de 1 hora)
19. Click en **Ejecutar vencidas** — solo se ejecuta la que ya paso
20. La que esta en futuro queda pendiente. Pruebala con boton **Ejecutar**

## 5. Que validar visualmente

- El historial siempre muestra mas reciente arriba (LinkedList)
- Las operaciones revertidas aparecen tachadas
- El nivel del usuario cambia al pasar 500 / 1000 / 5000 puntos
- En Programadas, las pendientes aparecen ordenadas por fecha (PriorityQueue)
- En Ranking, top N va de mayor a menor puntos (TreeMap)

## 6. Si algo no funciona

- Asegurate de que el backend este corriendo (`mvn spring-boot:run`)
- Mira la consola del navegador (F12) para ver errores de red
- Mira la consola del backend para errores de Java
- Si `npm install` falla, intenta borrar `node_modules/` y reintentar
