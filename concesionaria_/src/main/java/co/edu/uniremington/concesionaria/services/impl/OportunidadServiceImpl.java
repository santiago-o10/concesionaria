package co.edu.uniremington.concesionaria.services.impl;
import co.edu.uniremington.concesionaria.models.*;
import co.edu.uniremington.concesionaria.dto.CierreOportunidadRequest;
import co.edu.uniremington.concesionaria.repositorys.*;
import co.edu.uniremington.concesionaria.services.*;
import co.edu.uniremington.concesionaria.exceptions.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*; import java.util.*; import java.time.LocalDate;
import java.math.BigDecimal;

@Service public class OportunidadServiceImpl implements OportunidadService {
 private final OportunidadRepository repo; private final SolicitudAtencionRepository solicitudes; private final FinanciacionRepository financiaciones;
 private final SolicitudFinanciacionRepository solicitudesFinanciacion; private final SeguimientoRepository seguimientos;
 public OportunidadServiceImpl(OportunidadRepository r,SolicitudAtencionRepository s,FinanciacionRepository f,
   SolicitudFinanciacionRepository sf,SeguimientoRepository sg){repo=r;solicitudes=s;financiaciones=f;solicitudesFinanciacion=sf;seguimientos=sg;}
 @Override @Transactional public Oportunidad crearResultado(Long solicitudId,String resultado,String observaciones,String motivoPerdida,BigDecimal presupuesto,String formaPago,Long financiacionId,
      Boolean crearSeguimiento,String fechaSeguimiento,String medioSeguimiento,BigDecimal cuotaInicial,Integer plazoMeses){
   SolicitudAtencion s=solicitudes.findById(solicitudId).orElseThrow(()->new RecursoNoEncontradoException("No existe la asesoría."));
   if(!"REALIZADA".equalsIgnoreCase(s.getEstado())) throw new ReglaNegocioException("La asesoría debe finalizarse antes de registrar su resultado comercial.");
   if(s.getFechaFinAtencion()==null) throw new ReglaNegocioException("La asesoría no tiene registrada su hora de finalización.");
   String r=resultado==null?"":resultado.toUpperCase();
   List<String> valid=List.of("INTERESADO","SEGUIMIENTO","COMPARANDO","OTRA_ASESORIA","NO_INTERESADO");
   if(!valid.contains(r)) throw new ReglaNegocioException("Resultado no válido.");
   if(repo.findBySolicitudAtencionIdSolicitud(solicitudId).isPresent()) throw new RecursoDuplicadoException("Esta asesoría ya tiene un resultado comercial.");

   String fp=formaPago==null?"":formaPago.toUpperCase();
   if(!fp.isBlank() && !List.of("CONTADO","FINANCIAMIENTO").contains(fp))
      throw new ReglaNegocioException("La forma de pago debe ser CONTADO o FINANCIAMIENTO.");
   if("FINANCIAMIENTO".equals(fp)){
      if(financiacionId==null || cuotaInicial==null || plazoMeses==null) throw new ReglaNegocioException("Para financiar debes indicar plan, cuota inicial y plazo.");
      if(cuotaInicial.signum()<0 || plazoMeses<=0) throw new ReglaNegocioException("La cuota inicial y el plazo deben ser válidos.");
   } else if(financiacionId!=null) throw new ReglaNegocioException("La financiación solo aplica cuando la forma de pago es FINANCIAMIENTO.");

   Oportunidad o=new Oportunidad(); o.setCliente(s.getCliente()); o.setAsesor(s.getAsesor()); o.setVehiculo(s.getVehiculo()); o.setSolicitudAtencion(s);
   o.setEstado(r.equals("NO_INTERESADO")?"PERDIDA":r.equals("OTRA_ASESORIA")?"INTERESADO":r);
   if("NO_INTERESADO".equals(r) && (motivoPerdida==null || motivoPerdida.isBlank()))
      throw new ReglaNegocioException("Debes indicar el motivo por el que el cliente no está interesado.");
   if(!"NO_INTERESADO".equals(r) && (fechaSeguimiento==null || fechaSeguimiento.isBlank() || medioSeguimiento==null || medioSeguimiento.isBlank()))
      throw new ReglaNegocioException("Una oportunidad activa debe tener una próxima acción programada.");
   o.setObservaciones(observaciones); o.setMotivoPerdida(motivoPerdida); o.setPresupuesto(presupuesto); o.setFormaPago(fp.isBlank()?null:fp);
   Financiacion plan=null;
   if(financiacionId!=null) {
      plan=financiaciones.findById(financiacionId).orElseThrow(()->new RecursoNoEncontradoException("No existe la financiación."));
      if(plan.getConcesionaria()==null || s.getVehiculo().getConcesionaria()==null ||
         !plan.getConcesionaria().getIdConcesionaria().equals(s.getVehiculo().getConcesionaria().getIdConcesionaria()))
         throw new ReglaNegocioException("El plan de financiación no pertenece a la concesionaria del vehículo.");
      o.setFinanciacion(plan);
   }
   Oportunidad guardada=repo.save(o);

   if("FINANCIAMIENTO".equals(fp)){
      BigDecimal valor=s.getVehiculo().getPrecio();
      BigDecimal minimo=valor.multiply(plan.getPorcentajeInicial()).divide(BigDecimal.valueOf(100));
      if(cuotaInicial.compareTo(minimo)<0) throw new ReglaNegocioException("La cuota inicial no cumple el porcentaje mínimo del plan seleccionado.");
      if(cuotaInicial.compareTo(valor)>=0) throw new ReglaNegocioException("La cuota inicial debe ser menor que el valor del vehículo.");
      if(plazoMeses>plan.getPlazo()) throw new ReglaNegocioException("El plazo supera el máximo del plan seleccionado.");
      SolicitudFinanciacion sf=new SolicitudFinanciacion();
      sf.setOportunidad(guardada); sf.setFinanciacion(plan); sf.setMontoVehiculo(valor); sf.setCuotaInicial(cuotaInicial); sf.setMontoSolicitado(valor.subtract(cuotaInicial)); sf.setPlazoMeses(plazoMeses); sf.setEstado("PENDIENTE");
      solicitudesFinanciacion.save(sf);
   }

   if(Boolean.TRUE.equals(crearSeguimiento) && fechaSeguimiento!=null && !fechaSeguimiento.isBlank() && List.of("INTERESADO","SEGUIMIENTO","COMPARANDO","OTRA_ASESORIA").contains(r)){
      LocalDate fecha;
      try { fecha=LocalDate.parse(fechaSeguimiento); }
      catch(Exception ex){ throw new ReglaNegocioException("La fecha del seguimiento no tiene un formato válido."); }
      if(fecha.isBefore(LocalDate.now())) throw new ReglaNegocioException("La fecha del seguimiento no puede estar en el pasado.");
      if(medioSeguimiento==null || medioSeguimiento.isBlank()) throw new ReglaNegocioException("Debe indicar el medio del seguimiento.");
      Seguimiento sg=new Seguimiento(); sg.setOportunidad(guardada); sg.setFechaProgramada(fecha); sg.setMedio(medioSeguimiento.toUpperCase()); sg.setEstado("PENDIENTE"); sg.setObservaciones("Seguimiento generado al cerrar la asesoría.");
      seguimientos.save(sg);
   }
   return guardada;
 } public List<Oportunidad> listarTodos(){return repo.findAll();} public List<Oportunidad> listarPorAsesor(Long id){return repo.findByAsesorIdAsesor(id);} public List<Oportunidad> listarPorCliente(Long id){return repo.findByClienteIdCliente(id);}
 public Oportunidad actualizar(Long id,Oportunidad data){ Oportunidad o=repo.findById(id).orElseThrow(()->new RecursoNoEncontradoException("No existe la oportunidad."));
   String estado=data.getEstado()==null?o.getEstado():data.getEstado().toUpperCase();
   if(!List.of("INTERESADO","SEGUIMIENTO","NEGOCIACION","PERDIDA","VENDIDA").contains(estado)) throw new ReglaNegocioException("Estado de oportunidad no válido.");
   if(data.getVehiculo()!=null && data.getVehiculo().getIdVehiculo()!=null) o.setVehiculo(data.getVehiculo());
   o.setEstado(estado); o.setPresupuesto(data.getPresupuesto()); o.setFormaPago(data.getFormaPago()); o.setObservaciones(data.getObservaciones()); o.setMotivoPerdida(data.getMotivoPerdida()); return repo.save(o);
 }

 @Override @Transactional
 public Oportunidad cerrar(Long id, CierreOportunidadRequest req){
   Oportunidad o=repo.findById(id).orElseThrow(()->new RecursoNoEncontradoException("No existe la oportunidad."));
   String accion=req==null||req.accion()==null?"":req.accion().toUpperCase();
   if("VENDIDA".equalsIgnoreCase(o.getEstado())||"PERDIDA".equalsIgnoreCase(o.getEstado()))
      throw new ReglaNegocioException("La oportunidad ya está cerrada.");
   if(!List.of("COMPRAR","SEGUIMIENTO","PENSAR","COMPARAR","OTRA_ASESORIA","NO_INTERESADO","NO_RESPONDE").contains(accion))
      throw new ReglaNegocioException("Acción de cierre no válida.");

   if("NO_INTERESADO".equals(accion)){
      if(req.motivo()==null || req.motivo().isBlank())
         throw new ReglaNegocioException("Debes indicar por qué se perdió la oportunidad.");
      o.setEstado("PERDIDA");
      o.setMotivoPerdida(req.motivo().trim());
      o.setObservaciones(req.observaciones());
      return repo.save(o);
   }

   if("COMPRAR".equals(accion)){
      o.setEstado("NEGOCIACION");
      if(req.observaciones()!=null && !req.observaciones().isBlank()) o.setObservaciones(req.observaciones());
      return repo.save(o);
   }

   o.setEstado(("COMPARAR".equals(accion)) ? "COMPARANDO" : "SEGUIMIENTO");
   if(req.observaciones()!=null && !req.observaciones().isBlank()) o.setObservaciones(req.observaciones());

   boolean necesitaSeguimiento=true;
   if(necesitaSeguimiento){
      if(req.fechaSeguimiento()==null || req.fechaSeguimiento().isBlank())
         throw new ReglaNegocioException("Debes programar la próxima acción.");
      LocalDate fecha;
      try { fecha=LocalDate.parse(req.fechaSeguimiento()); }
      catch(Exception ex){ throw new ReglaNegocioException("La fecha del seguimiento no es válida."); }
      if(fecha.isBefore(LocalDate.now())) throw new ReglaNegocioException("La fecha del seguimiento no puede estar en el pasado.");
      if(req.medioSeguimiento()==null || req.medioSeguimiento().isBlank())
         throw new ReglaNegocioException("Debes indicar el medio del seguimiento.");
      Seguimiento sg=new Seguimiento();
      sg.setOportunidad(o);
      sg.setFechaProgramada(fecha);
      sg.setMedio(req.medioSeguimiento().toUpperCase());
      sg.setEstado("PENDIENTE");
      sg.setObservaciones(req.observaciones());
      seguimientos.save(sg);
   }
   return repo.save(o);
 }

}