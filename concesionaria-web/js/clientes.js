
let clientesAdminCache = [];

async function cargarClientesAdmin() {
  const s = sesion();
  if (!s || String(s.rol).toUpperCase() !== "ADMINISTRADOR") return;

  const box = document.getElementById("listaClientesAdmin");
  if (!box) return;
  box.innerHTML = `<div class="panel">Cargando clientes...</div>`;

  try {
    const respuesta = await api("/api/clientes");
    clientesAdminCache = Array.isArray(respuesta)
      ? respuesta
      : (Array.isArray(respuesta?.content) ? respuesta.content : []);
    renderClientesAdmin(clientesAdminCache);
  } catch (e) {
    box.innerHTML = `<div class="panel error">No fue posible cargar los clientes: ${esc(e.message)}</div>`;
  }
}

function renderClientesAdmin(lista) {
  const box = document.getElementById("listaClientesAdmin");
  const resumen = document.getElementById("clientesResumen");
  if (!box) return;

  if (resumen) {
    resumen.innerHTML = `
      <div><strong>${clientesAdminCache.length}</strong><span>clientes registrados</span></div>
      <div><strong>${lista.length}</strong><span>coincidencias</span></div>
    `;
  }

  if (!lista.length) {
    box.innerHTML = `
      <div class="panel agenda-vacia">
        <div class="agenda-icon">👤</div>
        <h3>No hay clientes para mostrar</h3>
        <p>Prueba con otro término de búsqueda.</p>
      </div>`;
    return;
  }

  box.innerHTML = lista.map(c => `
    <article class="admin-item">
      <div class="admin-item-info">
        <p class="eyebrow">CLIENTE</p>
        <h3>${esc(`${c.nombre || ""} ${c.apellido || ""}`.trim() || "Sin nombre")}</h3>
        <p>Documento: ${esc(c.documento || "—")}</p>
        <p>${esc(c.correo || "Sin correo")} · ${esc(c.telefono || "Sin teléfono")}</p>
        <p>${esc(c.ciudad || "Sin ciudad")} · Usuario: ${esc(c.usuario || "—")}</p>
        <span class="admin-estado ${String(c.estado||"ACTIVO").toUpperCase()==="ACTIVO" ? "activo" : "inactivo"}">${esc(c.estado || "ACTIVO")}</span>
      </div>
      <div class="admin-item-acciones">
        <button class="btn secundario pequeño" onclick="editarClienteAdmin(${Number(c.idCliente ?? c.id_cliente ?? c.id)})">Editar</button>
        <button class="btn ${String(c.estado||"ACTIVO").toUpperCase()==="ACTIVO" ? "peligro" : "principal"} pequeño" onclick="cambiarEstadoClienteAdmin(${Number(c.idCliente ?? c.id_cliente ?? c.id)}, '${String(c.estado||"ACTIVO").toUpperCase()==="ACTIVO" ? "INACTIVO" : "ACTIVO"}')">${String(c.estado||"ACTIVO").toUpperCase()==="ACTIVO" ? "Desactivar" : "Activar"}</button>
      </div>
    </article>
  `).join("");
}

function abrirFormularioClienteAdmin(cliente=null) {
  const panel=document.getElementById("clienteFormPanel");
  if(!panel) return;

  document.getElementById("clienteAdminForm")?.reset();
  document.getElementById("clienteAdminMensaje").textContent="";
  document.getElementById("clienteFormTitulo").textContent=cliente ? "Editar cliente" : "Nuevo cliente";
  document.getElementById("clienteAdminId").value=cliente?.idCliente || "";

  if(cliente){
    document.getElementById("clienteNombre").value=cliente.nombre || "";
    document.getElementById("clienteApellido").value=cliente.apellido || "";
    document.getElementById("clienteDocumento").value=cliente.documento || "";
    document.getElementById("clienteCiudad").value=cliente.ciudad || "";
    document.getElementById("clienteTelefono").value=cliente.telefono || "";
    document.getElementById("clienteCorreo").value=cliente.correo || "";
    document.getElementById("clienteUsuario").value=cliente.usuario || "";
    document.getElementById("clienteContrasena").value="";
    if(document.getElementById("clienteEstado")) document.getElementById("clienteEstado").value=cliente.estado || "ACTIVO";
  }

  panel.classList.remove("oculto");
  panel.scrollIntoView({behavior:"smooth",block:"start"});
}

function cerrarFormularioClienteAdmin(){
  document.getElementById("clienteFormPanel")?.classList.add("oculto");
  document.getElementById("clienteAdminForm")?.reset();
}

function valCliente(id){
  return document.getElementById(id)?.value?.trim() || null;
}

async function guardarClienteAdmin(e){
  e.preventDefault();
  const msg=document.getElementById("clienteAdminMensaje");
  msg.textContent="";

  const id=document.getElementById("clienteAdminId").value;
  const body={
    nombre:valCliente("clienteNombre"),
    apellido:valCliente("clienteApellido"),
    documento:valCliente("clienteDocumento"),
    ciudad:valCliente("clienteCiudad"),
    telefono:valCliente("clienteTelefono"),
    correo:valCliente("clienteCorreo"),
    usuario:valCliente("clienteUsuario"),
    estado:document.getElementById("clienteEstado")?.value || "ACTIVO"
  };

  const contrasena=valCliente("clienteContrasena");
  if(contrasena) body.contrasena=contrasena;

  try{
    await api(id ? `/api/clientes/${id}` : "/api/clientes",{
      method:id ? "PUT":"POST",
      headers:{"Content-Type":"application/json"},
      body:JSON.stringify(body)
    });
    cerrarFormularioClienteAdmin();
    await cargarClientesAdmin();
    toast(id ? "Cliente actualizado correctamente." : "Cliente creado correctamente.");
  }catch(e){
    msg.textContent=e.message;
  }
}

function editarClienteAdmin(id){
  const c=clientesAdminCache.find(x=>Number(x.idCliente ?? x.id_cliente ?? x.id)===Number(id));
  if(c) abrirFormularioClienteAdmin(c);
}

async function cambiarEstadoClienteAdmin(id, estado){
  const c=clientesAdminCache.find(x=>Number(x.idCliente ?? x.id_cliente ?? x.id)===Number(id));
  const nombre=c ? `${c.nombre||""} ${c.apellido||""}`.trim() : "este cliente";
  const accion=String(estado).toUpperCase()==="ACTIVO" ? "activar" : "desactivar";
  if(!confirm(`¿Seguro que deseas ${accion} a ${nombre}?`)) return;
  try{
    await api(`/api/clientes/${id}/estado`,{
      method:"PUT",
      headers:{"Content-Type":"application/json"},
      body:JSON.stringify({estado:String(estado).toUpperCase()})
    });
    await cargarClientesAdmin();
    toast(`Cliente ${accion}do correctamente.`);
  }catch(e){ toast(e.message); }
}

function filtrarClientesAdmin(){
  const q=(document.getElementById("buscarClienteAdmin")?.value || "").toLowerCase().trim();
  if(!q){ renderClientesAdmin(clientesAdminCache); return; }
  renderClientesAdmin(clientesAdminCache.filter(c =>
    [c.nombre,c.apellido,c.documento,c.usuario,c.correo,c.ciudad,c.telefono]
      .filter(Boolean).some(v=>String(v).toLowerCase().includes(q))
  ));
}

window.cargarClientesAdmin=cargarClientesAdmin;
window.editarClienteAdmin=editarClienteAdmin;
window.cambiarEstadoClienteAdmin=cambiarEstadoClienteAdmin;
window.abrirFormularioClienteAdmin=abrirFormularioClienteAdmin;

let solicitudesAdminCache = [];

async function cargarAdminSolicitudes() {
  const s = sesion();
  if (!s || String(s.rol).toUpperCase() !== "ADMINISTRADOR") return;

  const box = document.getElementById("adminSolicitudes");
  if (!box) return;
  box.innerHTML = `<div class="panel">Cargando solicitudes...</div>`;

  try {
    const respuesta = await api("/api/solicitudes");
    solicitudesAdminCache = Array.isArray(respuesta) ? respuesta : [];
    renderAdminSolicitudes();
  } catch (e) {
    box.innerHTML = `<div class="panel error">No fue posible cargar las solicitudes: ${esc(e.message)}</div>`;
  }
}

function renderAdminSolicitudes() {
  const box = document.getElementById("adminSolicitudes");
  const resumen = document.getElementById("solicitudesResumen");
  if (!box) return;

  const pendientes = solicitudesAdminCache.filter(x => (x.estado||"PENDIENTE").toUpperCase()==="PENDIENTE").length;

  if (resumen) {
    resumen.innerHTML = `
      <div><strong>${solicitudesAdminCache.length}</strong><span>solicitudes totales</span></div>
      <div><strong>${pendientes}</strong><span>pendientes</span></div>
    `;
  }

  if (!solicitudesAdminCache.length) {
    box.innerHTML = `
      <div class="panel agenda-vacia">
        <div class="agenda-icon">📋</div>
        <h3>No hay solicitudes registradas</h3>
      </div>`;
    return;
  }

  box.innerHTML = [...solicitudesAdminCache]
    .sort((a,b)=>(b.fechaAtencion||"").localeCompare(a.fechaAtencion||""))
    .map(x => {
      const estado = (x.estado||"PENDIENTE").toUpperCase();
      const cliente = `${x.cliente?.nombre||""} ${x.cliente?.apellido||""}`.trim() || "Cliente";
      const asesor = x.asesor ? `${x.asesor.nombre||""} ${x.asesor.apellido||""}`.trim() : "Sin asesor";
      const vehiculo = `${x.vehiculo?.modelo?.marca?.nombre||""} ${x.vehiculo?.modelo?.nombre||""}`.trim() || "Vehículo";
      return `
    <article class="admin-item">
      <div class="admin-item-info">
        <p class="eyebrow">SOLICITUD #${esc(x.idSolicitud)} · <span class="estado estado-${esc(estado.toLowerCase())}">${esc(estado)}</span></p>
        <h3>${esc(cliente)}</h3>
        <p>${esc(vehiculo)} · Asesor: ${esc(asesor)}</p>
        <p>${esc(formatearFecha(x.fechaAtencion))} · ${esc(formatearHoraCorta(x.horaAtencion))}</p>
      </div>
      ${estado==="PENDIENTE" ? `
      <div class="admin-item-acciones">
        <button class="btn principal pequeño" onclick="cambiarEstadoSolicitud(${Number(x.idSolicitud)},'ATENDIENDO','admin')">Iniciar atención</button>
        <button class="btn secundario pequeño" onclick="cambiarEstadoSolicitud(${Number(x.idSolicitud)},'NO_ASISTIO','admin')">No asistió</button>
        <button class="btn peligro pequeño" onclick="cambiarEstadoSolicitud(${Number(x.idSolicitud)},'CANCELADA','admin')">Cancelar</button>
      </div>` : estado==="ATENDIENDO" ? `
      <div class="admin-item-acciones">
        <button class="btn principal pequeño" onclick="cambiarEstadoSolicitud(${Number(x.idSolicitud)},'REALIZADA','admin')">Finalizar atención</button>
      </div>` : ``}
    </article>`;
    }).join("");
}

window.cargarAdminSolicitudes = cargarAdminSolicitudes;

document.addEventListener("DOMContentLoaded",()=>{
  document.getElementById("btnNuevoCliente")?.addEventListener("click",()=>abrirFormularioClienteAdmin());
  document.getElementById("btnCancelarCliente")?.addEventListener("click",cerrarFormularioClienteAdmin);
  document.getElementById("btnCancelarCliente2")?.addEventListener("click",cerrarFormularioClienteAdmin);
  document.getElementById("clienteAdminForm")?.addEventListener("submit",guardarClienteAdmin);
  document.getElementById("buscarClienteAdmin")?.addEventListener("input",filtrarClientesAdmin);
});
