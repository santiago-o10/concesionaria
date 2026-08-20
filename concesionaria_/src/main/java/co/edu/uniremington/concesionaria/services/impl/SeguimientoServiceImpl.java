package co.edu.uniremington.concesionaria.services.impl;
import co.edu.uniremington.concesionaria.models.*; import co.edu.uniremington.concesionaria.repositorys.*; import co.edu.uniremington.concesionaria.services.*; import co.edu.uniremington.concesionaria.exceptions.*;
import org.springframework.stereotype.Service; import java.time.*; import java.util.*;
@Service public class SeguimientoServiceImpl implements SeguimientoService {
 private final SeguimientoRepository repo; private final OportunidadRepository oportunidades;
 public SeguimientoServiceImpl(SeguimientoRepository r,OportunidadRepository o){repo=r;oportunidades=o;}
 public Seguimiento crear(Seguimiento s){ if(s.getOportunidad()==null||s.getOportunidad().getIdOportunidad()==null) throw new ReglaNegocioException("Debe indicar una oportunidad."); Oportunidad o=oportunidades.findById(s.getOportunidad().getIdOportunidad()).orElseThrow(()->new RecursoNoEncontradoException("No existe la oportunidad.")); if("VENDIDA".equalsIgnoreCase(o.getEstado())||"PERDIDA".equalsIgnoreCase(o.getEstado())) throw new ReglaNegocioException("No se puede crear seguimiento para una oportunidad cerrada."); if(s.getFechaProgramada()==null||s.getFechaProgramada().isBefore(LocalDate.now())) throw new ReglaNegocioException("La fecha de seguimiento no puede estar en el pasado."); if(s.getMedio()==null||s.getMedio().isBlank()) throw new ReglaNegocioException("Debe indicar el medio."); s.setOportunidad(o); s.setEstado("PENDIENTE"); return repo.save(s);}
 public List<Seguimiento> listarTodos(){return repo.findAll();} public List<Seguimiento> listarPorAsesor(Long id){return repo.findByOportunidadAsesorIdAsesor(id);} public List<Seguimiento> listarPorOportunidad(Long id){return repo.findByOportunidadIdOportunidad(id);}
 public Seguimiento actualizar(Long id,Seguimiento s){
   Seguimiento a=repo.findById(id).orElseThrow(()->new RecursoNoEncontradoException("No existe el seguimiento."));
   String e=s.getEstado()==null?a.getEstado():s.getEstado().toUpperCase();
   if(!List.of("PENDIENTE","REALIZADO","CANCELADO").contains(e)) throw new ReglaNegocioException("Estado de seguimiento no válido.");
   if("REALIZADO".equals(e)){
      if(a.getFechaProgramada()!=null && a.getFechaProgramada().isAfter(LocalDate.now()))
         throw new ReglaNegocioException("Este seguimiento todavía no está disponible. Solo puede marcarse realizado desde el día programado.");
      if(s.getResultado()==null || s.getResultado().isBlank())
         throw new ReglaNegocioException("Debes indicar qué ocurrió en el contacto.");
      String r=s.getResultado().toUpperCase().trim();
      if(!List.of("SIGUE_INTERESADO","QUIERE_COMPRAR","COMPARANDO","OTRA_ASESORIA","FINANCIACION","NO_RESPONDE","NO_INTERESADO","COMPRO_OTRO").contains(r))
         throw new ReglaNegocioException("Resultado de seguimiento no válido.");
      Oportunidad o=a.getOportunidad();
      if("NO_INTERESADO".equals(r)||"COMPRO_OTRO".equals(r)){
         o.setEstado("PERDIDA");
         o.setMotivoPerdida(r);
      } else if("QUIERE_COMPRAR".equals(r)){
         o.setEstado("NEGOCIACION");
      } else if("COMPARANDO".equals(r)){
         o.setEstado("COMPARANDO");
      } else {
         o.setEstado("SEGUIMIENTO");
      }
      oportunidades.save(o);
      a.setFechaRealizado(LocalDateTime.now());
   }
   a.setEstado(e); a.setResultado(s.getResultado()); a.setObservaciones(s.getObservaciones());
   if("CANCELADO".equals(e) && a.getFechaRealizado()==null) a.setFechaRealizado(LocalDateTime.now());
   return repo.save(a);
 }
}