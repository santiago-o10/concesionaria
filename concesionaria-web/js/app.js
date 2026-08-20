const listaVehiculos=document.getElementById("listaVehiculos");

let adminAsesoresCache = [];
let adminConcesionariasCache = [];

async function cargarAdminAsesores() {
  const box = document.getElementById("adminAsesores");
  if (!box) return;

  try {
    const [asesores, concesionarias] = await Promise.all([
      api("/api/asesores"),
      api("/api/concesionarias")
    ]);
    adminAsesoresCache = Array.isArray(asesores) ? asesores : [];
    adminConcesionariasCache = Array.isArray(concesionarias) ? concesionarias : [];
    renderAdminAsesores();
    cargarAdminAsesorConcesionarias();
  } catch (e) {
    box.innerHTML = `<div class="panel error">${esc(e.message)}</div>`;
  }
}

function renderAdminAsesores() {
  const box = document.getElementById("adminAsesores");
  if (!box) return;

  if (!adminAsesoresCache.length) {
    box.innerHTML = `
      <div class="panel agenda-vacia">
        <div class="agenda-icon">🧑‍💼</div>
        <h3>No hay asesores registrados</h3>
        <p>Agrega el primer asesor desde el botón de administración.</p>
      </div>`;
    return;
  }

  box.innerHTML = adminAsesoresCache.map(a => {
    const nombre = `${a.nombre || ""} ${a.apellido || ""}`.trim();
    const estado = String(a.estado || "").toUpperCase();
    const concesionaria = a.concesionaria?.nombre || "Sin concesionaria";
    const horario = a.horaInicioTrabajo && a.horaFinTrabajo
      ? `${String(a.horaInicioTrabajo).slice(0,5)} - ${String(a.horaFinTrabajo).slice(0,5)}`
      : "Horario no definido";

    return `<article class="admin-item">
      <div class="admin-item-info">
        <p class="eyebrow">${esc(a.especialidad || "ASESOR")}</p>
        <h3>${esc(nombre || "Asesor")}</h3>
        <p>${esc(a.usuario || "Sin usuario")} · ${esc(a.correo || "Sin correo")}</p>
        <p>${esc(concesionaria)} · ${esc(horario)}</p>
        <span class="admin-estado ${estado === "ACTIVO" ? "activo" : "inactivo"}">${esc(estado || "SIN ESTADO")}</span>
      </div>
      <div class="admin-item-acciones">
        <button class="btn secundario pequeño" onclick="editarAsesorAdmin(${Number(a.idAsesor)})">Editar</button>
        <button class="btn ${estado === "ACTIVO" ? "peligro" : "principal"} pequeño" onclick="cambiarEstadoAsesorAdmin(${Number(a.idAsesor)}, '${estado === "ACTIVO" ? "INACTIVO" : "ACTIVO"}')">${estado === "ACTIVO" ? "Desactivar" : "Activar"}</button>
      </div>
    </article>`;
  }).join("");
}

function cargarAdminAsesorConcesionarias(selectedId = null) {
  const select = document.getElementById("adminAsesorConcesionaria");
  if (!select) return;

  select.innerHTML =
    `<option value="">Selecciona una concesionaria</option>` +
    adminConcesionariasCache.map(c =>
      `<option value="${c.idConcesionaria}">${esc(c.nombre)}</option>`
    ).join("");

  if (selectedId != null) select.value = String(selectedId);
}

function abrirFormularioAsesorAdmin(a = null) {
  const panel = document.getElementById("adminAsesorFormPanel");
  const form = document.getElementById("adminAsesorForm");
  if (!panel || !form) return;

  form.reset();
  document.getElementById("adminAsesorMensaje").textContent = "";
  document.getElementById("adminAsesorFormTitulo").textContent = a ? "Editar asesor" : "Nuevo asesor";
  document.getElementById("adminAsesorId").value = a?.idAsesor || "";

  if (a) {
    document.getElementById("adminAsesorNombre").value = a.nombre ?? "";
    document.getElementById("adminAsesorApellido").value = a.apellido ?? "";
    document.getElementById("adminAsesorTelefono").value = a.telefono ?? "";
    document.getElementById("adminAsesorCorreo").value = a.correo ?? "";
    document.getElementById("adminAsesorEspecialidad").value = a.especialidad ?? "";
    document.getElementById("adminAsesorEstado").value = a.estado || "ACTIVO";
    document.getElementById("adminAsesorHoraInicio").value = "08:00";
    document.getElementById("adminAsesorHoraFin").value = "18:00";
    document.getElementById("adminAsesorUsuario").value = a.usuario ?? "";
    document.getElementById("adminAsesorContrasena").required = false;
    document.getElementById("adminAsesorContrasena").placeholder = "Déjala vacía para conservarla";
    cargarAdminAsesorConcesionarias(a.concesionaria?.idConcesionaria);
  } else {
    document.getElementById("adminAsesorContrasena").required = true;
    document.getElementById("adminAsesorContrasena").placeholder = "";
    cargarAdminAsesorConcesionarias();
  }

  panel.classList.remove("oculto");
  panel.scrollIntoView({behavior:"smooth", block:"start"});
}

function cerrarFormularioAsesorAdmin() {
  document.getElementById("adminAsesorFormPanel")?.classList.add("oculto");
  document.getElementById("adminAsesorForm")?.reset();
  const password = document.getElementById("adminAsesorContrasena");
  if (password) {
    password.required = false;
    password.placeholder = "";
  }
  const msg = document.getElementById("adminAsesorMensaje");
  if (msg) msg.textContent = "";
}

function valorAsesorAdmin(id) {
  return document.getElementById(id)?.value?.trim() || null;
}

function construirAsesorAdmin() {
  return {
    nombre: valorAsesorAdmin("adminAsesorNombre"),
    apellido: valorAsesorAdmin("adminAsesorApellido"),
    telefono: valorAsesorAdmin("adminAsesorTelefono"),
    correo: valorAsesorAdmin("adminAsesorCorreo"),
    especialidad: valorAsesorAdmin("adminAsesorEspecialidad"),
    estado: document.getElementById("adminAsesorEstado").value,
    horaInicioTrabajo: "08:00",
    horaFinTrabajo: "18:00",
    usuario: valorAsesorAdmin("adminAsesorUsuario"),
    contrasena: valorAsesorAdmin("adminAsesorContrasena"),
    concesionaria: {
      idConcesionaria: Number(document.getElementById("adminAsesorConcesionaria").value)
    }
  };
}

async function guardarAsesorAdmin(e) {
  e.preventDefault();
  const msg = document.getElementById("adminAsesorMensaje");
  msg.textContent = "";

  const concesionariaId = Number(document.getElementById("adminAsesorConcesionaria").value);
  if (!concesionariaId) {
    msg.textContent = "Selecciona una concesionaria.";
    return;
  }

  const id = document.getElementById("adminAsesorId").value;
  if (!id && !valorAsesorAdmin("adminAsesorContrasena")) {
    msg.textContent = "La contraseña es obligatoria para un asesor nuevo.";
    return;
  }

  try {
    await api(id ? `/api/asesores/${id}` : "/api/asesores", {
      method: id ? "PUT" : "POST",
      headers: {"Content-Type":"application/json"},
      body: JSON.stringify(construirAsesorAdmin())
    });

    cerrarFormularioAsesorAdmin();
    await cargarAdminAsesores();
    toast(id ? "Asesor actualizado correctamente." : "Asesor creado correctamente.");
  } catch (e) {
    msg.textContent = e.message;
  }
}

function editarAsesorAdmin(id) {
  const asesor = adminAsesoresCache.find(a => Number(a.idAsesor) === Number(id));
  if (asesor) abrirFormularioAsesorAdmin(asesor);
}

async function cambiarEstadoAsesorAdmin(id, estado){
  const asesor = adminAsesoresCache.find(a => Number(a.idAsesor) === Number(id));
  const nombre = asesor ? `${asesor.nombre || ""} ${asesor.apellido || ""}`.trim() : "este asesor";
  const accion = String(estado).toUpperCase()==="ACTIVO" ? "activar" : "desactivar";
  if(!confirm(`¿Seguro que deseas ${accion} a ${nombre}?`)) return;
  try{
    await api(`/api/asesores/${id}/estado`,{
      method:"PUT",
      headers:{"Content-Type":"application/json"},
      body:JSON.stringify({estado:String(estado).toUpperCase()})
    });
    await cargarAdminAsesores();
    toast(`Asesor ${accion}do correctamente.`);
  }catch(e){ toast(e.message); }
}


document.addEventListener("DOMContentLoaded",()=>{
  cargarVehiculos();
  cargarBloqueSolicitud();

  // Modal de vehículo: X, botón Cerrar, clic en el fondo y tecla Escape.
  document.getElementById("cerrarVehiculo")?.addEventListener("click",(e)=>{
    e.preventDefault();
    e.stopPropagation();
    cerrarDetalleVehiculo();
  });

  document.querySelectorAll(".modal").forEach((modal)=>{
    modal.addEventListener("click",(e)=>{
      if(e.target === modal) modal.classList.add("oculto");
    });
  });

  document.addEventListener("keydown",(e)=>{
    if(e.key === "Escape") {
      cerrarDetalleVehiculo();
      document.getElementById("modalSesion")?.classList.add("oculto");
      document.getElementById("modalPerfil")?.classList.add("oculto");
      document.getElementById("modalEditarPerfil")?.classList.add("oculto");
    }
  });
});

document.querySelectorAll("[data-vista]").forEach(el=>el.addEventListener("click",e=>{
  e.preventDefault(); const v=el.dataset.vista;
  if(v==="solicitud" && !sesion()) { mostrarVista("solicitud"); cargarBloqueSolicitud(); return; }
  mostrarVista(v);
  if(v==="solicitud") cargarBloqueSolicitud();
}));

function mostrarVista(id){
 document.querySelectorAll(".vista").forEach(x=>x.classList.remove("activa"));
 document.getElementById(id)?.classList.add("activa");
 window.scrollTo({top:0,behavior:"smooth"});
}

function obtenerVehiculoPorId(id){
  return vehiculosCatalogo.find(v=>Number(v.idVehiculo)===Number(id));
}
let vehiculosCatalogo = [];

async function cargarVehiculos(){
 try{
  const vs=await api("/api/vehiculos/estado/DISPONIBLE");
  vehiculosCatalogo = Array.isArray(vs) ? vs : [];
  listaVehiculos.innerHTML=vehiculosCatalogo.length?vehiculosCatalogo.map(v=>{
   const nombre=`${v.modelo?.marca?.nombre||""} ${v.modelo?.nombre||""}`.trim() || "Vehículo";
   const precio=v.precio!=null?new Intl.NumberFormat("es-CO",{style:"currency",currency:"COP",maximumFractionDigits:0}).format(v.precio):"Consultar";
   const imagen=imagenVehiculo(v);
   return `<article class="vehiculo">
      <div class="auto-imagen">${imagen ? `<img src="${esc(imagen)}" alt="${esc(nombre)} ${esc(v.color||"")}" loading="lazy" onerror="this.parentElement.innerHTML='<div class=&quot;auto-placeholder&quot;>Imagen no disponible</div>'">` : `<div class="auto-placeholder">Imagen no disponible</div>`}</div>
      <div class="vehiculo-contenido">
        <p class="eyebrow">${esc(v.tipo||"VEHÍCULO")}</p>
        <h3>${esc(nombre)}</h3>
        <div class="vehiculo-meta">
          <span>${esc(String(v.anio||"Año no disponible"))}</span>
          <span>${esc(v.color||"Color no disponible")}</span>
          <span>${esc(v.transmision||"Transmisión no disponible")}</span>
        </div>
        <strong class="precio">${esc(precio)}</strong>
        <div class="vehiculo-acciones">
          <button class="btn secundario pequeño" onclick="verDetalleVehiculo(${Number(v.idVehiculo)})">Ver detalles</button>
          <button class="btn principal pequeño" onclick="solicitarVehiculo(${Number(v.idVehiculo)})">Solicitar asesoría</button>
        </div>
      </div>
   </article>`;
  }).join(""):`<div class="panel">No hay vehículos disponibles.</div>`;
 }catch(e){listaVehiculos.innerHTML=`<div class="panel error">${esc(e.message)}</div>`;}
}

function normalizarTexto(valor){
  return String(valor ?? "")
    .normalize("NFD")
    .replace(/[\\u0300-\\u036f]/g,"")
    .toLowerCase()
    .trim();
}

function imagenVehiculo(v){
  // La imagen debe venir exclusivamente del vehículo registrado en el backend.
  // No se crean vehículos ni imágenes de respaldo desde el frontend.
  return v?.imagenUrl || null;
}

function verDetalleVehiculo(id){
 const v=obtenerVehiculoPorId(id);
 if(!v) return;
 const nombre=`${v.modelo?.marca?.nombre||""} ${v.modelo?.nombre||""}`.trim() || "Vehículo";
 const precio=v.precio!=null?new Intl.NumberFormat("es-CO",{style:"currency",currency:"COP",maximumFractionDigits:0}).format(v.precio):"Consultar";
 const img=imagenVehiculo(v);
 const dato=(valor, unidad="")=>valor!=null && valor!=="" ? `${esc(String(valor))}${unidad}` : "No disponible";
 const lista=(texto)=>texto ? texto.split(/[.;]/).map(x=>x.trim()).filter(Boolean).map(x=>`<li>${esc(x)}</li>`).join("") : "<li>No disponible</li>";
 document.getElementById("detalleVehiculoContenido").innerHTML=`
   <div class="detalle-vehiculo-cabecera">
     <div class="detalle-auto">${img ? `<img src="${esc(img)}" alt="${esc(nombre)} ${esc(v.color||"")}" onerror="this.parentElement.innerHTML='<div class=&quot;auto-placeholder grande&quot;>Imagen no disponible</div>'">` : `<div class="auto-placeholder grande">Imagen no disponible</div>`}</div>
     <div class="detalle-info-superior">
       <p class="eyebrow">${esc(v.tipo||"VEHÍCULO")}</p>
       <h2>${esc(nombre)}</h2>
       <div class="vehiculo-meta">
         <span>${esc(String(v.anio||"Año no disponible"))}</span>
         <span>${esc(v.color||"Color no disponible")}</span>
         <span>${esc(v.transmision||"Transmisión no disponible")}</span>
       </div>
       <div class="detalle-precio">${esc(precio)}</div>
       <p><strong>Estado:</strong> ${esc(v.estado||"No disponible")}</p>
     </div>
   </div>
   <section class="ficha-seccion"><h3>Información general</h3><div class="detalle-grid">
     <div><span>Año</span><strong>${dato(v.anio)}</strong></div>
     <div><span>Tipo</span><strong>${dato(v.tipo)}</strong></div>
     <div><span>Motor</span><strong>${dato(v.motor)}</strong></div>
     <div><span>Combustible</span><strong>${dato(v.combustible)}</strong></div>
     <div><span>Transmisión</span><strong>${dato(v.transmision)}</strong></div>
     <div><span>Tracción</span><strong>${dato(v.traccion)}</strong></div>
   </div></section>
   <section class="ficha-seccion"><h3>Motor</h3><div class="detalle-grid">
     <div><span>Cilindrada</span><strong>${dato(v.cilindrada)}</strong></div>
     <div><span>Potencia</span><strong>${dato(v.potencia)}</strong></div>
     <div><span>Torque</span><strong>${dato(v.torque)}</strong></div>
   </div></section>
   <section class="ficha-seccion"><h3>Dimensiones y capacidad</h3><div class="detalle-grid">
     <div><span>Pasajeros</span><strong>${dato(v.pasajeros)}</strong></div>
     <div><span>Baúl</span><strong>${v.capacidadBaul!=null ? dato(v.capacidadBaul," L") : "No disponible"}</strong></div>
     <div><span>Largo</span><strong>${v.largo!=null ? dato(v.largo," mm") : "No disponible"}</strong></div>
     <div><span>Ancho</span><strong>${v.ancho!=null ? dato(v.ancho," mm") : "No disponible"}</strong></div>
     <div><span>Alto</span><strong>${v.alto!=null ? dato(v.alto," mm") : "No disponible"}</strong></div>
     <div><span>Peso</span><strong>${v.peso!=null ? dato(v.peso," kg") : "No disponible"}</strong></div>
   </div></section>
   <section class="ficha-seccion"><h3>Seguridad</h3><ul class="ficha-lista">${lista(v.seguridad)}</ul></section>
   <section class="ficha-seccion"><h3>Equipamiento</h3><ul class="ficha-lista">${lista(v.equipamiento)}</ul></section>
   ${v.descripcion ? `<section class="ficha-seccion"><h3>Descripción</h3><p>${esc(v.descripcion)}</p></section>` : ""}
   <div class="detalle-acciones">
     <button class="btn principal" onclick="solicitarVehiculo(${Number(v.idVehiculo)})">Solicitar asesoría</button>
     <button class="btn secundario" onclick="cerrarDetalleVehiculo()">Cerrar</button>
   </div>`;
 document.getElementById("modalVehiculo").classList.remove("oculto");
}

function cerrarDetalleVehiculo(){
 document.getElementById("modalVehiculo")?.classList.add("oculto");
}

function solicitarVehiculo(id){
 const s=sesion();
 cerrarDetalleVehiculo();
 if(!s){
   mostrarVista("solicitud");
   cargarBloqueSolicitud();
   setTimeout(()=>{ abrirLogin(); },50);
   return;
 }
 if(s.rol!=="CLIENTE"){
   mostrarVista("solicitud");
   cargarBloqueSolicitud();
   toast("La solicitud de asesoría está disponible para clientes.");
   return;
 }
 mostrarVista("solicitud");
 cargarBloqueSolicitud();
 setTimeout(()=>{
   const select=document.getElementById("vehiculo");
   if(select){
     select.value=String(id);
     select.dispatchEvent(new Event("change"));
   }
 },150);
}

function cargarBloqueSolicitud(){
 const box=document.getElementById("bloqueSolicitud"), s=sesion();
 if(!s){box.innerHTML=`<div class="panel aviso"><h3>Necesitas una cuenta de cliente</h3><p>Puedes navegar por la página sin registrarte. Para confirmar una asesoría debes iniciar sesión o crear una cuenta de cliente.</p><button class="btn principal" onclick="abrirLogin();mostrarRegistro()">Crear cuenta</button> <button class="btn secundario" onclick="abrirLogin()">Iniciar sesión</button></div>`;return;}
 if(s.rol!=="CLIENTE"){box.innerHTML=`<div class="panel aviso"><h3>Esta función es para clientes</h3><p>Has iniciado sesión como ${esc(s.rol)}. Para solicitar una asesoría necesitas una cuenta de cliente.</p></div>`;return;}
 box.innerHTML=`<form id="solicitudForm" class="formulario">
 <div class="panel aviso" id="limiteCitasAviso">Puedes tener máximo 2 citas futuras activas.</div>
 <div class="campo"><label>Vehículo de interés<select id="vehiculo" required><option>Cargando...</option></select></label></div>
 <div class="dos"><label>Fecha de atención<input id="fecha" type="date" required></label><label>Hora<select id="hora" required><option>Selecciona una fecha</option></select></label></div>
 <div class="tipo-fijo">🚗 <strong>Asesoría para compra de vehículo · 1 hora</strong></div>
 <p id="solMsg" class="error"></p><button class="btn principal">Confirmar asesoría</button></form>`;
 prepararSolicitud();
}
async function prepararSolicitud(){
 const v=document.getElementById("vehiculo"), f=document.getElementById("fecha"), h=document.getElementById("hora");
 if(!v)return;
 try{
   const misCitas=await api("/api/solicitudes/cliente/me");
   const ahora=new Date();
   const activas=misCitas.filter(x=>{
     const estado=(x.estado||"").toUpperCase();
     if(["CANCELADA","NO_ASISTIO","REALIZADA"].includes(estado)) return false;
     return new Date(`${x.fechaAtencion}T${String(x.horaAtencion||"").slice(0,8)}`) > ahora;
   }).length;
   const aviso=document.getElementById("limiteCitasAviso");
   const boton=document.querySelector("#solicitudForm button[type='submit']");
   if(activas>=2){
     if(aviso) aviso.textContent="Ya tienes 2 citas futuras activas. Debes atender, cancelar o esperar a que termine una antes de reservar otra.";
     if(boton) boton.disabled=true;
   }
 }catch(e){ console.warn("No se pudo consultar el límite de citas:",e.message); }
 try{
   const historial=await api("/api/solicitudes/cliente/me");
   const grupos={PENDIENTE:[],ATENDIENDO:[],REALIZADA:[],CANCELADA:[],NO_ASISTIO:[]};
   historial.forEach(x=>{ const k=(x.estado||"PENDIENTE").toUpperCase(); (grupos[k]||grupos.PENDIENTE).push(x); });
   const resumen=["PENDIENTE","ATENDIENDO","REALIZADA","CANCELADA","NO_ASISTIO"].map(k=>
      `<span class="estado estado-${k.toLowerCase()}">${k.replace("_"," ")}: ${grupos[k].length}</span>`).join(" ");
   const form=document.getElementById("solicitudForm");
   if(form && !document.getElementById("historialCitasCliente"))
     form.insertAdjacentHTML("afterend",`<section id="historialCitasCliente" class="panel"><h3>Mi historial de asesorías</h3><p>${resumen}</p></section>`);
 }catch(e){ console.warn("No se pudo cargar el historial:",e.message); }
 try{const vs=await api("/api/vehiculos/estado/DISPONIBLE");v.innerHTML=`<option value="">Selecciona un vehículo</option>`+vs.map(x=>`<option value="${x.idVehiculo}">${esc((x.modelo?.marca?.nombre||"")+" "+(x.modelo?.nombre||""))} · ${x.anio}</option>`).join("");}catch(e){v.innerHTML=`<option>Error</option>`;}
 const d=new Date(); f.min=d.toISOString().slice(0,10);
 f.addEventListener("change",async()=>{h.disabled=true;h.innerHTML="<option>Cargando...</option>";try{const hs=await api("/api/solicitudes/horarios-disponibles/"+f.value);h.innerHTML=`<option value="">Selecciona una hora</option>`+hs.map(x=>`<option value="${x.slice(0,5)}">${x.slice(0,5)} - ${sumar(x,1)}</option>`).join("");h.disabled=!hs.length;}catch(e){h.innerHTML="<option>Error</option>";}});
 document.getElementById("solicitudForm").addEventListener("submit",async e=>{
  e.preventDefault(); const msg=document.getElementById("solMsg");msg.textContent="";
  try{const me=await api("/api/clientes/me");const data=await api("/api/solicitudes",{method:"POST",headers:authHeaders(),body:JSON.stringify({fechaAtencion:f.value,horaAtencion:h.value,tipoAtencion:"Asesoría para compra de vehículo",cliente:{idCliente:me.idCliente},vehiculo:{idVehiculo:Number(v.value)}})});msg.className="ok";msg.textContent=`Solicitud creada. Asesor asignado: ${data.asesor?.nombre||"automático"} ${data.asesor?.apellido||""}`;e.target.reset();}catch(err){msg.textContent=err.message;}
 });
}
async function cargarCliente(){
 try{
  const c=await api("/api/clientes/me");
  clientePerfil.innerHTML=`
    <div class="cliente-resumen">
      <div>
        <p class="eyebrow">CLIENTE</p>
        <strong>${esc(c.nombre)} ${esc(c.apellido)}</strong>
        <p>${esc(c.correo || "Sin correo")} · ${esc(c.telefono || "Sin teléfono")}</p>
      </div>
      <button class="btn secundario pequeño" id="editarDesdeCliente">Editar perfil</button>
    </div>`;

  document.getElementById("editarDesdeCliente").onclick=()=>{
    abrirPerfil();
  };

  const xs=await api("/api/solicitudes/cliente/me");
  misSolicitudes.innerHTML=`
    <div class="solicitudes-cabecera">
      <div>
        <h3>Mis solicitudes</h3>
        <p>Consulta el estado y los datos de cada asesoría que has agendado.</p>
      </div>
      <span class="contador-solicitudes">${xs.length}</span>
    </div>
    ${xs.length ? xs.map(tarjetaSolicitudCliente).join("") :
      `<div class="panel agenda-vacia">
        <div class="agenda-icon">＋</div>
        <h3>Aún no tienes solicitudes</h3>
        <p>Cuando agendes una asesoría aparecerá aquí.</p>
        <button class="btn principal" onclick="mostrarVista('solicitud');cargarBloqueSolicitud()">Solicitar asesoría</button>
      </div>`}`;
 window.cargarComercialCliente?.();
 }catch(e){toast(e.message);}
}

function tarjetaSolicitudCliente(x){
 const estado=textoEstado(x.estado);
 const clase=(estado||"PENDIENTE").toLowerCase();
 const marca=x.vehiculo?.modelo?.marca?.nombre || "";
 const modelo=x.vehiculo?.modelo?.nombre || "";
 const vehiculo=`${marca} ${modelo}`.trim() || "Vehículo seleccionado";
 const asesor=x.asesor
   ? `${x.asesor.nombre||""} ${x.asesor.apellido||""}`.trim()
   : "Sin asesor asignado";
 const hora=formatearHoraCorta(x.horaAtencion);
 return `<article class="solicitud-card">
   <div class="solicitud-top">
     <div>
       <p class="eyebrow">SOLICITUD #${esc(x.idSolicitud)}</p>
       <h3>${esc(vehiculo)}</h3>
       <p>${esc(x.tipoAtencion||"Asesoría para compra de vehículo")}</p>
     </div>
     <span class="estado estado-${esc(clase)}">${esc(estado)}</span>
   </div>
   <div class="solicitud-datos">
     <div><span>Fecha</span><strong>${esc(formatearFecha(x.fechaAtencion))}</strong></div>
     <div><span>Hora</span><strong>${esc(hora)}</strong></div>
     <div><span>Asesor</span><strong>${esc(asesor)}</strong></div>
   </div>
   ${estado==="PENDIENTE" ? `<div class="detalle-acciones">
     <button class="btn peligro pequeño" onclick="cambiarEstadoSolicitud(${Number(x.idSolicitud)},'CANCELADA','cliente')">Cancelar solicitud</button>
   </div>` : ``}
 </article>`;
}

async function cambiarEstadoSolicitud(id, nuevoEstado, origen){
 const etiqueta = nuevoEstado==="CANCELADA" ? "cancelar" :
   nuevoEstado==="ATENDIENDO" ? "iniciar la atención" : "finalizar la atención";
 if(!confirm(`¿Seguro que deseas ${etiqueta} la solicitud #${id}?`)) return;
 try{
   await api(`/api/solicitudes/${id}/estado`,{
     method:"PUT",
     headers:{"Content-Type":"application/json"},
     body:JSON.stringify({estado:nuevoEstado})
   });
   if(nuevoEstado==="REALIZADA" && (origen==="asesor" || origen==="admin")){
     toast("Atención finalizada. Registra ahora el resultado comercial.");
     if(typeof abrirResultadoAsesoria==="function") abrirResultadoAsesoria(id);
   }else{
     toast("Estado actualizado correctamente.");
   }
   if(origen==="cliente") await cargarCliente();
   else if(origen==="asesor") await cargarAsesor();
   else if(origen==="admin" && typeof cargarAdminSolicitudes==="function") await cargarAdminSolicitudes();
 }catch(e){ toast(e.message); }
}

window.cambiarEstadoSolicitud=cambiarEstadoSolicitud;

function formatearFecha(fecha){
 if(!fecha) return "Sin fecha";
 const [y,m,d]=fecha.split("-");
 return `${d}/${m}/${y}`;
}
function formatearHoraCorta(hora){
 if(!hora) return "Sin hora";
 const [hh,mm]=hora.slice(0,5).split(":").map(Number);
 const periodo=hh>=12?"PM":"AM";
 const h=hh%12||12;
 return `${String(h).padStart(2,"0")}:${String(mm).padStart(2,"0")} ${periodo}`;
}

let citasAsesor = [];
let asesorActual = null;
let timerDisponibilidadAsesor = null;
let clientesPresencialCache = [];
let vehiculosPresencialCache = [];
let oportunidadesAsesorAgendaCache = [];
let seguimientosAsesorAgendaCache = [];

async function cargarAsesor(){
 try{
  const a=await api("/api/asesores/me");
  asesorActual = a;
  asesorPerfil.innerHTML=`
    <div class="perfil-asesor">
      <div>
        <p class="eyebrow">ASESOR</p>
        <strong>${esc(a.nombre)} ${esc(a.apellido)}</strong>
        <p>Horario laboral: ${esc(a.horaInicioTrabajo?.slice(0,5) || "--:--")} - ${esc(a.horaFinTrabajo?.slice(0,5) || "--:--")}</p>
      </div>
      <div id="estadoDisponibilidadAsesor" class="estado">Calculando...</div>
    </div>`;

  const fecha=document.getElementById("fechaAgenda");
  const hoy=new Date();
  fecha.value=fechaLocalHoy();
  fecha.min="";
  document.getElementById("btnHoy").onclick=()=>{
    fecha.value=new Date().toISOString().slice(0,10);
    renderAgenda();
  };
  fecha.onchange=renderAgenda;

  const [citas, oportunidades, seguimientos] = await Promise.all([
    api("/api/solicitudes/asesor/me"),
    api("/api/oportunidades/asesor/me"),
    api("/api/seguimientos/asesor/me")
  ]);
  citasAsesor=citas;
  oportunidadesAsesorAgendaCache=Array.isArray(oportunidades)?oportunidades:[];
  seguimientosAsesorAgendaCache=Array.isArray(seguimientos)?seguimientos:[];
  renderAgenda();
  renderClientesAsesor();
  actualizarEstadoDisponibilidadAsesor();
  cargarDatosPresencial();
  window.cargarComercialAsesor?.();
  if (timerDisponibilidadAsesor) clearInterval(timerDisponibilidadAsesor);
  timerDisponibilidadAsesor = setInterval(actualizarEstadoDisponibilidadAsesor, 30000);
 }catch(e){toast(e.message);}
}


function fechaLocalHoy(){
 const d=new Date();
 return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,"0")}-${String(d.getDate()).padStart(2,"0")}`;
}
function horaLocalAhora(){
 const d=new Date();
 return `${String(d.getHours()).padStart(2,"0")}:${String(d.getMinutes()).padStart(2,"0")}`;
}
function horaActualMinutos(){
 const ahora=new Date();
 return ahora.getHours()*60+ahora.getMinutes();
}
function horaAminutos(hora){
 if(!hora) return null;
 const [h,m]=String(hora).slice(0,5).split(":").map(Number);
 return h*60+m;
}
function asesorPuedeRegistrarPresencial(){
 if(!asesorActual) return {ok:false,motivo:"No se pudo cargar el asesor."};
 if(String(asesorActual.estado||"").toUpperCase()!=="ACTIVO")
   return {ok:false,motivo:"Tu cuenta de asesor está inactiva."};

 const inicio=horaAminutos(asesorActual.horaInicioTrabajo);
 const fin=horaAminutos(asesorActual.horaFinTrabajo);
 const ahora=horaActualMinutos();
 if(inicio==null || fin==null) return {ok:false,motivo:"Tu horario laboral no está configurado."};
 if(ahora<inicio || ahora>=fin) return {ok:false,motivo:"Estás fuera de tu horario laboral."};
 if(ahora+60>fin) return {ok:false,motivo:"No queda una hora completa antes de terminar tu jornada."};

 const hoy=fechaLocalHoy();
 const limite=ahora+60;
 const ocupada=citasAsesor.some(x=>{
   if(x.fechaAtencion!==hoy) return false;
   const estado=String(x.estado||"").toUpperCase();
   if(["CANCELADA","REALIZADA","NO_ASISTIO"].includes(estado)) return false;
   const ini=horaAminutos(x.horaAtencion);
   if(ini==null) return false;
   const finCita=ini+60;
   return ini<limite && ahora<finCita;
 });
 if(ocupada) return {ok:false,motivo:"Estás ocupado con una cita o atención en este momento."};

 const cruceFuturo=citasAsesor.some(x=>{
   if(x.fechaAtencion!==hoy) return false;
   const estado=String(x.estado||"").toUpperCase();
   if(["CANCELADA","REALIZADA","NO_ASISTIO"].includes(estado)) return false;
   const ini=horaAminutos(x.horaAtencion);
   return ini!=null && ini>=ahora && ini<limite;
 });
 if(cruceFuturo) return {ok:false,motivo:"Tienes una cita próxima y no hay una hora completa disponible para atender presencialmente."};

 return {ok:true,motivo:"Disponible para una atención presencial de 1 hora."};
}

function actualizarEstadoDisponibilidadAsesor(){
 const box=document.getElementById("estadoDisponibilidadAsesor");
 const boton=document.getElementById("btnAtencionPresencial");
 const estado=document.getElementById("atencionPresencialEstado");
 const resultado=asesorPuedeRegistrarPresencial();
 if(!box) return;

 let texto="🟢 DISPONIBLE";
 if(!asesorActual || String(asesorActual.estado||"").toUpperCase()!=="ACTIVO"){
   texto="⚫ INACTIVO";
 } else if(!resultado.ok && resultado.motivo.includes("fuera de tu horario")){
   texto="⚫ FUERA DE HORARIO";
 } else if(!resultado.ok && resultado.motivo.includes("ocupado")){
   texto="🔴 OCUPADO";
 } else if(!resultado.ok){
   texto="🟡 NO DISPONIBLE";
 }
 box.textContent=texto;

 if(boton){
   boton.disabled=!resultado.ok;
   boton.title=resultado.ok ? resultado.motivo : resultado.motivo;
 }
 if(estado) estado.textContent=resultado.motivo;
}

async function cargarDatosPresencial(){
 const clienteSelect=document.getElementById("presencialCliente");
 const vehiculoSelect=document.getElementById("presencialVehiculo");
 if(!clienteSelect || !vehiculoSelect) return;
 try{
   const [clientes, vehiculos]=await Promise.all([
     api("/api/clientes/para-asesor"),
     api("/api/vehiculos/estado/DISPONIBLE")
   ]);
   clientesPresencialCache=Array.isArray(clientes)?clientes:[];
   vehiculosPresencialCache=Array.isArray(vehiculos)?vehiculos:[];
   clienteSelect.innerHTML='<option value="">Selecciona un cliente</option>'+
     clientesPresencialCache.map(c=>`<option value="${c.idCliente}">${esc(`${c.nombre||""} ${c.apellido||""}`.trim())} · ${esc(c.documento||"Sin documento")}</option>`).join("");
   vehiculoSelect.innerHTML='<option value="">Selecciona un vehículo</option>'+
     vehiculosPresencialCache.map(v=>{
       const nombre=`${v.modelo?.marca?.nombre||""} ${v.modelo?.nombre||""}`.trim();
       return `<option value="${v.idVehiculo}">${esc(nombre||"Vehículo")} · ${esc(String(v.anio||""))}</option>`;
     }).join("");
 }catch(e){
   if(clienteSelect) clienteSelect.innerHTML='<option value="">No se pudieron cargar los clientes</option>';
   if(vehiculoSelect) vehiculoSelect.innerHTML='<option value="">No se pudieron cargar los vehículos</option>';
   console.error(e);
 }
}

async function abrirAtencionPresencial(){
 const panel=document.getElementById("panelAtencionPresencial");
 if(!panel) return;
 const resultado=asesorPuedeRegistrarPresencial();
 if(!resultado.ok){ toast(resultado.motivo); actualizarEstadoDisponibilidadAsesor(); return; }
 await cargarDatosPresencial();
 document.getElementById("atencionPresencialMensaje").textContent="";
 panel.classList.remove("oculto");
 actualizarEstadoDisponibilidadAsesor();
 panel.scrollIntoView({behavior:"smooth",block:"start"});
}
function cerrarAtencionPresencial(){
 document.getElementById("panelAtencionPresencial")?.classList.add("oculto");
 document.getElementById("atencionPresencialForm")?.reset();
}
async function guardarAtencionPresencial(e){
 e.preventDefault();
 const msg=document.getElementById("atencionPresencialMensaje");
 msg.textContent="";
 const resultado=asesorPuedeRegistrarPresencial();
 if(!resultado.ok){msg.textContent=resultado.motivo; return;}
 const clienteId=Number(document.getElementById("presencialCliente").value);
 const vehiculoId=Number(document.getElementById("presencialVehiculo").value);
 if(!clienteId || !vehiculoId){msg.textContent="Selecciona un cliente y un vehículo.";return;}
 const btn=document.getElementById("btnConfirmarPresencial");
 btn.disabled=true;
 try{
   await api("/api/solicitudes/presencial",{
     method:"POST",
     headers:{"Content-Type":"application/json"},
     body:JSON.stringify({
       fechaAtencion:fechaLocalHoy(),
       horaAtencion:horaLocalAhora(),
       tipoAtencion:"PRESENCIAL",
       estado:"ATENDIENDO",
       cliente:{idCliente:clienteId},
       vehiculo:{idVehiculo:vehiculoId}
     })
   });
   toast("Atención presencial iniciada correctamente.");
   cerrarAtencionPresencial();
   citasAsesor=await api("/api/solicitudes/asesor/me");
   oportunidadesAsesorAgendaCache=await api("/api/oportunidades/asesor/me");
   renderAgenda();
   renderClientesAsesor();
   actualizarEstadoDisponibilidadAsesor();
 }catch(e){
   msg.textContent=e.message;
 }finally{
   actualizarEstadoDisponibilidadAsesor();
   btn.disabled=!asesorPuedeRegistrarPresencial().ok;
 }
}

function renderClientesAsesor(){
 const contenedor=document.getElementById("misClientesAsesor");
 if(!contenedor) return;

 const clientesPorId=new Map();
 citasAsesor.forEach(x=>{
   const c=x.cliente;
   if(!c || !c.idCliente) return;
   if(!clientesPorId.has(c.idCliente)){
     clientesPorId.set(c.idCliente, {cliente:c, solicitudes:0});
   }
   clientesPorId.get(c.idCliente).solicitudes++;
 });

 const clientes=[...clientesPorId.values()]
   .sort((a,b)=>(a.cliente.nombre||"").localeCompare(b.cliente.nombre||""));

 if(!clientes.length){
   contenedor.innerHTML=`<div class="panel agenda-vacia"><div class="agenda-icon">👤</div><h3>Sin clientes todavía</h3><p>Cuando se te asigne una solicitud, el cliente aparecerá aquí.</p></div>`;
   return;
 }

 contenedor.innerHTML=clientes.map(({cliente:c, solicitudes})=>`
   <article class="cita">
     <div>
       <strong>${esc(c.nombre)} ${esc(c.apellido)}</strong>
       <p>${esc(c.telefono||"Sin teléfono")} ${c.correo?("· "+esc(c.correo)):""}</p>
     </div>
     <span class="estado estado-pendiente">${solicitudes} ${solicitudes===1?"solicitud":"solicitudes"}</span>
   </article>`).join("");
}

function renderAgenda(){
 const fecha=document.getElementById("fechaAgenda").value;
 const citas=citasAsesor
   .filter(x=>x.fechaAtencion===fecha)
   .sort((a,b)=>(a.horaAtencion||"").localeCompare(b.horaAtencion||""));

 const resumen=document.getElementById("resumenAgenda");
 resumen.innerHTML=`
   <div><strong>${citas.length}</strong><span>${citas.length===1?"cita":"citas"} programadas</span></div>
   <div><strong>08:00 - 18:00</strong><span>horario laboral · citas de 1 hora</span></div>`;

 if(!citas.length){
   misCitas.innerHTML=`<div class="panel agenda-vacia"><div class="agenda-icon">✓</div><h3>Día libre</h3><p>No tienes citas asignadas para el ${esc(fecha)}.</p></div>`;
   return;
 }
 misCitas.innerHTML=citas.map(tarjetaSolicitud).join("");
}
function textoEstado(estado){
 const e=(estado||"PENDIENTE").toUpperCase();
 if(e==="ATENDIENDO") return "ATENDIENDO";
 if(e==="REALIZADA") return "REALIZADA";
 if(e==="NO_ASISTIO") return "NO ASISTIÓ";
 if(e==="CANCELADA") return "CANCELADA";
 return "PENDIENTE";
}
function momentoCita(x){
  const fecha=String(x.fechaAtencion||"");
  const hora=String(x.horaAtencion||"").slice(0,5);
  if(!fecha||!hora) return null;
  const d=new Date(`${fecha}T${hora}:00`);
  return Number.isNaN(d.getTime())?null:d;
}
function tarjetaSolicitud(x){
 const estado=(x.estado||"PENDIENTE").toUpperCase();
 const ahora=new Date();
 const inicio=momentoCita(x);
 const fin=inicio?new Date(inicio.getTime()+60*60*1000):null;
 const tolerancia=inicio?new Date(inicio.getTime()+15*60*1000):null;
 const cliente=x.cliente||{};
 const vehiculo=x.vehiculo||{};
 const marca=vehiculo.modelo?.marca?.nombre||"";
 const modelo=vehiculo.modelo?.nombre||"";
 const nombreVehiculo=`${marca} ${modelo}`.trim() || "Vehículo seleccionado";
 const precio=vehiculo.precio!=null?new Intl.NumberFormat("es-CO",{style:"currency",currency:"COP",maximumFractionDigits:0}).format(Number(vehiculo.precio)):"Precio no disponible";
 const oportunidad=oportunidadesAsesorAgendaCache.find(o=>o.solicitudAtencion?.idSolicitud===x.idSolicitud || (o.cliente?.idCliente===cliente.idCliente && o.vehiculo?.idVehiculo===vehiculo.idVehiculo && o.idOportunidad));
 const seguimientos=seguimientosAsesorAgendaCache.filter(s=>s.oportunidad?.idOportunidad===oportunidad?.idOportunidad && String(s.estado).toUpperCase()==="PENDIENTE");
 let acciones="";
 if(estado==="PENDIENTE" && inicio && ahora>=inicio){
   acciones=`<div class="detalle-acciones">
     <button class="btn principal pequeño" onclick="cambiarEstadoSolicitud(${Number(x.idSolicitud)},'ATENDIENDO','asesor')">Iniciar atención</button>
     ${ahora>=tolerancia?`<button class="btn secundario pequeño" onclick="cambiarEstadoSolicitud(${Number(x.idSolicitud)},'NO_ASISTIO','asesor')">No asistió</button>`:""}
   </div>`;
 } else if(estado==="ATENDIENDO" && inicio && ahora>=inicio){
   const duracion=x.fechaInicioAtencion?formatearDuracion(new Date()-new Date(x.fechaInicioAtencion)):null;
   acciones=`<div class="detalle-acciones">
     ${duracion?`<span class="admin-subtexto">En curso · ${esc(duracion)}</span>`:""}
     <button class="btn principal pequeño" onclick="cambiarEstadoSolicitud(${Number(x.idSolicitud)},'REALIZADA','asesor')">Finalizar asesoría</button>
   </div>`;
 }
 return `<article class="cita cita-completa">
   <div class="cita-topline">
    <div>
      <p class="eyebrow">ASESORÍA #${Number(x.idSolicitud)}</p>
      <strong>${esc(formatearFecha(x.fechaAtencion))} · ${esc(formatearHoraCorta(x.horaAtencion))}</strong>
      <p>${esc(x.tipoAtencion||"Asesoría")} · ${esc(estado==="ATENDIENDO"?"En atención":"Atención programada")}</p>
    </div>
    <span class="estado estado-${esc(estado.toLowerCase())}">${esc(textoEstado(estado))}</span>
   </div>
   <div class="asesoria-contexto">
    <section class="asesoria-bloque">
      <p class="eyebrow">CLIENTE</p>
      <h4>${esc(`${cliente.nombre||""} ${cliente.apellido||""}`.trim()||"Cliente")}</h4>
      <p>${esc(cliente.telefono||"Sin teléfono")} · ${esc(cliente.correo||"Sin correo")}</p>
      <p>Documento: ${esc(cliente.documento||"No registrado")} · ${esc(cliente.ciudad||"Sin ciudad")}</p>
    </section>
    <section class="asesoria-bloque">
      <p class="eyebrow">VEHÍCULO DE INTERÉS</p>
      <h4>${esc(nombreVehiculo)} ${vehiculo.anio?`· ${esc(vehiculo.anio)}`:""}</h4>
      <p><strong>${esc(precio)}</strong> · ${esc(vehiculo.tipo||"Vehículo")} · ${esc(vehiculo.transmision||"Transmisión no registrada")}</p>
      <p>${esc(vehiculo.combustible||"Combustible no registrado")} · ${esc(vehiculo.pasajeros||"—")} pasajeros · ${esc(vehiculo.potencia||"—")} potencia</p>
      ${vehiculo.imagenUrl?`<img class="asesoria-vehiculo-img" src="${esc(vehiculo.imagenUrl)}" alt="${esc(nombreVehiculo)}">`:""}
    </section>
   </div>
   ${oportunidad?`<div class="asesoria-bloque asesoria-comercial">
      <p class="eyebrow">CONTEXTO COMERCIAL</p>
      <p><strong>Estado:</strong> ${esc(oportunidad.estado||"—")} ${oportunidad.formaPago?`· <strong>Pago:</strong> ${esc(oportunidad.formaPago)}`:""}</p>
      ${oportunidad.presupuesto!=null?`<p><strong>Presupuesto:</strong> ${esc(new Intl.NumberFormat("es-CO",{style:"currency",currency:"COP",maximumFractionDigits:0}).format(Number(oportunidad.presupuesto)))}</p>`:""}
      ${oportunidad.observaciones?`<p><strong>Notas:</strong> ${esc(oportunidad.observaciones)}</p>`:""}
      ${seguimientos.length?`<p><strong>Seguimiento pendiente:</strong> ${esc(seguimientos.sort((a,b)=>String(a.fechaProgramada).localeCompare(String(b.fechaProgramada)))[0].fechaProgramada)}</p>`:""}
   </div>`:""}
   <div class="cita-derecha">${acciones}</div>
 </article>`;
}
function formatearDuracion(ms){
 const total=Math.max(0,Math.floor(ms/1000)); const h=Math.floor(total/3600), m=Math.floor((total%3600)/60), s=total%60;
 return `${String(h).padStart(2,"0")}:${String(m).padStart(2,"0")}:${String(s).padStart(2,"0")}`;
}
function sumar(h,n){let [a,b]=h.slice(0,5).split(":").map(Number),m=a*60+b+n*60;return `${String(Math.floor(m/60)%24).padStart(2,"0")}:${String(m%60).padStart(2,"0")}`;}
function esc(v){return String(v??"").replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"}[c]));}
function toast(t){const x=document.getElementById("toast");x.textContent=t;x.classList.add("show");setTimeout(()=>x.classList.remove("show"),3000);}



let adminVehiculosCache = [];
let adminMarcasCache = [];
let adminConcesionariaId = null;

async function cargarAdmin() {
  const s = sesion();
  if (!s || s.rol !== "ADMINISTRADOR") return;
  try {
    const [vehiculos, marcas, concesionarias] = await Promise.all([
      api("/api/vehiculos"), api("/api/marcas"), api("/api/concesionarias")
    ]);
    adminVehiculosCache = Array.isArray(vehiculos) ? vehiculos : [];
    adminMarcasCache = Array.isArray(marcas) ? marcas : [];
    adminConcesionariaId = concesionarias?.[0]?.idConcesionaria ?? null;
    renderAdminVehiculos();
    cargarAdminMarcas();

    // Los asesores se cargan desde la BD existente; no se crean datos de prueba.
    await cargarAdminAsesores();
    window.cargarComercialAdmin?.();
  } catch(e) {
    document.getElementById("adminVehiculos").innerHTML = `<div class="panel error">${esc(e.message)}</div>`;
  }
}

function renderAdminVehiculos() {
  const box=document.getElementById("adminVehiculos"), resumen=document.getElementById("adminResumen");
  if(!box) return;
  const disponibles=adminVehiculosCache.filter(v=>String(v.estado).toUpperCase()==="DISPONIBLE").length;
  if(resumen) resumen.innerHTML=`
    <div><strong>${adminVehiculosCache.length}</strong><span>vehículos registrados</span></div>
    <div><strong>${disponibles}</strong><span>disponibles</span></div>
    <div><strong>${adminVehiculosCache.length-disponibles}</strong><span>otros estados</span></div>`;
  if(!adminVehiculosCache.length){
    box.innerHTML=`<div class="panel agenda-vacia"><div class="agenda-icon">🚗</div><h3>No hay vehículos registrados</h3><p>Agrega el primer vehículo desde el botón de administración.</p></div>`;
    return;
  }
  box.innerHTML=adminVehiculosCache.map(v=>{
    const nombre=`${v.modelo?.marca?.nombre||""} ${v.modelo?.nombre||""}`.trim()||"Vehículo";
    const precio=v.precio!=null?new Intl.NumberFormat("es-CO",{style:"currency",currency:"COP",maximumFractionDigits:0}).format(v.precio):"Sin precio";
    return `<article class="admin-item">
      <div class="admin-item-info"><p class="eyebrow">${esc(v.tipo||"VEHÍCULO")}</p><h3>${esc(nombre)}</h3>
      <p>${esc(String(v.anio||""))} · ${esc(v.color||"")} · ${esc(v.estado||"")}</p><strong>${esc(precio)}</strong></div>
      <div class="admin-item-acciones">
        <button class="btn secundario pequeño" onclick="editarVehiculoAdmin(${Number(v.idVehiculo)})">Editar</button>
        <button class="btn peligro pequeño" onclick="eliminarVehiculoAdmin(${Number(v.idVehiculo)})">Eliminar</button>
      </div></article>`;
  }).join("");
}

function cargarAdminMarcas(selectedId=null) {
  const select=document.getElementById("adminMarca"); if(!select)return;
  select.innerHTML=`<option value="">Selecciona una marca</option>`+adminMarcasCache.map(m=>`<option value="${m.idMarca}">${esc(m.nombre)}</option>`).join("");
  if(selectedId!=null){select.value=String(selectedId); cargarAdminModelos(selectedId);}
}

async function cargarAdminModelos(idMarca,selectedId=null) {
  const select=document.getElementById("adminModelo"); if(!select)return;
  if(!idMarca){select.disabled=true;select.innerHTML=`<option value="">Selecciona una marca</option>`;return;}
  select.disabled=true;select.innerHTML=`<option value="">Cargando...</option>`;
  try{
    const modelos=await api(`/api/modelos/marca/${idMarca}`);
    select.innerHTML=`<option value="">Selecciona un modelo</option>`+modelos.map(m=>`<option value="${m.idModelo}">${esc(m.nombre)}</option>`).join("");
    select.disabled=false;if(selectedId!=null)select.value=String(selectedId);
  }catch(e){select.innerHTML=`<option value="">No se pudieron cargar los modelos</option>`;}
}

function abrirFormularioVehiculoAdmin(v=null) {
  const panel=document.getElementById("adminFormPanel"),form=document.getElementById("adminVehiculoForm");
  if(!panel||!form)return;
  form.reset();document.getElementById("adminVehiculoMensaje").textContent="";
  document.getElementById("adminFormTitulo").textContent=v?"Editar vehículo":"Nuevo vehículo";
  document.getElementById("adminVehiculoId").value=v?.idVehiculo||"";
  if(v){
    const map={
      adminAnio:v.anio,adminTipo:v.tipo,adminColor:v.color,adminPrecio:v.precio,adminMotor:v.motor,
      adminTransmision:v.transmision,adminCombustible:v.combustible,adminEstado:v.estado||"DISPONIBLE",
      adminCilindrada:v.cilindrada,adminPotencia:v.potencia,adminTorque:v.torque,adminTraccion:v.traccion,
      adminPasajeros:v.pasajeros,adminBaul:v.capacidadBaul,adminLargo:v.largo,adminAncho:v.ancho,
      adminAlto:v.alto,adminPeso:v.peso,adminImagen:v.imagenUrl,adminSeguridad:v.seguridad,
      adminEquipamiento:v.equipamiento,adminDescripcion:v.descripcion
    };
    Object.entries(map).forEach(([id,val])=>{const el=document.getElementById(id);if(el)el.value=val??"";});
    cargarAdminMarcas(v.modelo?.marca?.idMarca);
    setTimeout(()=>cargarAdminModelos(v.modelo?.marca?.idMarca,v.modelo?.idModelo),100);
  }else{cargarAdminMarcas();document.getElementById("adminModelo").disabled=true;}
  panel.classList.remove("oculto");panel.scrollIntoView({behavior:"smooth",block:"start"});
}

function cerrarFormularioVehiculoAdmin(){
  document.getElementById("adminFormPanel")?.classList.add("oculto");
  document.getElementById("adminVehiculoForm")?.reset();
  const m=document.getElementById("adminVehiculoMensaje");if(m)m.textContent="";
}

function valorAdmin(id){return document.getElementById(id)?.value?.trim()||null;}

function construirVehiculoAdmin(){
  return {
    anio:Number(document.getElementById("adminAnio").value),
    precio:Number(document.getElementById("adminPrecio").value),
    tipo:valorAdmin("adminTipo"),color:valorAdmin("adminColor"),motor:valorAdmin("adminMotor"),
    transmision:valorAdmin("adminTransmision"),combustible:valorAdmin("adminCombustible"),
    descripcion:valorAdmin("adminDescripcion"),cilindrada:valorAdmin("adminCilindrada"),
    potencia:valorAdmin("adminPotencia"),torque:valorAdmin("adminTorque"),traccion:valorAdmin("adminTraccion"),
    pasajeros:valorAdmin("adminPasajeros"),capacidadBaul:valorAdmin("adminBaul"),largo:valorAdmin("adminLargo"),
    ancho:valorAdmin("adminAncho"),alto:valorAdmin("adminAlto"),peso:valorAdmin("adminPeso"),
    seguridad:valorAdmin("adminSeguridad"),equipamiento:valorAdmin("adminEquipamiento"),
    imagenUrl:valorAdmin("adminImagen"),estado:document.getElementById("adminEstado").value,
    modelo:{idModelo:Number(document.getElementById("adminModelo").value)},
    concesionaria:{idConcesionaria:adminConcesionariaId}
  };
}

async function guardarVehiculoAdmin(e){
  e.preventDefault();
  const msg=document.getElementById("adminVehiculoMensaje");msg.textContent="";
  if(!adminConcesionariaId){msg.textContent="No se encontró una concesionaria configurada.";return;}
  if(!Number(document.getElementById("adminModelo").value)){msg.textContent="Selecciona un modelo.";return;}
  try{
    const id=document.getElementById("adminVehiculoId").value;
    await api(id?`/api/vehiculos/${id}`:"/api/vehiculos",{
      method:id?"PUT":"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(construirVehiculoAdmin())
    });
    cerrarFormularioVehiculoAdmin();await cargarAdmin();await cargarVehiculos();
    toast(id?"Vehículo actualizado correctamente.":"Vehículo creado correctamente.");
  }catch(e){msg.textContent=e.message;}
}

function editarVehiculoAdmin(id){
  const v=adminVehiculosCache.find(x=>Number(x.idVehiculo)===Number(id));if(v)abrirFormularioVehiculoAdmin(v);
}
async function eliminarVehiculoAdmin(id){
  const v=adminVehiculosCache.find(x=>Number(x.idVehiculo)===Number(id));
  const nombre=v?`${v.modelo?.marca?.nombre||""} ${v.modelo?.nombre||""}`.trim():"este vehículo";
  if(!confirm(`¿Seguro que deseas eliminar ${nombre}?`))return;
  try{await api(`/api/vehiculos/${id}`,{method:"DELETE"});await cargarAdmin();await cargarVehiculos();toast("Vehículo eliminado correctamente.");}
  catch(e){toast(e.message);}
}


document.addEventListener("DOMContentLoaded",()=>{
  document.getElementById("btnNuevoVehiculo")?.addEventListener("click",()=>abrirFormularioVehiculoAdmin());
  document.getElementById("btnCancelarVehiculo")?.addEventListener("click",cerrarFormularioVehiculoAdmin);
  document.getElementById("btnCancelarVehiculo2")?.addEventListener("click",cerrarFormularioVehiculoAdmin);
  document.getElementById("adminMarca")?.addEventListener("change",e=>cargarAdminModelos(e.target.value));
  document.getElementById("adminVehiculoForm")?.addEventListener("submit",guardarVehiculoAdmin);
  document.getElementById("btnNuevoAsesor")?.addEventListener("click",async()=>{
    // Garantiza que el formulario siempre tenga las concesionarias reales de Oracle.
    if (!adminConcesionariasCache.length) {
      try {
        const concesionarias = await api("/api/concesionarias");
        adminConcesionariasCache = Array.isArray(concesionarias) ? concesionarias : [];
      } catch(e) {
        document.getElementById("adminAsesorMensaje").textContent = e.message;
      }
    }
    cargarAdminAsesorConcesionarias();
    abrirFormularioAsesorAdmin();
  });
  document.getElementById("btnCancelarAsesor")?.addEventListener("click",cerrarFormularioAsesorAdmin);
  document.getElementById("btnCancelarAsesor2")?.addEventListener("click",cerrarFormularioAsesorAdmin);
  document.getElementById("adminAsesorForm")?.addEventListener("submit",guardarAsesorAdmin);
document.getElementById("btnNuevoCliente")?.addEventListener("click",()=>{
    abrirFormularioClienteAdmin();
  });
});

document.addEventListener("DOMContentLoaded",()=>{
  document.getElementById("btnAtencionPresencial")?.addEventListener("click",abrirAtencionPresencial);
  document.getElementById("btnCerrarPresencial")?.addEventListener("click",cerrarAtencionPresencial);
  document.getElementById("atencionPresencialForm")?.addEventListener("submit",guardarAtencionPresencial);
});
