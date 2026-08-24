package co.edu.uniremington.concesionaria.controllers;
import co.edu.uniremington.concesionaria.dto.ResultadoAsesoriaRequest; import co.edu.uniremington.concesionaria.dto.CierreOportunidadRequest; import co.edu.uniremington.concesionaria.repositorys.SolicitudAtencionRepository; import co.edu.uniremington.concesionaria.models.Oportunidad; import co.edu.uniremington.concesionaria.services.OportunidadService;
import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/oportunidades") public class OportunidadController {
 private final OportunidadService service; private final SolicitudAtencionRepository solicitudes;
 public OportunidadController(OportunidadService s,SolicitudAtencionRepository r){service=s;solicitudes=r;}
 private Long id(Authentication a){return (Long)((org.springframework.security.authentication.UsernamePasswordAuthenticationToken)a).getDetails();}
 @GetMapping @PreAuthorize("hasRole('ADMINISTRADOR')") public List<Oportunidad> all(){return service.listarTodos();}
 @GetMapping("/asesor/me") @PreAuthorize("hasRole('ASESOR')") public List<Oportunidad> me(Authentication a){return service.listarPorAsesor(id(a));}
 @GetMapping("/cliente/me") @PreAuthorize("hasRole('CLIENTE')") public List<Oportunidad> cliente(Authentication a){return service.listarPorCliente(id(a));}
 @PostMapping("/asesoria/{solicitudId}/resultado") @PreAuthorize("hasAnyRole('ASESOR','ADMINISTRADOR')") public ResponseEntity<Oportunidad> resultado(@PathVariable Long solicitudId,@Valid @RequestBody ResultadoAsesoriaRequest r,Authentication a){
  Long asesorId=id(a);
  var solicitud=solicitudes.findById(solicitudId).orElseThrow(()->new co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException("No existe la asesoría."));
  String rol=a.getAuthorities().stream().findFirst().map(x->x.getAuthority()).orElse("");
  if("ROLE_ASESOR".equals(rol) && (solicitud.getAsesor()==null || !asesorId.equals(solicitud.getAsesor().getIdAsesor()))) throw new org.springframework.security.access.AccessDeniedException("La asesoría no pertenece al asesor autenticado.");
  Oportunidad resultado=service.crearResultado(solicitudId,r.resultado(),r.observaciones(),r.motivoPerdida(),r.presupuesto(),r.formaPago(),r.financiacionId(),r.crearSeguimiento(),r.fechaSeguimiento(),r.medioSeguimiento(),r.cuotaInicial(),r.plazoMeses());
  return ResponseEntity.status(HttpStatus.CREATED).body(resultado);}
 @PostMapping("/{id}/cierre") @PreAuthorize("hasRole('ASESOR')") public Oportunidad cierre(@PathVariable Long id,@Valid @RequestBody CierreOportunidadRequest r,Authentication a){
  var actual=service.listarTodos().stream().filter(x->id.equals(x.getIdOportunidad())).findFirst().orElseThrow(()->new co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException("No existe la oportunidad."));
  if(actual.getAsesor()==null || !id(a).equals(actual.getAsesor().getIdAsesor())) throw new org.springframework.security.access.AccessDeniedException("La oportunidad no pertenece al asesor autenticado.");
  return service.cerrar(id,r);
 }
 @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ASESOR','ADMINISTRADOR')") public Oportunidad actualizar(@PathVariable Long id,@RequestBody Oportunidad o,Authentication a){
  var actual=service.listarTodos().stream().filter(x->id.equals(x.getIdOportunidad())).findFirst().orElseThrow(()->new co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException("No existe la oportunidad."));
  String rol=a.getAuthorities().stream().findFirst().map(x->x.getAuthority()).orElse("");
  if("ROLE_ASESOR".equals(rol) && (actual.getAsesor()==null || !id(a).equals(actual.getAsesor().getIdAsesor()))) throw new org.springframework.security.access.AccessDeniedException("No tienes acceso a esta oportunidad.");
  return service.actualizar(id,o);
}
}