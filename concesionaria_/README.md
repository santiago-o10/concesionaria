# Concesionaria - versión gestión comercial

## Flujo principal
Cliente -> cita -> iniciar asesoría -> finalizar -> resultado -> oportunidad -> seguimiento / financiación -> venta o pérdida.

## Base de datos
El proyecto usa **PostgreSQL**. Ejecuta `POSTGRES_SCHEMA.sql` una sola vez sobre una
base de datos Postgres vacía (crea las secuencias, las tablas y los datos semilla):

```bash
psql "<connection string de tu base Postgres>" -f POSTGRES_SCHEMA.sql
```

Verifica que existan las tablas `oportunidades`, `seguimientos`, `ventas` y
`solicitudes_financiacion` además de las tablas base (`clientes`, `vehiculos`, etc.).

Los archivos `BASE_DATOS_ORIGINAL.sql` y `MIGRACION_NITIDA.sql` corresponden a la
versión anterior en Oracle y se mantienen solo como referencia histórica; no se usan
más para desplegar.

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
El backend requiere Java + Maven y una instancia PostgreSQL compatible con el esquema
(`POSTGRES_SCHEMA.sql`). El frontend es estático y consume la URL de la API que le
indique la variable `API_URL` (ver `concesionaria-web/server.js`); en local cae por
defecto a `http://localhost:8081`.

## Docker y Render

Construye y ejecuta la API localmente con:

```bash
docker compose up --build
```

`docker-compose.yml` levanta un contenedor de Postgres junto con la API. En Render,
`render.yaml` crea automáticamente una base Postgres administrada (bloque `databases`)
y conecta la API a ella mediante `DB_HOST`, `DB_PORT`, `DB_NAME`,
`SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` (todas generadas por Render,
no hay que llenarlas a mano). `APP_JWT_SECRET` también se autogenera.

Si en cambio quieres apuntar a una base Postgres externa (no la de Render), define
`SPRING_DATASOURCE_URL` manualmente con formato `jdbc:postgresql://host:puerto/basededatos`
— esta variable, si existe, tiene prioridad sobre `DB_HOST`/`DB_PORT`/`DB_NAME`.

Render proporciona automáticamente `PORT`; la aplicación lo utiliza y conserva `8081` como valor local predeterminado.

Recuerda ejecutar `POSTGRES_SCHEMA.sql` una vez contra la base antes del primer deploy
(ver sección "Base de datos" arriba) — con `spring.jpa.hibernate.ddl-auto=none` la
aplicación no crea las tablas por sí sola.
