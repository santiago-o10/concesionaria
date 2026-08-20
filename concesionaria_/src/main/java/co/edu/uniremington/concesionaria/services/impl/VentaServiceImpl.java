package co.edu.uniremington.concesionaria.services.impl;
import co.edu.uniremington.concesionaria.models.*; import co.edu.uniremington.concesionaria.repositorys.*; import co.edu.uniremington.concesionaria.services.*; import co.edu.uniremington.concesionaria.exceptions.*;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.*; import java.util.*;
@Service public class VentaServiceImpl implements VentaService {
 private final VentaRepository repo; private final OportunidadRepository oportunidades; private final VehiculoRepository vehiculos; private final FinanciacionRepository financiaciones; private final SolicitudFinanciacionRepository solicitudesFinanciacion;
 public VentaServiceImpl(VentaRepository r,OportunidadRepository o,VehiculoRepository v,FinanciacionRepository f,
                         SolicitudFinanciacionRepository sf){repo=r;oportunidades=o;vehiculos=v;financiaciones=f;solicitudesFinanciacion=sf;}
 @Transactional public Venta crear(Venta v){ if(v.getOportunidad()==null||v.getOportunidad().getIdOportunidad()==null) throw new ReglaNegocioException("Debe seleccionar una oportunidad."); Oportunidad o=oportunidades.findById(v.getOportunidad().getIdOportunidad()).orElseThrow(()->new RecursoNoEncontradoException("No existe la oportunidad.")); if("VENDIDA".equalsIgnoreCase(o.getEstado())) throw new ReglaNegocioException("La oportunidad ya está vendida."); if(!"DISPONIBLE".equalsIgnoreCase(o.getVehiculo().getEstado())) throw new ReglaNegocioException("El vehículo ya no está disponible."); if(v.getPrecioFinal()==null||v.getPrecioFinal().signum()<=0) throw new ReglaNegocioException("El precio final debe ser mayor que cero."); if(v.getFormaPago()==null||v.getFormaPago().isBlank()) throw new ReglaNegocioException("Debe indicar la forma de pago."); v.setOportunidad(o); v.setCliente(o.getCliente()); v.setAsesor(o.getAsesor()); v.setVehiculo(o.getVehiculo()); v.setFechaVenta(LocalDate.now()); if(v.getFinanciacion()==null) v.setFinanciacion(o.getFinanciacion()); if("FINANCIAMIENTO".equalsIgnoreCase(v.getFormaPago())){
      SolicitudFinanciacion sf=solicitudesFinanciacion.findByOportunidadIdOportunidad(o.getIdOportunidad())
          .orElseThrow(()->new ReglaNegocioException("No existe una solicitud de financiación para esta venta."));
      if(!"APROBADA".equalsIgnoreCase(sf.getEstado()))
          throw new ReglaNegocioException("La financiación debe estar aprobada antes de registrar la venta.");
      v.setFinanciacion(sf.getFinanciacion());
   } else {
      v.setFinanciacion(null);
   } o.setEstado("VENDIDA"); oportunidades.save(o); Vehiculo car=o.getVehiculo(); car.setEstado("VENDIDO"); vehiculos.save(car); return repo.save(v);}
 public List<Venta> listarTodos(){return repo.findAll();} public List<Venta> listarPorAsesor(Long id){return repo.findByAsesorIdAsesor(id);}
}