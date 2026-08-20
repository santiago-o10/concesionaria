# Concesionaria Web

Frontend independiente que consume la API REST.

- Frontend: `http://localhost:8080`
- Backend: `http://localhost:8081`

## Ejecutar

```bash
npm install
npm start
```

También se puede usar:

```bash
python server.py
```

## Flujo de acceso

La página es pública para consultar vehículos e información.

Para realizar acciones:
- Cliente: registrarse/iniciar sesión y solicitar asesoría.
- Asesor: iniciar sesión para ver sus citas.
- Administrador: iniciar sesión para acceder al panel administrativo.

El frontend nunca se conecta directamente a Oracle; únicamente consume la API.


## Vista Mis solicitudes
El panel del cliente muestra sus solicitudes, vehículo, fecha, hora, asesor y estado, consumiendo `/api/solicitudes/cliente/me`.


