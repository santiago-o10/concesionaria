// URL Base de la API REST de Spring Boot
const API = "http://localhost:8081";

const form = document.getElementById("solicitudForm");
const nombreInput = document.getElementById("nombre");
const apellidoInput = document.getElementById("apellido");
const documentoInput = document.getElementById("documento");
const telefonoInput = document.getElementById("telefono");
const correoInput = document.getElementById("correo");
const ciudadInput = document.getElementById("ciudad");
const vehiculoSelect = document.getElementById("vehiculo");
const vehiculoInfo = document.getElementById("vehiculoInfo");
const fechaInput = document.getElementById("fecha");
const horaSelect = document.getElementById("hora");
const btnAgendar = document.getElementById("btnAgendar");
const mensaje = document.getElementById("mensaje");
const resultado = document.getElementById("resultado");
const detalleSolicitud = document.getElementById("detalleSolicitud");
const nuevaSolicitud = document.getElementById("nuevaSolicitud");

let vehiculos = [];

document.addEventListener("DOMContentLoaded", iniciar);
vehiculoSelect.addEventListener("change", mostrarVehiculo);
fechaInput.addEventListener("change", cargarDisponibilidades);
form.addEventListener("submit", crearSolicitud);
nuevaSolicitud.addEventListener("click", reiniciarFormulario);

async function iniciar() {
    establecerFechaMinima();
    await cargarVehiculos();
}

function establecerFechaMinima() {
    const ahora = new Date();
    const fecha = new Date(ahora.getTime() - ahora.getTimezoneOffset() * 60000)
        .toISOString()
        .split("T")[0];

    fechaInput.min = fecha;
    fechaInput.value = fecha;
    cargarDisponibilidades();
}

async function cargarVehiculos() {
    vehiculoSelect.disabled = true;
    vehiculoSelect.innerHTML = '<option value="">Cargando vehículos...</option>';

    try {
        const respuesta = await fetch(`${API}/api/vehiculos/estado/DISPONIBLE`);

        if (!respuesta.ok) {
            throw new Error(`No se pudieron cargar los vehículos (HTTP ${respuesta.status}).`);
        }

        vehiculos = await respuesta.json();

        vehiculoSelect.innerHTML = '<option value="">Selecciona un vehículo</option>';

        vehiculos.forEach(vehiculo => {
            const marca = vehiculo.modelo?.marca?.nombre || "";
            const modelo = vehiculo.modelo?.nombre || "Modelo";
            const texto = `${marca} ${modelo}`.trim();

            const option = document.createElement("option");
            option.value = vehiculo.idVehiculo;
            option.textContent = `${texto} • ${vehiculo.anio}`;
            vehiculoSelect.appendChild(option);
        });

        if (vehiculos.length === 0) {
            vehiculoSelect.innerHTML = '<option value="">No hay vehículos disponibles</option>';
        } else {
            vehiculoSelect.disabled = false;
        }
    } catch (error) {
        vehiculoSelect.innerHTML = '<option value="">No se pudieron cargar los vehículos</option>';
        mostrarMensaje(
            "No se pudo conectar con la API backend (http://localhost:8081). Asegúrate de que la aplicación Spring Boot esté corriendo.",
            true
        );
        console.error(error);
    }
}

function mostrarVehiculo() {
    const id = Number(vehiculoSelect.value);
    const vehiculo = vehiculos.find(item => item.idVehiculo === id);

    if (!vehiculo) {
        vehiculoInfo.classList.add("oculto");
        vehiculoInfo.innerHTML = "";
        return;
    }

    const marca = vehiculo.modelo?.marca?.nombre || "";
    const modelo = vehiculo.modelo?.nombre || "";
    const precio = vehiculo.precio != null
        ? new Intl.NumberFormat("es-CO", {
            style: "currency",
            currency: "COP",
            maximumFractionDigits: 0
        }).format(Number(vehiculo.precio))
        : "Precio no disponible";

    vehiculoInfo.innerHTML = `
        <div>
            <strong>${escaparHtml(`${marca} ${modelo}`.trim())}</strong>
            <span>${escaparHtml(String(vehiculo.anio))}</span>
        </div>
        <strong>${escaparHtml(precio)}</strong>
    `;
    vehiculoInfo.classList.remove("oculto");
}

async function cargarDisponibilidades() {
    const fecha = fechaInput.value;

    horaSelect.disabled = true;
    horaSelect.innerHTML = '<option value="">Cargando horarios...</option>';

    if (!fecha) {
        horaSelect.innerHTML = '<option value="">Selecciona una fecha</option>';
        return;
    }

    try {
        const respuesta = await fetch(
            `${API}/api/solicitudes/horarios-disponibles/${fecha}`
        );

        if (!respuesta.ok) {
            throw new Error(
                `No se pudieron cargar los horarios (HTTP ${respuesta.status}).`
            );
        }

        const horarios = await respuesta.json();

        horaSelect.innerHTML = '<option value="">Selecciona una hora</option>';

        horarios.forEach(hora => {
            const inicio = hora.substring(0, 5);
            const fin = sumarHoras(hora, 1);

            const option = document.createElement("option");
            option.value = inicio;
            option.textContent = `${formatearHora(inicio)} - ${formatearHora(fin)}`;
            horaSelect.appendChild(option);
        });

        if (horarios.length === 0) {
            horaSelect.innerHTML =
                '<option value="">No hay horarios disponibles para esta fecha</option>';
        } else {
            horaSelect.disabled = false;
        }
    } catch (error) {
        horaSelect.innerHTML =
            '<option value="">Error al cargar horarios</option>';
        mostrarMensaje(
            "No se pudieron cargar los horarios disponibles desde la API.",
            true
        );
        console.error(error);
    }
}

function sumarHoras(hora, cantidad) {
    const partes = hora.substring(0, 5).split(":");
    const minutosTotales =
        Number(partes[0]) * 60 +
        Number(partes[1]) +
        cantidad * 60;

    const horas = Math.floor(minutosTotales / 60) % 24;
    const minutos = minutosTotales % 60;

    return `${String(horas).padStart(2, "0")}:${String(minutos).padStart(2, "0")}`;
}

async function crearSolicitud(evento) {
    evento.preventDefault();
    limpiarMensaje();

    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const datosCliente = {
        nombre: nombreInput.value.trim(),
        apellido: apellidoInput.value.trim(),
        documento: documentoInput.value.trim(),
        telefono: telefonoInput.value.trim(),
        correo: correoInput.value.trim() || null,
        ciudad: ciudadInput.value.trim() || null
    };

    const cliente = {
        ...datosCliente
    };

    const solicitud = {
        fechaAtencion: fechaInput.value,
        horaAtencion: horaSelect.value,
        tipoAtencion: "Asesoría para compra de vehículo",
        cliente: null,
        vehiculo: {
            idVehiculo: Number(vehiculoSelect.value)
        }
    };

    btnAgendar.disabled = true;
    btnAgendar.innerHTML = 'Registrando y agendando... <span>⏳</span>';

    try {
        // 1. Registro o verificación del cliente en la API
        const respuestaCliente = await fetch(`${API}/api/clientes`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(cliente)
        });

        const datosClienteGuardado = await respuestaCliente.json().catch(() => ({}));

        if (!respuestaCliente.ok) {
            throw new Error(obtenerMensajeError(
                datosClienteGuardado,
                respuestaCliente.status
            ));
        }

        solicitud.cliente = {
            idCliente: datosClienteGuardado.idCliente
        };

        // 2. Creación de la solicitud de atención en la API
        const respuestaSolicitud = await fetch(`${API}/api/solicitudes`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(solicitud)
        });

        const datosSolicitud = await respuestaSolicitud.json().catch(() => ({}));

        if (!respuestaSolicitud.ok) {
            throw new Error(obtenerMensajeError(
                datosSolicitud,
                respuestaSolicitud.status
            ));
        }

        mostrarResultado(datosSolicitud, datosClienteGuardado);
    } catch (error) {
        mostrarMensaje(error.message || "No fue posible completar la solicitud.", true);
        console.error(error);
    } finally {
        btnAgendar.disabled = false;
        btnAgendar.innerHTML = 'Agendar mi cita <span>→</span>';
    }
}

function mostrarResultado(solicitud, cliente) {
    const vehiculo = solicitud.vehiculo?.modelo
        ? `${solicitud.vehiculo.modelo.marca?.nombre || ""} ${solicitud.vehiculo.modelo.nombre || ""}`.trim()
        : obtenerNombreVehiculoSeleccionado();

    const asesor = solicitud.asesor
        ? `${solicitud.asesor.nombre || ""} ${solicitud.asesor.apellido || ""}`.trim()
        : "Asignado automáticamente";

    detalleSolicitud.innerHTML = `
        <div class="detalle-item">
            <span>Cliente</span>
            <strong>${escaparHtml(`${cliente.nombre} ${cliente.apellido}`)}</strong>
        </div>
        <div class="detalle-item">
            <span>Vehículo</span>
            <strong>${escaparHtml(vehiculo)}</strong>
        </div>
        <div class="detalle-item">
            <span>Fecha</span>
            <strong>${escaparHtml(solicitud.fechaAtencion || fechaInput.value)}</strong>
        </div>
        <div class="detalle-item">
            <span>Hora</span>
            <strong>${escaparHtml(formatearHora(solicitud.horaAtencion || horaSelect.value))}</strong>
        </div>
        <div class="detalle-item">
            <span>Asesor asignado</span>
            <strong>${escaparHtml(asesor)}</strong>
        </div>
        <div class="detalle-item">
            <span>Estado</span>
            <strong class="estado">${escaparHtml(solicitud.estado || "PENDIENTE")}</strong>
        </div>
    `;

    form.classList.add("oculto");
    resultado.classList.remove("oculto");
    window.scrollTo({ top: 0, behavior: "smooth" });
}

function obtenerNombreVehiculoSeleccionado() {
    const seleccionado = vehiculos.find(
        item => item.idVehiculo === Number(vehiculoSelect.value)
    );

    if (!seleccionado) return "Vehículo seleccionado";

    const marca = seleccionado.modelo?.marca?.nombre || "";
    const modelo = seleccionado.modelo?.nombre || "";
    return `${marca} ${modelo}`.trim();
}

function reiniciarFormulario() {
    resultado.classList.add("oculto");
    form.classList.remove("oculto");
    form.reset();
    vehiculoInfo.classList.add("oculto");
    establecerFechaMinima();
    limpiarMensaje();
}

function mostrarMensaje(texto, esError = false) {
    mensaje.textContent = texto;
    mensaje.className = `mensaje ${esError ? "error" : "exito"}`;
}

function limpiarMensaje() {
    mensaje.textContent = "";
    mensaje.className = "mensaje";
}

function obtenerMensajeError(datos, status) {
    if (datos?.mensaje) return datos.mensaje;
    if (datos?.message) return datos.message;

    if (datos?.errores && typeof datos.errores === "object") {
        return Object.values(datos.errores).join(" ");
    }

    if (status === 409) {
        return "Ya existe un cliente registrado con ese número de documento.";
    }

    return `No se pudo completar la operación (HTTP ${status}).`;
}

function formatearHora(hora) {
    if (!hora) return "";

    const [horas, minutos] = hora.split(":");
    const h = Number(horas);
    const periodo = h >= 12 ? "PM" : "AM";
    const hora12 = h % 12 || 12;

    return `${String(hora12).padStart(2, "0")}:${minutos} ${periodo}`;
}

function escaparHtml(valor) {
    return String(valor)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
