# Concesionaria - versión gestión comercial

## Flujo principal
Cliente -> cita -> iniciar asesoría -> finalizar -> resultado -> oportunidad -> seguimiento / financiación -> venta o pérdida.

## Base de datos
1. Ejecuta `MIGRACION_NITIDA.sql` en Oracle sobre el esquema que contiene las tablas originales.
2. No ejecutes las migraciones antiguas encima de la nueva.
3. Verifica que existan las tablas `OPORTUNIDADES`, `SEGUIMIENTOS`, `VENTAS` y `SOLICITUDES_FINANCIACION`.

## Reglas principales
- Citas de 1 hora.
- Se pueden reservar el mismo día.
- Los horarios pasados u ocupados no se muestran.
- El backend vuelve a validar disponibilidad al reservar.
- El asesor es quien inicia/finaliza su atención.
- El administrador supervisa, pero no puede iniciar, finalizar ni marcar inasistencia de una cita.
- Una asesoría finalizada debe tener resultado comercial.
- Financiación: solicitud pendiente -> en estudio -> aprobada/rechazada. Solo una financiación aprobada permite registrar una venta financiada.
- Un vehículo vendido pasa a `VENDIDO`.

## Validación local
El backend requiere Java + Maven y una instancia Oracle compatible con el esquema. El frontend es estático y consume `http://localhost:8081`.

## Docker y Render

Construye y ejecuta la API localmente con:

```bash
docker compose up --build
```

La base de datos Oracle debe estar disponible desde el contenedor. En local, el archivo Compose apunta por defecto a `host.docker.internal:1521/XE`. En Render, crea un servicio web usando el `Dockerfile` de este repositorio y configura estas variables de entorno con los datos de una Oracle accesible externamente:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_JWT_SECRET`

Render proporciona automáticamente `PORT`; la aplicación lo utiliza y conserva `8081` como valor local predeterminado.
