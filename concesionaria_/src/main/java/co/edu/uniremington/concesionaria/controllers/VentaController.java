package co.edu.uniremington.concesionaria.controllers;
import co.edu.uniremington.concesionaria.models.Venta; import co.edu.uniremington.concesionaria.repositorys.OportunidadRepository; import co.edu.uniremington.concesionaria.services.VentaService;
import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/ventas") public class VentaController {
 private final VentaService service; private final OportunidadRepository oportunidades;
 public VentaController(VentaService s,OportunidadRepository o){service=s;oportunidades=o;}
 private Long id(Authentication a){return (Long)((org.springframework.security.authentication.UsernamePasswordAuthenticationToken)a).getDetails();}
 @GetMapping @PreAuthorize("hasRole('ADMINISTRADOR')") public List<Venta> all(){return service.listarTodos();}
 @GetMapping("/asesor/me") @PreAuthorize("hasRole('ASESOR')") public List<Venta> me(Authentication a){return service.listarPorAsesor(id(a));}
 @PostMapping @PreAuthorize("hasRole('ASESOR')") public ResponseEntity<Venta> crear(@RequestBody Venta v,Authentication a){
  Long user=id(a);
  if(v.getOportunidad()==null || v.getOportunidad().getIdOportunidad()==null) throw new co.edu.uniremington.concesionaria.exceptions.ReglaNegocioException("Debe seleccionar una oportunidad.");
  var o=oportunidades.findById(v.getOportunidad().getIdOportunidad()).orElseThrow(()->new co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException("No existe la oportunidad."));
  if(o.getAsesor()==null || !user.equals(o.getAsesor().getIdAsesor())) throw new org.springframework.security.access.AccessDeniedException("La oportunidad no pertenece al asesor autenticado.");
  return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(v));
}
}