-- ============================================================
-- Esquema + datos semilla para PostgreSQL
-- Reemplaza a BASE_DATOS_ORIGINAL.sql (Oracle) + MIGRACION_NITIDA.sql
-- Ejecutar UNA sola vez sobre la base de datos Postgres vacía
-- (la que crea Render a partir del bloque "databases" en render.yaml).
--
-- Cómo ejecutarlo contra la base de Render:
--   psql "<External Connection String de Render>" -f POSTGRES_SCHEMA.sql
-- ============================================================

BEGIN;

-- ------------------------------------------------------------
-- Secuencias (los mismos nombres que usan las entidades JPA
-- vía @SequenceGenerator, para que Hibernate las reutilice tal cual)
-- ------------------------------------------------------------
CREATE SEQUENCE administrador_seq          START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE cliente_seq                START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE concesionaria_seq          START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE marca_seq                  START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE modelo_seq                 START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE vehiculo_seq               START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE asesor_seq                 START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE disponibilidad_seq         START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE financiacion_seq           START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE solicitudatencion_seq      START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE oportunidad_seq            START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seguimiento_seq            START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE venta_seq                  START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE solicitud_financiacion_seq START WITH 1 INCREMENT BY 1;

-- ------------------------------------------------------------
-- Tablas base
-- ------------------------------------------------------------
CREATE TABLE administradores (
    id_administrador BIGINT PRIMARY KEY,
    usuario           VARCHAR(50)  NOT NULL UNIQUE,
    contrasena        VARCHAR(100) NOT NULL,
    nombre            VARCHAR(100) NOT NULL,
    correo            VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE clientes (
    id_cliente  BIGINT PRIMARY KEY,
    nombre      VARCHAR(80)  NOT NULL,
    apellido    VARCHAR(80)  NOT NULL,
    documento   VARCHAR(30)  NOT NULL UNIQUE,
    telefono    VARCHAR(30)  NOT NULL,
    correo      VARCHAR(120),
    ciudad      VARCHAR(80),
    estado      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    usuario     VARCHAR(50)  UNIQUE,
    contrasena  VARCHAR(100)
);

CREATE TABLE concesionarias (
    id_concesionaria BIGINT PRIMARY KEY,
    nombre           VARCHAR(120) NOT NULL,
    direccion        VARCHAR(180) NOT NULL,
    telefono         VARCHAR(30)  NOT NULL,
    correo           VARCHAR(120),
    horario_atencion VARCHAR(150) NOT NULL
);

CREATE TABLE marcas (
    id_marca BIGINT PRIMARY KEY,
    nombre   VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE modelos (
    id_modelo BIGINT PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    marca_id  BIGINT NOT NULL REFERENCES marcas(id_marca)
);

CREATE TABLE vehiculos (
    id_vehiculo      BIGINT PRIMARY KEY,
    anio             INTEGER NOT NULL CHECK (anio >= 1900),
    precio           NUMERIC(14,2) NOT NULL,
    tipo             VARCHAR(50) NOT NULL,
    color            VARCHAR(40) NOT NULL,
    motor            VARCHAR(80) NOT NULL,
    transmision      VARCHAR(40) NOT NULL,
    combustible      VARCHAR(40) NOT NULL,
    descripcion      VARCHAR(500),
    cilindrada       VARCHAR(120),
    potencia         VARCHAR(120),
    torque           VARCHAR(120),
    traccion         VARCHAR(80),
    rendimiento      VARCHAR(120),
    pasajeros        VARCHAR(255),
    capacidad_baul   VARCHAR(255),
    largo            VARCHAR(255),
    ancho            VARCHAR(255),
    alto             VARCHAR(255),
    peso             VARCHAR(255),
    seguridad        VARCHAR(1000),
    equipamiento     VARCHAR(1500),
    imagen_url       VARCHAR(1000),
    estado           VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE',
    modelo_id        BIGINT NOT NULL REFERENCES modelos(id_modelo),
    concesionaria_id BIGINT NOT NULL REFERENCES concesionarias(id_concesionaria)
);

CREATE TABLE asesores (
    id_asesor           BIGINT PRIMARY KEY,
    nombre               VARCHAR(80)  NOT NULL,
    apellido             VARCHAR(80)  NOT NULL,
    telefono             VARCHAR(30)  NOT NULL,
    correo               VARCHAR(120),
    especialidad         VARCHAR(100),
    estado               VARCHAR(30)  NOT NULL DEFAULT 'ACTIVO',
    hora_inicio_trabajo  TIME,
    hora_fin_trabajo     TIME,
    usuario              VARCHAR(50)  UNIQUE,
    contrasena           VARCHAR(100),
    concesionaria_id     BIGINT NOT NULL REFERENCES concesionarias(id_concesionaria)
);

CREATE TABLE disponibilidades (
    id_disponibilidad BIGINT PRIMARY KEY,
    fecha             DATE NOT NULL,
    hora_inicio       TIME NOT NULL,
    hora_fin          TIME NOT NULL,
    estado            VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE',
    asesor_id         BIGINT NOT NULL REFERENCES asesores(id_asesor)
);

CREATE TABLE financiaciones (
    id_financiacion    BIGINT PRIMARY KEY,
    nombre             VARCHAR(100) NOT NULL,
    descripcion        VARCHAR(400),
    porcentaje_inicial NUMERIC(5,2) NOT NULL,
    plazo              INTEGER NOT NULL,
    condiciones        VARCHAR(400),
    concesionaria_id   BIGINT NOT NULL REFERENCES concesionarias(id_concesionaria)
);

CREATE TABLE solicitudes_atencion (
    id_solicitud          BIGINT PRIMARY KEY,
    fecha_solicitud       TIMESTAMP NOT NULL,
    fecha_atencion        DATE NOT NULL,
    hora_atencion         TIME NOT NULL,
    fecha_inicio_atencion TIMESTAMP,
    fecha_fin_atencion    TIMESTAMP,
    tipo_atencion         VARCHAR(100) NOT NULL,
    estado                VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    cliente_id            BIGINT NOT NULL REFERENCES clientes(id_cliente),
    vehiculo_id           BIGINT NOT NULL REFERENCES vehiculos(id_vehiculo),
    asesor_id             BIGINT REFERENCES asesores(id_asesor),
    disponibilidad_id     BIGINT REFERENCES disponibilidades(id_disponibilidad)
);

-- ------------------------------------------------------------
-- Flujo comercial (equivalente a MIGRACION_NITIDA.sql)
-- ------------------------------------------------------------
CREATE TABLE oportunidades (
    id_oportunidad         BIGINT PRIMARY KEY,
    cliente_id             BIGINT NOT NULL REFERENCES clientes(id_cliente),
    asesor_id              BIGINT NOT NULL REFERENCES asesores(id_asesor),
    vehiculo_id            BIGINT NOT NULL REFERENCES vehiculos(id_vehiculo),
    solicitud_atencion_id  BIGINT NOT NULL UNIQUE REFERENCES solicitudes_atencion(id_solicitud),
    estado                 VARCHAR(30) NOT NULL DEFAULT 'INTERESADO',
    presupuesto            NUMERIC(14,2),
    forma_pago             VARCHAR(30),
    financiacion_id        BIGINT REFERENCES financiaciones(id_financiacion),
    observaciones          VARCHAR(1000),
    motivo_perdida         VARCHAR(300),
    fecha_creacion         TIMESTAMP NOT NULL,
    fecha_actualizacion    TIMESTAMP NOT NULL
);

CREATE TABLE seguimientos (
    id_seguimiento    BIGINT PRIMARY KEY,
    oportunidad_id    BIGINT NOT NULL REFERENCES oportunidades(id_oportunidad),
    fecha_programada  DATE NOT NULL,
    medio             VARCHAR(30) NOT NULL,
    estado            VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    resultado         VARCHAR(40),
    observaciones     VARCHAR(1000),
    fecha_realizado   TIMESTAMP
);

CREATE TABLE ventas (
    id_venta         BIGINT PRIMARY KEY,
    oportunidad_id   BIGINT NOT NULL UNIQUE REFERENCES oportunidades(id_oportunidad),
    cliente_id       BIGINT NOT NULL REFERENCES clientes(id_cliente),
    asesor_id        BIGINT NOT NULL REFERENCES asesores(id_asesor),
    vehiculo_id      BIGINT NOT NULL REFERENCES vehiculos(id_vehiculo),
    precio_final     NUMERIC(14,2) NOT NULL,
    forma_pago       VARCHAR(30) NOT NULL,
    financiacion_id  BIGINT REFERENCES financiaciones(id_financiacion),
    fecha_venta      DATE NOT NULL
);

CREATE TABLE solicitudes_financiacion (
    id_solicitud_financiacion BIGINT PRIMARY KEY,
    oportunidad_id            BIGINT NOT NULL UNIQUE REFERENCES oportunidades(id_oportunidad),
    financiacion_id           BIGINT NOT NULL REFERENCES financiaciones(id_financiacion),
    monto_vehiculo            NUMERIC(14,2) NOT NULL,
    cuota_inicial             NUMERIC(14,2) NOT NULL,
    monto_solicitado          NUMERIC(14,2) NOT NULL,
    plazo_meses               INTEGER NOT NULL,
    estado                    VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    observaciones             VARCHAR(1000),
    fecha_solicitud           TIMESTAMP NOT NULL,
    fecha_actualizacion       TIMESTAMP NOT NULL
);

-- Índices para consultas frecuentes del flujo comercial
CREATE INDEX idx_oport_asesor  ON oportunidades(asesor_id);
CREATE INDEX idx_oport_cliente ON oportunidades(cliente_id);
CREATE INDEX idx_oport_estado  ON oportunidades(estado);
CREATE INDEX idx_seg_fecha     ON seguimientos(fecha_programada);
CREATE INDEX idx_solfin_estado ON solicitudes_financiacion(estado);

-- ------------------------------------------------------------
-- Datos semilla (equivalentes a los del BASE_DATOS_ORIGINAL.sql)
-- Las contraseñas ya están hasheadas con BCrypt, se copian tal cual.
-- ------------------------------------------------------------
INSERT INTO administradores (id_administrador, usuario, contrasena, nombre, correo) VALUES
(1, 'admin', '$2a$10$Wtx2oCxVdRJuF8bhR2avUuT8hSJatBTa1aM/5/X5Psd6VqzglNedG', 'Administrador Principal', 'admin@concesionaria.com');

INSERT INTO concesionarias (id_concesionaria, nombre, direccion, telefono, correo, horario_atencion) VALUES
(1, 'Concesionaria Central', 'Carrera 10 # 20-30', '3001234567', 'info@concesionariacentral.com', 'Lunes a sábado de 8:00 AM a 6:00 PM');

INSERT INTO asesores (id_asesor, nombre, apellido, telefono, correo, especialidad, estado, hora_inicio_trabajo, hora_fin_trabajo, usuario, contrasena, concesionaria_id) VALUES
(1, 'Carlos', 'Pérez',     '3001112233', 'carlos@concesionaria.com', 'Vehículos Toyota',    'ACTIVO',   '08:00:00', '18:00:00', 'carlosperez',    '$2a$10$873DZqvgr58CuAZ4ZLzYGe2JMXrGGj1QZAXvCZ/hj2cPD7djvBHpW', 1),
(2, 'Laura', 'Gómez',      '3004445566', 'laura@concesionaria.com',  'Vehículos Chevrolet', 'ACTIVO',   '08:00:00', '18:00:00', 'lauragomez',     '$2a$10$zbFQYxfv.lpDD2x7roDE6OGDtAlUcEMP58KtCdztjzYtHpSUPukBu', 1),
(3, 'Andrés','Martínez',   '3007771122', 'andres@concesionaria.com', 'Vehículos Mazda',     'INACTIVO', '08:00:00', '18:00:00', 'andresmartinez', '$2a$10$oGEbpveUztyOktC/ZuzWe.MqJ666WwFAYTVQJVUmowU54./5D49R6', 1);

INSERT INTO clientes (id_cliente, documento, telefono, apellido, ciudad, nombre, correo, contrasena, usuario, estado) VALUES
(1, '1000000001', '3009998877', 'Pérez',     'Cereté',   'Juan',     'juan@example.com',              '$2a$10$4Ky9dxvFgQcbLJgqPxDJluC0JLo3c1iqAEGl5N5mzjg/E6jyWY.fu', 'cliente1000000001', 'ACTIVO'),
(2, '1000000456', '3015557788', 'Rodríguez', 'Montería', 'Daniel',   'daniel.rodriguez@gmail.com',    '$2a$10$pSP.H4TXk//DIz5rf3JRc.tpFdkCbQpxeE1VlimPfR4sCZQVTyRwq', 'cliente1000000456', 'ACTIVO'),
(3, '1104867789', '3022100833', 'Otero',     'Tolú',     'Santiago', 'ftydytty@bugug.com',             '$2a$10$NDsPauBGqth8kgFeDXmALOLnQwydeun/c8Kach.Xq5N4gROKnSzJa', 'cliente1104867789', 'ACTIVO'),
(4, '1327277227', '3145195831', 'Corcho',    'Tolú',     'Rivi',     'sjisjisj@jushs.com',             '$2a$10$tuOstSuXhAeUdS8YnrdhhOBVhjQZOv/u6QRkggrmXZuji3/3.KRzq', 'cliente1327277227', 'ACTIVO'),
(5, '44829589',   '3022100833', 'Otero',     'Tolú',     'Atixto',   'theotercoc@gmail.com',           '$2a$10$wBHllBcr8INCogt4kd7Oa.u6jwfZxOqwuG/iu6ntGi9cOxNQ7oWge', 'atix',              'ACTIVO');

INSERT INTO marcas (id_marca, nombre) VALUES
(2, 'Chevrolet'),
(1, 'Toyota');

INSERT INTO modelos (id_modelo, marca_id, nombre) VALUES
(1, 1, 'Corolla'),
(2, 1, 'Hilux'),
(3, 2, 'Onix');

INSERT INTO financiaciones (id_financiacion, plazo, porcentaje_inicial, concesionaria_id, nombre, condiciones, descripcion) VALUES
(1, 60, 20, 1, 'Plan estándar', 'Sujeto a estudio y aprobación de crédito.', 'Financiación para compra de vehículo nuevo.');

INSERT INTO vehiculos (id_vehiculo, anio, precio, concesionaria_id, modelo_id, estado, color, combustible, transmision, tipo, motor, descripcion, alto, ancho, capacidad_baul, cilindrada, equipamiento, imagen_url, largo, pasajeros, peso, potencia, seguridad, torque, traccion, rendimiento) VALUES
(1, 2026, 98000000,  1, 1, 'DISPONIBLE', 'Blanco', 'Gasolina', 'Automática', 'Sedán',  '2.0L',        'Vehículo familiar de excelente rendimiento', '1.435 mm', '1.780 mm', '371 L',              '1.987 cc',        'Pantalla multimedia; Bluetooth; Apple CarPlay/Android Auto; aire acondicionado',                     'https://commons.wikimedia.org/wiki/Special:FilePath/2025_Toyota_Corolla_LE_ice_cap.jpg',      '4.630 mm', '5', '1.390 kg', '169 hp', '7 airbags; ABS con EBD; control de estabilidad; cámara de reversa',                          '200 Nm', 'Delantera (FWD)', 'Según versión y condiciones de conducción'),
(2, 2025, 175000000, 1, 2, 'DISPONIBLE', 'Gris',   'Diesel',   'Automática', 'Pickup', '2.8L',        'Pickup para trabajo y uso personal',        '1.815 mm', '1.855 mm', 'Caja de carga',      '2.755 cc',        'Pantalla multimedia; cámara de reversa; aire acondicionado; control crucero',                        'https://commons.wikimedia.org/wiki/Special:FilePath/Toyota_Hilux_4x2_V_Conquest_2025.jpg',     '5.325 mm', '5', '2.125 kg', '201 hp', '7 airbags; ABS; control de estabilidad; control de tracción; asistencia de descenso',        '500 Nm', '4x4',              'Según versión y condiciones de conducción'),
(3, 2026, 76000000,  1, 3, 'DISPONIBLE', 'Rojo',   'Gasolina', 'Automática', 'Sedán',  '1.0L Turbo',  'Sedán compacto y eficiente',                 '1.476 mm', '1.730 mm', '469 L',              '999 cc Turbo',    'Pantalla multimedia; Bluetooth; Apple CarPlay/Android Auto; aire acondicionado',                     'https://commons.wikimedia.org/wiki/Special:FilePath/Chevrolet_Onix_Turbo_RS_2024.jpg',         '4.163 mm', '5', '1.112 kg', '115 hp', '6 airbags; ABS; control de estabilidad; asistente de arranque en pendiente; cámara de reversa', '160 Nm', 'Delantera (FWD)', 'Según versión y condiciones de conducción');

INSERT INTO disponibilidades (id_disponibilidad, fecha, hora_inicio, hora_fin, asesor_id, estado) VALUES
(1, '2026-08-13', '09:00:00', '10:00:00', 1, 'DISPONIBLE'),
(2, '2026-08-13', '10:00:00', '11:00:00', 1, 'DISPONIBLE'),
(3, '2026-08-13', '09:00:00', '10:00:00', 2, 'DISPONIBLE'),
(4, '2026-08-13', '11:00:00', '12:00:00', 2, 'OCUPADA'),
(5, '2026-08-25', '14:00:00', '15:00:00', 3, 'OCUPADA');

INSERT INTO solicitudes_atencion (id_solicitud, fecha_atencion, hora_atencion, asesor_id, cliente_id, disponibilidad_id, fecha_solicitud, vehiculo_id, estado, tipo_atencion) VALUES
(17, '2026-08-19', '17:00:00', 1, 5, NULL, '2026-08-19 16:32:44.764241', 1, 'ATENDIENDO', 'Asesoría para compra de vehículo');

-- ------------------------------------------------------------
-- Sincronizar las secuencias con el mayor id insertado, para que
-- los próximos inserts hechos por la aplicación no choquen con estos IDs.
-- ------------------------------------------------------------
SELECT setval('administrador_seq',          (SELECT COALESCE(MAX(id_administrador), 1) FROM administradores));
SELECT setval('cliente_seq',                (SELECT COALESCE(MAX(id_cliente), 1) FROM clientes));
SELECT setval('concesionaria_seq',          (SELECT COALESCE(MAX(id_concesionaria), 1) FROM concesionarias));
SELECT setval('marca_seq',                  (SELECT COALESCE(MAX(id_marca), 1) FROM marcas));
SELECT setval('modelo_seq',                 (SELECT COALESCE(MAX(id_modelo), 1) FROM modelos));
SELECT setval('vehiculo_seq',               (SELECT COALESCE(MAX(id_vehiculo), 1) FROM vehiculos));
SELECT setval('asesor_seq',                 (SELECT COALESCE(MAX(id_asesor), 1) FROM asesores));
SELECT setval('disponibilidad_seq',         (SELECT COALESCE(MAX(id_disponibilidad), 1) FROM disponibilidades));
SELECT setval('financiacion_seq',           (SELECT COALESCE(MAX(id_financiacion), 1) FROM financiaciones));
SELECT setval('solicitudatencion_seq',      (SELECT COALESCE(MAX(id_solicitud), 1) FROM solicitudes_atencion));
-- Las siguientes tablas nacen vacías (flujo comercial), sus secuencias quedan en 1.

COMMIT;
