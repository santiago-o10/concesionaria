package co.edu.uniremington.concesionaria.controllers;
import co.edu.uniremington.concesionaria.models.Seguimiento; import co.edu.uniremington.concesionaria.repositorys.OportunidadRepository; import co.edu.uniremington.concesionaria.services.SeguimientoService;
import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/seguimientos") public class SeguimientoController {
 private final SeguimientoService service; private final OportunidadRepository oportunidades;
 public SeguimientoController(SeguimientoService s,OportunidadRepository o){service=s;oportunidades=o;}
 private Long id(Authentication a){return (Long)((org.springframework.security.authentication.UsernamePasswordAuthenticationToken)a).getDetails();}
 @GetMapping @PreAuthorize("hasRole('ADMINISTRADOR')") public List<Seguimiento> all(){return service.listarTodos();}
 @GetMapping("/asesor/me") @PreAuthorize("hasRole('ASESOR')") public List<Seguimiento> me(Authentication a){return service.listarPorAsesor(id(a));}
 @GetMapping("/oportunidad/{id}") @PreAuthorize("hasAnyRole('ASESOR','ADMINISTRADOR','CLIENTE')")
 public List<Seguimiento> op(@PathVariable Long id,Authentication a){
   var o=oportunidades.findById(id).orElseThrow(()->new co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException("No existe la oportunidad."));
   Long user=id(a);
   String rol=a.getAuthorities().stream().findFirst().map(x->x.getAuthority()).orElse("");
   if("ROLE_ASESOR".equals(rol) && (o.getAsesor()==null || !user.equals(o.getAsesor().getIdAsesor()))) throw new org.springframework.security.access.AccessDeniedException("No tienes acceso a esta oportunidad.");
   if("ROLE_CLIENTE".equals(rol) && (o.getCliente()==null || !user.equals(o.getCliente().getIdCliente()))) throw new org.springframework.security.access.AccessDeniedException("No tienes acceso a esta oportunidad.");
   return service.listarPorOportunidad(id);
 }
 @PostMapping @PreAuthorize("hasRole('ASESOR')") public Seguimiento crear(@RequestBody Seguimiento s,Authentication a){
   Long user=id(a);
   if(s.getOportunidad()==null || s.getOportunidad().getIdOportunidad()==null) throw new co.edu.uniremington.concesionaria.exceptions.ReglaNegocioException("Debe indicar una oportunidad.");
   var o=oportunidades.findById(s.getOportunidad().getIdOportunidad()).orElseThrow(()->new co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException("No existe la oportunidad."));
   if(o.getAsesor()==null || !user.equals(o.getAsesor().getIdAsesor())) throw new org.springframework.security.access.AccessDeniedException("La oportunidad no pertenece al asesor autenticado.");
   return service.crear(s);
 }
 @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ASESOR','ADMINISTRADOR')") public Seguimiento actualizar(@PathVariable Long id,@RequestBody Seguimiento s){return service.actualizar(id,s);}
}