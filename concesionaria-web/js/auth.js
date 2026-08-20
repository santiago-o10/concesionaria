const API = "http://localhost:8081";
const SESION_KEY = "concesionaria_sesion";

function sesion() { return JSON.parse(sessionStorage.getItem(SESION_KEY) || "null"); }
function token() { return sesion()?.token || null; }
function cerrarSesion() {
  sessionStorage.removeItem(SESION_KEY);
  limpiarCamposSesion();
  actualizarZonaSesion();
  window.refrescarNavegacionAdmin?.();
  mostrarVista("inicio");
}
function limpiarCamposSesion() {
  const login = document.getElementById("loginForm");
  const registro = document.getElementById("registroForm");
  login?.reset();
  registro?.reset();
  document.getElementById("loginMensaje")?.replaceChildren();
  document.getElementById("registroMensaje")?.replaceChildren();
}
function authHeaders() {
  const t = token();
  return t ? { "Authorization": `Bearer ${t}`, "Content-Type": "application/json" } : { "Content-Type": "application/json" };
}
async function api(url, options={}) {
  options.headers = { ...(options.headers || {}), ...(token() ? {Authorization:`Bearer ${token()}`} : {}) };
  const r = await fetch(`${API}${url}`, options);
  const data = await r.json().catch(()=>null);
  if (!r.ok) {
    const msg = data?.message || data?.mensaje || data?.error || (r.status === 409
      ? "No se pudo completar la operación porque el dato ya está registrado."
      : `Error HTTP ${r.status}`);
    throw new Error(msg);
  }
  return data;
}
function abrirLogin() { document.getElementById("modalSesion").classList.remove("oculto"); mostrarLogin(); }
function cerrarLogin() { document.getElementById("modalSesion").classList.add("oculto"); }
function mostrarLogin() { document.getElementById("loginBox").classList.remove("oculto"); document.getElementById("registroBox").classList.add("oculto"); }
function mostrarRegistro() { document.getElementById("loginBox").classList.add("oculto"); document.getElementById("registroBox").classList.remove("oculto"); }

async function iniciarSesion(e) {
  e.preventDefault();
  const mensaje=document.getElementById("loginMensaje"); mensaje.textContent="";
  try {
    const data=await api("/api/auth/login",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({
      usuario:document.getElementById("loginUsuario").value.trim(),
      contrasena:document.getElementById("loginContrasena").value
    })});
    sessionStorage.setItem(SESION_KEY,JSON.stringify(data)); cerrarLogin(); actualizarZonaSesion(); abrirPanelRol(); toast("Sesión iniciada correctamente.");
  } catch(err) { mensaje.textContent=err.message; }
}
async function registrar(e) {
  e.preventDefault();
  const m=document.getElementById("registroMensaje"); m.textContent="";
  try {
    const data=await api("/api/auth/registro",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({
      nombre:regNombre.value.trim(),apellido:regApellido.value.trim(),documento:regDocumento.value.trim(),
      telefono:regTelefono.value.trim(),correo:regCorreo.value.trim()||null,ciudad:regCiudad.value.trim()||null,
      usuario:regUsuario.value.trim(),contrasena:regContrasena.value
    })});
    sessionStorage.setItem(SESION_KEY,JSON.stringify(data)); cerrarLogin(); actualizarZonaSesion(); abrirPanelRol(); toast("Cuenta creada. Bienvenido.");
  } catch(err) {
    m.textContent = mensajeAmigableAuth(err);
  }
}

function mensajeAmigableAuth(err) {
  const t = String(err?.message || "").toLowerCase();

  if (t.includes("ya existe un cliente con ese documento") || t.includes("documento")) {
    return "Este documento ya está registrado. Si ya tienes una cuenta, inicia sesión.";
  }

  if (t.includes("ese usuario ya está registrado") || t.includes("usuario ya está registrado")) {
    return "Ese usuario ya está registrado. Prueba con otro usuario o inicia sesión.";
  }

  if (t.includes("409") || t.includes("conflict")) {
    return "No se pudo crear la cuenta porque alguno de los datos ya está registrado.";
  }

  return err?.message || "No se pudo completar la operación.";
}

function actualizarZonaSesion() {
  const z=document.getElementById("zonaSesion"), s=sesion();
  if(!s) {
    z.innerHTML=`<button class="btn pequeño" id="btnLogin">Iniciar sesión</button>`;
  } else {
    z.innerHTML=`
      <div class="usuario">
        <button class="usuario-boton" id="btnPerfil" title="Abrir mi perfil">
          <span class="avatar">${esc((s.nombre||"U").charAt(0).toUpperCase())}</span>
          <span>${esc(s.nombre || s.usuario || "Usuario")}</span>
          <span class="chevron">⌄</span>
        </button>
        <div id="menuUsuario" class="menu-usuario oculto">
          <button id="menuPerfil">Mi perfil</button>
          ${s.rol==="CLIENTE" ? '<button id="menuSolicitudes">Mis solicitudes</button>' : ''}
          ${s.rol==="ASESOR" ? '<button id="menuCitas">Mis citas</button>' : ''}
          ${s.rol==="ADMINISTRADOR" ? '<button id="menuAdmin">Administración</button>' : ''}
          <button id="btnSalir" class="salir-menu">Cerrar sesión</button>
        </div>
      </div>`;
  }

  document.getElementById("btnLogin")?.addEventListener("click",abrirLogin);
  document.getElementById("btnPerfil")?.addEventListener("click",(e)=>{
    e.stopPropagation();
    document.getElementById("menuUsuario")?.classList.toggle("oculto");
  });
  document.getElementById("menuPerfil")?.addEventListener("click",()=>{
    document.getElementById("menuUsuario")?.classList.add("oculto");
    abrirPerfil();
  });
  document.getElementById("menuSolicitudes")?.addEventListener("click",()=>{
    document.getElementById("menuUsuario")?.classList.add("oculto");
    mostrarVista("panelCliente"); cargarCliente();
  });
  document.getElementById("menuCitas")?.addEventListener("click",()=>{
    document.getElementById("menuUsuario")?.classList.add("oculto");
    mostrarVista("panelAsesor"); cargarAsesor();
  });
  document.getElementById("menuAdmin")?.addEventListener("click",()=>{
    document.getElementById("menuUsuario")?.classList.add("oculto");
    mostrarVista("panelAdmin");
    window.refrescarNavegacionAdmin?.();
    cargarAdmin?.();
  });
  document.getElementById("btnSalir")?.addEventListener("click",cerrarSesion);
}

async function abrirPerfil() {
  const s=sesion();
  if(!s) return abrirLogin();

  const modal=document.getElementById("modalPerfil");
  const datos=document.getElementById("perfilDatos");
  const acciones=document.getElementById("perfilAcciones");
  document.getElementById("perfilRol").textContent=`PERFIL · ${s.rol || "USUARIO"}`;
  document.getElementById("perfilNombre").textContent=`${s.nombre || ""} ${s.apellido || ""}`.trim() || s.usuario || "Mi perfil";
  datos.innerHTML=`<div class="perfil-cargando">Cargando información...</div>`;
  acciones.innerHTML="";
  modal.classList.remove("oculto");

  try {
    let p = s;
    if(s.rol==="CLIENTE") p = await api("/api/clientes/me");
    else if(s.rol==="ASESOR") p = await api("/api/asesores/me");
    // El administrador puede mostrar los datos entregados por el login sin
    // depender de un endpoint adicional de perfil.
    const campos = [];
    if(p.usuario) campos.push(["Usuario", p.usuario]);
    if(p.nombre || p.apellido) campos.push(["Nombre completo", `${p.nombre||""} ${p.apellido||""}`.trim()]);
    if(p.documento) campos.push(["Documento", p.documento]);
    if(p.telefono) campos.push(["Teléfono", p.telefono]);
    if(p.correo) campos.push(["Correo", p.correo]);
    if(p.ciudad) campos.push(["Ciudad", p.ciudad]);
    if(p.especialidad) campos.push(["Especialidad", p.especialidad]);
    if(p.horaInicioTrabajo || p.horaFinTrabajo) campos.push(["Horario", `${p.horaInicioTrabajo?.slice(0,5)||"--:--"} - ${p.horaFinTrabajo?.slice(0,5)||"--:--"}`]);
    if(p.estado) campos.push(["Estado", p.estado]);

    datos.innerHTML=campos.length
      ? campos.map(([k,v])=>`<div class="dato-perfil"><span>${esc(k)}</span><strong>${esc(v)}</strong></div>`).join("")
      : `<div class="perfil-cargando">No hay más información disponible.</div>`;

    if(s.rol==="CLIENTE") {
      acciones.innerHTML=`<button class="btn secundario" id="editarPerfilBtn">Editar perfil</button>
        <button class="btn secundario" id="irSolicitudesPerfil">Ver mis solicitudes</button>`;
      document.getElementById("editarPerfilBtn").onclick=()=>{
        cerrarPerfil();
        abrirEditarPerfil(p);
      };
      document.getElementById("irSolicitudesPerfil").onclick=()=>{
        cerrarPerfil(); mostrarVista("panelCliente"); cargarCliente();
      };
    } else if(s.rol==="ASESOR") {
      acciones.innerHTML=`<button class="btn secundario" id="irCitasPerfil">Ver mis citas</button>`;
      document.getElementById("irCitasPerfil").onclick=()=>{
        cerrarPerfil(); mostrarVista("panelAsesor"); cargarAsesor();
      };
    } else if(s.rol==="ADMINISTRADOR") {
      acciones.innerHTML=`<button class="btn secundario" id="irAdminPerfil">Ir a administración</button>`;
      document.getElementById("irAdminPerfil").onclick=()=>{
        cerrarPerfil(); mostrarVista("panelAdmin");
      };
    }
  } catch(e) {
    datos.innerHTML=`<div class="error">${esc(e.message)}</div>`;
  }
}


function abrirEditarPerfil(p) {
  const modal = document.getElementById("modalEditarPerfil");
  if (!modal) return;

  document.getElementById("editNombre").value = p.nombre || "";
  document.getElementById("editApellido").value = p.apellido || "";
  document.getElementById("editTelefono").value = p.telefono || "";
  document.getElementById("editCorreo").value = p.correo || "";
  document.getElementById("editCiudad").value = p.ciudad || "";
  document.getElementById("editContrasena").value = "";
  document.getElementById("editMensaje").textContent = "";
  modal.classList.remove("oculto");
}

function cerrarEditarPerfil() {
  document.getElementById("modalEditarPerfil")?.classList.add("oculto");
}

async function guardarPerfil(e) {
  e.preventDefault();

  const mensaje = document.getElementById("editMensaje");
  mensaje.textContent = "";

  const body = {
    nombre: document.getElementById("editNombre").value.trim(),
    apellido: document.getElementById("editApellido").value.trim(),
    telefono: document.getElementById("editTelefono").value.trim(),
    correo: document.getElementById("editCorreo").value.trim() || null,
    ciudad: document.getElementById("editCiudad").value.trim() || null,
    contrasena: document.getElementById("editContrasena").value || null
  };

  try {
    const actualizado = await api("/api/clientes/me", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body)
    });

    const actual = sesion();
    const nuevaSesion = {
      ...actual,
      nombre: `${actualizado.nombre} ${actualizado.apellido}`.trim()
    };
    sessionStorage.setItem(SESION_KEY, JSON.stringify(nuevaSesion));

    cerrarEditarPerfil();
    actualizarZonaSesion();
    await abrirPerfil();
    toast("Perfil actualizado correctamente.");
  } catch (err) {
    mensaje.textContent = err.message || "No se pudo actualizar el perfil.";
  }
}

function cerrarPerfil() {
  document.getElementById("modalPerfil")?.classList.add("oculto");
}

function abrirPanelRol() {
  const s=sesion(); if(!s) return;
  if(s.rol==="CLIENTE"){ mostrarVista("panelCliente"); cargarCliente(); }
  if(s.rol==="ASESOR"){ mostrarVista("panelAsesor"); cargarAsesor(); }
  if(s.rol==="ADMINISTRADOR"){ mostrarVista("panelAdmin"); window.refrescarNavegacionAdmin?.(); cargarAdmin?.(); }
}
document.addEventListener("DOMContentLoaded",()=>{
  actualizarZonaSesion();
  document.getElementById("loginForm").addEventListener("submit",iniciarSesion);
  document.getElementById("registroForm").addEventListener("submit",registrar);
  document.getElementById("mostrarRegistro").addEventListener("click",mostrarRegistro);
  document.getElementById("mostrarLogin").addEventListener("click",mostrarLogin);
  document.getElementById("cerrarModal").addEventListener("click",cerrarLogin);
  document.getElementById("cerrarPerfil").addEventListener("click",cerrarPerfil);
  document.getElementById("cerrarEditarPerfil")?.addEventListener("click",cerrarEditarPerfil);
  document.getElementById("editarPerfilForm")?.addEventListener("submit",guardarPerfil);
  document.addEventListener("click",(e)=>{
    if(!e.target.closest(".usuario")) document.getElementById("menuUsuario")?.classList.add("oculto");
  });
});
