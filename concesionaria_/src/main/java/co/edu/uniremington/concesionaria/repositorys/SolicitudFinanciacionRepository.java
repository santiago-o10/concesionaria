package co.edu.uniremington.concesionaria.repositorys;

import co.edu.uniremington.concesionaria.models.SolicitudFinanciacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface SolicitudFinanciacionRepository extends JpaRepository<SolicitudFinanciacion, Long> {
    Optional<SolicitudFinanciacion> findByOportunidadIdOportunidad(Long id);
    List<SolicitudFinanciacion> findByOportunidadAsesorIdAsesor(Long id);
    List<SolicitudFinanciacion> findByOportunidadClienteIdCliente(Long id);
}
