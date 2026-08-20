
let oportunidadesComercialCache=[];
let seguimientosComercialCache=[];
let ventasComercialCache=[];
let solicitudesFinanciacionCache=[];

function comercialEsc(v){return esc(v);}
function oportunidadNombre(o){
  return `${o.cliente?.nombre||""} ${o.cliente?.apellido||""}`.trim() || "Cliente";
}
function oportunidadVehiculo(o){
  return `${o.vehiculo?.modelo?.marca?.nombre||""} ${o.vehiculo?.modelo?.nombre||""}`.trim() || "Vehículo";
}
function fechaHoyLocal(){
  const d=new Date(); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,"0")}-${String(d.getDate()).padStart(2,"0")}`;
}

async function cargarComercialAsesor(){
  const box=document.getElementById("panelComercialAsesor");
  if(!box || sesion()?.rol!=="ASESOR") return;
  box.innerHTML='<p>Cargando gestión comercial...</p>';
  try{
    [oportunidadesComercialCache,seguimientosComercialCache,solicitudesFinanciacionCache]=await Promise.all([
      api("/api/oportunidades/asesor/me"),
      api("/api/seguimientos/asesor/me"),
      api("/api/solicitudes-financiacion/asesor/me")
    ]);
    renderComercialAsesor();
  }catch(e){box.innerHTML=`<p class="error">${comercialEsc(e.message)}</p>`;}
}

function renderComercialAsesor(){
 const box=document.getElementById("panelComercialAsesor"); if(!box)return;
 const hoy=fechaHoyLocal();
 const pendientes=seguimientosComercialCache.filter(s=>String(s.estado).toUpperCase()==="PENDIENTE").length;
 const vencidos=seguimientosComercialCache.filter(s=>String(s.estado).toUpperCase()==="PENDIENTE" && s.fechaProgramada<=hoy).length;
 box.innerHTML=`
  <div class="admin-resumen">
    <div><strong>${oportunidadesComercialCache.length}</strong><span>oportunidades</span></div>
    <div><strong>${oportunidadesComercialCache.filter(o=>["INTERESADO","SEGUIMIENTO","COMPARANDO","NEGOCIACION"].includes(String(o.estado).toUpperCase())).length}</strong><span>activas</span></div>
    <div><strong>${pendientes}</strong><span>seguimientos pendientes</span></div>
    <div><strong>${vencidos}</strong><span>para atender hoy</span></div>
  </div>
  <div class="lista">${oportunidadesComercialCache.length ? oportunidadesComercialCache.map(renderOportunidadAsesor).join("") :
    '<div class="panel agenda-vacia"><h3>Aún no tienes oportunidades</h3><p>Cuando cierres una asesoría con interés, aparecerá aquí.</p></div>'}</div>`;
}

function renderOportunidadAsesor(o){
 const estado=String(o.estado||"").toUpperCase();
 const seguimientos=seguimientosComercialCache.filter(s=>s.oportunidad?.idOportunidad===o.idOportunidad);
 const pendientes=seguimientos.filter(s=>String(s.estado).toUpperCase()==="PENDIENTE");
 return `<article class="admin-item">
  <div class="admin-item-info">
   <p class="eyebrow">OPORTUNIDAD #${o.idOportunidad} · ${comercialEsc(estado)}</p>
   <h3>${comercialEsc(oportunidadNombre(o))}</h3>
   <p>${comercialEsc(oportunidadVehiculo(o))} · ${comercialEsc(o.formaPago||"Forma de pago pendiente")}</p>
   ${o.presupuesto!=null?`<p>Presupuesto: ${new Intl.NumberFormat("es-CO",{style:"currency",currency:"COP",maximumFractionDigits:0}).format(o.presupuesto)}</p>`:""}
   ${o.observaciones?`<p>${comercialEsc(o.observaciones)}</p>`:""}
   ${solicitudesFinanciacionCache.filter(f=>f.oportunidad?.idOportunidad===o.idOportunidad).map(f=>`<p><strong>Financiación:</strong> ${comercialEsc(f.estado)} · Solicita ${comercialEsc(String(f.montoSolicitado||0))}</p>`).join("")}
   ${pendientes.length?`<p><strong>Próximo seguimiento:</strong> ${comercialEsc(pendientes.sort((a,b)=>a.fechaProgramada.localeCompare(b.fechaProgramada))[0].fechaProgramada)}</p>`:"<p>Sin seguimiento pendiente.</p>"}
  </div>
  <div class="admin-item-acciones">
   ${!["VENDIDA","PERDIDA"].includes(estado)?`<button class="btn secundario pequeño" onclick="abrirSeguimiento(${o.idOportunidad})">+ Seguimiento</button>`:""}
   ${!["VENDIDA","PERDIDA"].includes(estado)?`<button class="btn principal pequeño" onclick="abrirCierreOportunidad(${o.idOportunidad})">Cerrar oportunidad</button>`:""}
   ${pendientes.map(s=>`<button class="btn secundario pequeño" onclick="marcarSeguimientoRealizado(${s.idSeguimiento})">Completar seguimiento</button>`).join("")}
  </div>
 </article>`;
}


function abrirCierreOportunidad(id){
 const modal=document.getElementById("modalCierreOportunidad"); if(!modal)return;
 document.getElementById("cierreOportunidadId").value=id;
 document.getElementById("cierreOportunidadForm").reset();
 document.getElementById("cierreOportunidadId").value=id;
 document.getElementById("cierreFecha").min=fechaHoyLocal();
 document.getElementById("cierreFecha").value="";
 document.getElementById("cierreSeguimientoWrap").classList.add("oculto");
 document.getElementById("cierrePerdidaWrap").classList.add("oculto");
 document.getElementById("cierreMensaje").textContent="";
 modal.classList.remove("oculto");
}
window.abrirCierreOportunidad=abrirCierreOportunidad;

function actualizarCierreOportunidad(){
 const accion=document.getElementById("cierreAccion")?.value;
 const seg=document.getElementById("cierreSeguimientoWrap");
 const perdida=document.getElementById("cierrePerdidaWrap");
 if(!seg||!perdida)return;
 seg.classList.toggle("oculto", !["SEGUIMIENTO","PENSAR","COMPARAR","OTRA_ASESORIA"].includes(accion));
 perdida.classList.toggle("oculto", accion!=="NO_INTERESADO");
}
window.actualizarCierreOportunidad=actualizarCierreOportunidad;

async function guardarCierreOportunidad(e){
 e.preventDefault();
 const msg=document.getElementById("cierreMensaje"); msg.textContent="";
 const id=Number(document.getElementById("cierreOportunidadId").value);
 const accion=document.getElementById("cierreAccion").value;
 const fecha=document.getElementById("cierreFecha").value||null;
 const medio=document.getElementById("cierreMedio").value||null;
 const motivo=document.getElementById("cierreMotivoPerdida").value.trim()||null;
 const observaciones=document.getElementById("cierreObservaciones").value.trim()||null;
 if(!accion){msg.textContent="Selecciona qué pasó con el cliente.";return;}
 if(accion==="NO_INTERESADO" && !motivo){msg.textContent="Indica el motivo de pérdida.";return;}
 if(["SEGUIMIENTO","PENSAR","COMPARAR","OTRA_ASESORIA"].includes(accion) && (!fecha||!medio)){
   msg.textContent="Programa la próxima acción para que la oportunidad no quede abandonada.";return;
 }
 if(accion==="COMPRAR"){
   document.getElementById("modalCierreOportunidad").classList.add("oculto");
   abrirVenta(id);
   return;
 }
 try{
  await api(`/api/oportunidades/${id}/cierre`,{
   method:"POST",headers:{"Content-Type":"application/json"},
   body:JSON.stringify({accion,motivo,observaciones,fechaSeguimiento:fecha,medioSeguimiento:medio})
  });
  document.getElementById("modalCierreOportunidad").classList.add("oculto");
  toast(accion==="NO_INTERESADO"?"Oportunidad cerrada como perdida.":"Oportunidad actualizada y próxima acción programada.");
  await cargarComercialAsesor();
 }catch(e){msg.textContent=e.message;}
}
window.guardarCierreOportunidad=guardarCierreOportunidad;

function abrirResultadoAsesoria(id){
 const modal=document.getElementById("modalResultadoAsesoria"); if(!modal)return;
 document.getElementById("resultadoSolicitudId").value=id;
 document.getElementById("resultadoAsesoriaForm").reset();
 document.getElementById("resultadoSolicitudId").value=id;
 document.getElementById("resultadoFechaSeguimiento").min=fechaHoyLocal();
 document.getElementById("resultadoFechaSeguimiento").value="";
 document.getElementById("bloqueSeguimientoResultado").classList.add("oculto");
 document.getElementById("resultadoAsesoriaMensaje").textContent="";
 modal.classList.remove("oculto");
}
window.abrirResultadoAsesoria=abrirResultadoAsesoria;

function actualizarBloqueResultado(){
 const r=document.getElementById("resultadoComercial")?.value;
 const b=document.getElementById("bloqueSeguimientoResultado");
 if(!b)return;
 if(["INTERESADO","SEGUIMIENTO","COMPARANDO","OTRA_ASESORIA"].includes(r)) b.classList.remove("oculto");
 else b.classList.add("oculto");
}

async function guardarResultadoAsesoria(e){
 e.preventDefault();
 const msg=document.getElementById("resultadoAsesoriaMensaje"); msg.textContent="";
 const id=Number(document.getElementById("resultadoSolicitudId").value);
 const resultado=document.getElementById("resultadoComercial").value;
 const formaPago=document.getElementById("resultadoFormaPago").value||null;
 const planId=document.getElementById("resultadoFinanciacion").value?Number(document.getElementById("resultadoFinanciacion").value):null;
 const cuotaInicial=document.getElementById("resultadoCuotaInicial").value?Number(document.getElementById("resultadoCuotaInicial").value):null;
 const plazoMeses=document.getElementById("resultadoPlazoMeses").value?Number(document.getElementById("resultadoPlazoMeses").value):null;
 const fecha=document.getElementById("resultadoFechaSeguimiento").value||null;
 const medio=document.getElementById("resultadoMedioSeguimiento").value||null;
 if(!resultado){msg.textContent="Selecciona el resultado de la asesoría.";return;}
 const motivoPerdida=document.getElementById("resultadoMotivoPerdida")?.value.trim()||"";
 if(resultado==="NO_INTERESADO" && !motivoPerdida){msg.textContent="Indica por qué el cliente no está interesado.";return;}
 if(formaPago==="FINANCIAMIENTO" && (!planId || cuotaInicial===null || plazoMeses===null)){
   msg.textContent="Para financiar debes indicar plan, cuota inicial y plazo."; return;
 }
 if(["INTERESADO","SEGUIMIENTO","COMPARANDO","OTRA_ASESORIA"].includes(resultado) && (!fecha || !medio)){
   msg.textContent="Selecciona el medio del seguimiento."; return;
 }
 try{
   await api(`/api/oportunidades/asesoria/${id}/resultado`,{
    method:"POST",headers:{"Content-Type":"application/json"},
    body:JSON.stringify({
      resultado,
      observaciones:document.getElementById("resultadoObservaciones").value.trim()||null,
      motivoPerdida:document.getElementById("resultadoMotivoPerdida")?.value.trim()||null,
      presupuesto:document.getElementById("resultadoPresupuesto").value?Number(document.getElementById("resultadoPresupuesto").value):null,
      formaPago,
      financiacionId:planId,
      crearSeguimiento:Boolean(fecha && ["INTERESADO","SEGUIMIENTO","COMPARANDO","OTRA_ASESORIA"].includes(resultado)),
      fechaSeguimiento:fecha,
      medioSeguimiento:medio,
      cuotaInicial,
      plazoMeses
    })
   });
   document.getElementById("modalResultadoAsesoria").classList.add("oculto");
   toast("Asesoría cerrada y proceso comercial guardado.");
   if(typeof cargarAsesor==="function") await cargarAsesor();
   await cargarComercialAsesor();
 }catch(err){msg.textContent=err.message;}
}
async function abrirSeguimiento(id){
 const fecha=prompt("Fecha del próximo contacto (YYYY-MM-DD):",fechaHoyLocal());
 if(!fecha)return;
 const medio=prompt("Medio: LLAMADA, WHATSAPP, CORREO o PRESENCIAL","LLAMADA");
 if(!medio)return;
 const observaciones=prompt("Observación del seguimiento:","")||null;
 try{
  await api("/api/seguimientos",{method:"POST",headers:{"Content-Type":"application/json"},
   body:JSON.stringify({oportunidad:{idOportunidad:id},fechaProgramada:fecha,medio:medio.toUpperCase(),observaciones})});
  toast("Seguimiento programado.");
  await cargarComercialAsesor();
 }catch(e){toast(e.message);}
}
window.abrirSeguimiento=abrirSeguimiento;

async function marcarSeguimientoRealizado(id){
 const opciones="SIGUE_INTERESADO | QUIERE_COMPRAR | COMPARANDO | OTRA_ASESORIA | FINANCIACION | NO_RESPONDE | NO_INTERESADO | COMPRO_OTRO";
 const resultado=prompt("Resultado del contacto. Usa una opción:\n"+opciones,"SIGUE_INTERESADO");
 if(resultado===null)return;
 const normalizado=resultado.trim().toUpperCase();
 if(!opciones.split(" | ").includes(normalizado)){toast("Resultado no válido.");return;}
 let observaciones=prompt("¿Qué pasó?","Contacto realizado.")||"";
 try{
  await api(`/api/seguimientos/${id}`,{method:"PUT",headers:{"Content-Type":"application/json"},
   body:JSON.stringify({estado:"REALIZADO",resultado:normalizado,observaciones})});
  toast(normalizado==="QUIERE_COMPRAR"?"Cliente listo para negociar.":"Seguimiento completado.");
  await cargarComercialAsesor();
 }catch(e){toast(e.message);}
}
window.marcarSeguimientoRealizado=marcarSeguimientoRealizado;

function abrirVenta(id){
 const o=oportunidadesComercialCache.find(x=>x.idOportunidad===id);
 if(!o){toast("No se encontró la oportunidad.");return;}
 if(!confirm(`¿Confirmas que el cliente va a comprar ${oportunidadVehiculo(o)}?`))return;
 const precio=prompt("Precio final de venta:", o.vehiculo?.precio||"");
 if(precio===null)return;
 const precioNum=Number(precio);
 if(!Number.isFinite(precioNum)||precioNum<=0){toast("El precio final no es válido.");return;}
 const forma=confirm("¿El cliente pagará de contado?\n\nAceptar = Contado\nCancelar = Financiación") ? "CONTADO" : "FINANCIAMIENTO";
 api("/api/ventas",{method:"POST",headers:{"Content-Type":"application/json"},
  body:JSON.stringify({oportunidad:{idOportunidad:id},precioFinal:precioNum,formaPago:forma})
 }).then(()=>{toast("Venta registrada y vehículo marcado como vendido.");cargarComercialAsesor();})
 .catch(e=>toast(e.message));
}
window.abrirVenta=abrirVenta;

async function cargarComercialAdmin(){
 if(sesion()?.rol!=="ADMINISTRADOR")return;
 const box=document.getElementById("comercialAdminLista"); if(!box)return;
 try{
  [oportunidadesComercialCache,ventasComercialCache,seguimientosComercialCache,solicitudesFinanciacionCache]=await Promise.all([
    api("/api/oportunidades"),api("/api/ventas"),api("/api/seguimientos"),api("/api/solicitudes-financiacion")
  ]);
  const activas=oportunidadesComercialCache.filter(o=>!["VENDIDA","PERDIDA"].includes(String(o.estado).toUpperCase())).length;
  const ventas=ventasComercialCache.length;
  const pendientes=seguimientosComercialCache.filter(s=>String(s.estado).toUpperCase()==="PENDIENTE").length;
  const vencidos=seguimientosComercialCache.filter(s=>String(s.estado).toUpperCase()==="PENDIENTE"&&s.fechaProgramada<=fechaHoyLocal()).length;
  document.getElementById("comercialAdminResumen").innerHTML=`
   <div><strong>${oportunidadesComercialCache.length}</strong><span>oportunidades</span></div>
   <div><strong>${activas}</strong><span>activas</span></div>
   <div><strong>${pendientes}</strong><span>seguimientos pendientes</span></div>
   <div><strong>${vencidos}</strong><span>seguimientos vencidos</span></div>
   <div><strong>${ventas}</strong><span>ventas</span></div>
   <div><strong>${solicitudesFinanciacionCache.filter(f=>!["APROBADA","RECHAZADA"].includes(String(f.estado).toUpperCase())).length}</strong><span>financiaciones en trámite</span></div>`;
  box.innerHTML=oportunidadesComercialCache.length?oportunidadesComercialCache.map(o=>`
   <article class="admin-item">
    <div class="admin-item-info">
     <p class="eyebrow">OPORTUNIDAD #${o.idOportunidad} · ${comercialEsc(o.estado)}</p>
     <h3>${comercialEsc(oportunidadNombre(o))}</h3>
     <p>${comercialEsc(oportunidadVehiculo(o))} · Asesor: ${comercialEsc(o.asesor?.nombre||"")}</p>
     <p>Creada: ${comercialEsc(String(o.fechaCreacion||"").slice(0,10))} · ${o.presupuesto!=null?comercialEsc(String(o.presupuesto)):"Presupuesto no definido"}</p>
     ${o.motivoPerdida?`<p>Motivo de pérdida: ${comercialEsc(o.motivoPerdida)}</p>`:""}
     ${solicitudesFinanciacionCache.filter(f=>f.oportunidad?.idOportunidad===o.idOportunidad).map(f=>`<p><strong>Financiación:</strong> ${comercialEsc(f.estado)} · ${comercialEsc(String(f.montoSolicitado||0))}</p>`).join("")}
    </div>
   </article>`).join(""):'<div class="panel agenda-vacia"><h3>No hay oportunidades todavía</h3><p>Los resultados registrados después de las asesorías aparecerán aquí.</p></div>';
 }catch(e){box.innerHTML=`<div class="panel error">${comercialEsc(e.message)}</div>`;}
}


async function cargarComercialCliente(){
 const box=document.getElementById("panelComercialCliente"); if(!box||sesion()?.rol!=="CLIENTE")return;
 try{
  const [ops,fin]=await Promise.all([
    api("/api/oportunidades/cliente/me"),
    api("/api/solicitudes-financiacion/cliente/me")
  ]);
  if(!ops.length && !fin.length){box.innerHTML='<h3>Mi proceso comercial</h3><p>Aún no tienes una oportunidad comercial. Después de una asesoría aparecerá aquí.</p>';return;}
  box.innerHTML=`<h3>Mi proceso comercial</h3>${ops.map(o=>`
   <article class="solicitud-card">
    <div class="solicitud-top"><div><p class="eyebrow">OPORTUNIDAD #${o.idOportunidad}</p><h3>${comercialEsc(oportunidadVehiculo(o))}</h3></div><span class="estado">${comercialEsc(o.estado)}</span></div>
    <div class="solicitud-datos"><div><span>Asesor</span><strong>${comercialEsc(`${o.asesor?.nombre||""} ${o.asesor?.apellido||""}`)}</strong></div><div><span>Forma de pago</span><strong>${comercialEsc(o.formaPago||"Pendiente")}</strong></div></div>
    ${o.observaciones?`<p>${comercialEsc(o.observaciones)}</p>`:""}
    ${fin.filter(f=>f.oportunidad?.idOportunidad===o.idOportunidad).map(f=>`<p><strong>Financiación:</strong> ${comercialEsc(f.estado)} · ${comercialEsc(f.financiacion?.nombre||"Plan seleccionado")}</p>`).join("")}
   </article>`).join("")}
   ${fin.filter(f=>!ops.some(o=>o.idOportunidad===f.oportunidad?.idOportunidad)).map(f=>`<article class="solicitud-card"><div class="solicitud-top"><div><p class="eyebrow">FINANCIACIÓN</p><h3>${comercialEsc(f.financiacion?.nombre||"Solicitud")}</h3></div><span class="estado">${comercialEsc(f.estado)}</span></div></article>`).join("")}`;
 }catch(e){box.innerHTML=`<p class="error">${comercialEsc(e.message)}</p>`;}
}
window.cargarComercialCliente=cargarComercialCliente;


async function cargarFinanciacionAdmin(){
 if(sesion()?.rol!=="ADMINISTRADOR") return;
 const box=document.getElementById("financiacionAdminLista"); if(!box)return;
 try{
   const [planes,solicitudes]=await Promise.all([
     api("/api/financiaciones"), api("/api/solicitudes-financiacion")
   ]);
   const arr=Array.isArray(solicitudes)?solicitudes:[];
   const pendientes=arr.filter(x=>!["APROBADA","RECHAZADA"].includes(String(x.estado||"").toUpperCase())).length;
   const aprobadas=arr.filter(x=>String(x.estado||"").toUpperCase()==="APROBADA").length;
   document.getElementById("financiacionAdminResumen").innerHTML=`
     <div><strong>${arr.length}</strong><span>solicitudes</span></div>
     <div><strong>${pendientes}</strong><span>en trámite</span></div>
     <div><strong>${aprobadas}</strong><span>aprobadas</span></div>
     <div><strong>${Array.isArray(planes)?planes.length:0}</strong><span>planes disponibles</span></div>`;
   box.innerHTML=`
     <h3>Solicitudes</h3>
     ${arr.length?arr.map(f=>`<article class="admin-item">
       <div class="admin-item-info">
         <p class="eyebrow">SOLICITUD #${f.idSolicitudFinanciacion} · ${comercialEsc(f.estado)}</p>
         <h3>${comercialEsc(oportunidadNombre(f.oportunidad))}</h3>
         <p>${comercialEsc(oportunidadVehiculo(f.oportunidad))} · Plan: ${comercialEsc(f.financiacion?.nombre||"")}</p>
         <p>Vehículo: ${comercialEsc(String(f.montoVehiculo||0))} · Inicial: ${comercialEsc(String(f.cuotaInicial||0))} · Solicita: ${comercialEsc(String(f.montoSolicitado||0))}</p>
         <p>Plazo: ${comercialEsc(String(f.plazoMeses||""))} meses</p>
       </div>
       <div class="admin-item-acciones">
         ${["PENDIENTE","EN_ESTUDIO"].includes(String(f.estado||"").toUpperCase())?`
           <button class="btn secundario pequeño" onclick="actualizarFinanciacion(${f.idSolicitudFinanciacion},'EN_ESTUDIO')">En estudio</button>
           <button class="btn principal pequeño" onclick="actualizarFinanciacion(${f.idSolicitudFinanciacion},'APROBADA')">Aprobar</button>
           <button class="btn peligro pequeño" onclick="actualizarFinanciacion(${f.idSolicitudFinanciacion},'RECHAZADA')">Rechazar</button>`:""}
       </div>
     </article>`).join(""):'<div class="panel agenda-vacia"><h3>No hay solicitudes de financiación</h3><p>Las solicitudes aparecerán cuando un asesor registre una oportunidad con financiación.</p></div>'}
     <h3>Planes disponibles</h3>
     ${(Array.isArray(planes)?planes:[]).map(p=>`<article class="admin-item"><div class="admin-item-info"><h3>${comercialEsc(p.nombre)}</h3><p>${comercialEsc(p.descripcion||"")} · ${comercialEsc(String(p.porcentajeInicial||0))}% inicial · ${comercialEsc(String(p.plazo||0))} meses</p></div></article>`).join("")}`;
 }catch(e){box.innerHTML=`<div class="panel error">${comercialEsc(e.message)}</div>`;}
}
async function actualizarFinanciacion(id,estado){
 const observaciones=estado==="RECHAZADA" ? (prompt("Motivo de rechazo:","")||null) : null;
 try{
   await api(`/api/solicitudes-financiacion/${id}/estado`,{method:"PUT",headers:{"Content-Type":"application/json"},body:JSON.stringify({estado,observaciones})});
   toast("Estado de financiación actualizado.");
   await cargarFinanciacionAdmin();
 }catch(e){toast(e.message);}
}
window.cargarFinanciacionAdmin=cargarFinanciacionAdmin;
window.actualizarFinanciacion=actualizarFinanciacion;

document.addEventListener("DOMContentLoaded",async ()=>{
 document.getElementById("resultadoComercial")?.addEventListener("change",actualizarBloqueResultado);
 document.getElementById("resultadoFormaPago")?.addEventListener("change",async ()=>{
   const wrap=document.getElementById("resultadoFinanciacionWrap");
   if(!wrap) return;
   if(document.getElementById("resultadoFormaPago").value!=="FINANCIAMIENTO"){wrap.classList.add("oculto");return;}
   try{
     const fs=await api("/api/financiaciones");
     document.getElementById("resultadoFinanciacion").innerHTML='<option value="">Selecciona una financiación</option>'+
       (Array.isArray(fs)?fs:[]).map(x=>`<option value="${x.idFinanciacion}" data-plazo="${x.plazo||""}" data-inicial="${x.porcentajeInicial||0}">${comercialEsc(x.nombre)} · ${comercialEsc(String(x.plazo))} meses · inicial ${comercialEsc(String(x.porcentajeInicial||0))}%</option>`).join("");
     wrap.classList.remove("oculto");
   }catch(e){toast(e.message);}
 });
 document.getElementById("resultadoFinanciacion")?.addEventListener("change",()=>{
   const opt=document.getElementById("resultadoFinanciacion").selectedOptions[0];
   const plazo=opt?.dataset?.plazo;
   const porcentaje=Number(opt?.dataset?.inicial||0);
   const presupuesto=Number(document.getElementById("resultadoPresupuesto")?.value||0);
   const cuota=document.getElementById("resultadoCuotaInicial");
   const plazoInput=document.getElementById("resultadoPlazoMeses");
   if(plazoInput && plazo) plazoInput.value=plazo;
   if(cuota && presupuesto>0) cuota.value=Math.round(presupuesto*porcentaje/100);
 });
 document.getElementById("resultadoPresupuesto")?.addEventListener("input",()=>{
   const opt=document.getElementById("resultadoFinanciacion")?.selectedOptions[0];
   const cuota=document.getElementById("resultadoCuotaInicial");
   const presupuesto=Number(document.getElementById("resultadoPresupuesto").value||0);
   const porcentaje=Number(opt?.dataset?.inicial||0);
   if(cuota && porcentaje>0 && presupuesto>0) cuota.value=Math.round(presupuesto*porcentaje/100);
 });
 document.getElementById("resultadoAsesoriaForm")?.addEventListener("submit",guardarResultadoAsesoria);
 document.getElementById("cerrarResultadoAsesoria")?.addEventListener("click",()=>document.getElementById("modalResultadoAsesoria")?.classList.add("oculto"));
});
window.cargarComercialAsesor=cargarComercialAsesor;
window.cargarComercialAdmin=cargarComercialAdmin;
