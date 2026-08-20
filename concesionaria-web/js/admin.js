
/**
 * Navegación exclusiva del administrador.
 * Los botones de administración viven dentro de #panelAdmin.
 */
(function(){
  function esAdministrador(){
    try{
      const s = typeof sesion === "function" ? sesion() : null;
      return !!s && String(s.rol || s.role || "").toUpperCase() === "ADMINISTRADOR";
    }catch(e){
      return false;
    }
  }

  function ocultarAdmin(){
    document.getElementById("adminNav")?.classList.add("oculto");
    document.getElementById("panelAdmin")?.classList.add("oculto");
    document.querySelectorAll("#adminNav button").forEach(b=>b.disabled=true);
  }

  function mostrarSeccionAdmin(seccion){
    if(!esAdministrador()) return;

    const vehiculos = document.getElementById("adminVistaVehiculos");
    const asesores = document.getElementById("adminVistaAsesores");
    const clientes = document.getElementById("adminVistaClientes");
    const solicitudes = document.getElementById("adminVistaSolicitudes");
    const comercial = document.getElementById("adminVistaComercial");
    const financiacion = document.getElementById("adminVistaFinanciacion");
    const formAsesor = document.getElementById("adminAsesorFormPanel");
    const formVehiculo = document.getElementById("adminFormPanel");
    const formCliente = document.getElementById("clienteFormPanel");

    [vehiculos, asesores, clientes, solicitudes, comercial, financiacion, formAsesor, formVehiculo, formCliente].forEach(el=>el?.classList.add("oculto"));

    let idBotonActivo = "adminNavVehiculos";

    if(seccion === "asesores"){
      asesores?.classList.remove("oculto");
      if(typeof cargarAdminAsesores === "function") cargarAdminAsesores();
      idBotonActivo = "adminNavAsesores";
    }else if(seccion === "clientes"){
      clientes?.classList.remove("oculto");
      if(typeof cargarClientesAdmin === "function") cargarClientesAdmin();
      idBotonActivo = "adminNavClientes";
    }else if(seccion === "solicitudes"){
      solicitudes?.classList.remove("oculto");
      if(typeof cargarAdminSolicitudes === "function") cargarAdminSolicitudes();
      idBotonActivo = "adminNavSolicitudes";
    }else if(seccion === "comercial"){
      comercial?.classList.remove("oculto");
      window.cargarComercialAdmin?.();
      idBotonActivo = "adminNavComercial";
    }else if(seccion === "financiacion"){
      financiacion?.classList.remove("oculto");
      window.cargarFinanciacionAdmin?.();
      idBotonActivo = "adminNavFinanciacion";
    }else{
      vehiculos?.classList.remove("oculto");
      if(typeof cargarAdmin === "function") cargarAdmin();
      idBotonActivo = "adminNavVehiculos";
    }

    document.querySelectorAll("#adminNav .admin-nav-item").forEach(b=>b.classList.remove("activo"));
    document.getElementById(idBotonActivo)?.classList.add("activo");
  }

  function activarAdmin(){
    if(!esAdministrador()){
      ocultarAdmin();
      return;
    }

    const nav=document.getElementById("adminNav");
    if(!nav) return;
    nav.classList.remove("oculto");
    nav.querySelectorAll("button").forEach(b=>b.disabled=false);

    const v=document.getElementById("adminNavVehiculos");
    const a=document.getElementById("adminNavAsesores");
    const c=document.getElementById("adminNavClientes");
    const so=document.getElementById("adminNavSolicitudes");
    const co=document.getElementById("adminNavComercial");
    const fi=document.getElementById("adminNavFinanciacion");

    if(v && !v.dataset.enlazado){
      v.dataset.enlazado="1";
      v.addEventListener("click",()=>{
        if(!esAdministrador()) return;
        mostrarVista("panelAdmin");
        mostrarSeccionAdmin("vehiculos");
      });
    }

    if(a && !a.dataset.enlazado){
      a.dataset.enlazado="1";
      a.addEventListener("click",()=>{
        if(!esAdministrador()) return;
        mostrarVista("panelAdmin");
        mostrarSeccionAdmin("asesores");
      });
    }

    if(c && !c.dataset.enlazado){
      c.dataset.enlazado="1";
      c.addEventListener("click",()=>{
        if(!esAdministrador()) return;
        mostrarVista("panelAdmin");
        mostrarSeccionAdmin("clientes");
      });
    }

    if(so && !so.dataset.enlazado){
      so.dataset.enlazado="1";
      so.addEventListener("click",()=>{
        if(!esAdministrador()) return;
        mostrarVista("panelAdmin");
        mostrarSeccionAdmin("solicitudes");
      });
    }

    if(co && !co.dataset.enlazado){
      co.dataset.enlazado="1";
      co.addEventListener("click",()=>{
        if(!esAdministrador()) return;
        mostrarVista("panelAdmin");
        mostrarSeccionAdmin("comercial");
      });
    }

    if(fi && !fi.dataset.enlazado){
      fi.dataset.enlazado="1";
      fi.addEventListener("click",()=>{
        if(!esAdministrador()) return;
        mostrarVista("panelAdmin");
        mostrarSeccionAdmin("financiacion");
      });
    }

    mostrarSeccionAdmin("vehiculos");
  }

  document.addEventListener("DOMContentLoaded",()=>{
    if(esAdministrador()) activarAdmin();
    else ocultarAdmin();
  });

  window.mostrarSeccionAdmin=mostrarSeccionAdmin;
  window.refrescarNavegacionAdmin=function(){
    if(esAdministrador()) activarAdmin();
    else ocultarAdmin();
  };
})();
