package co.edu.uniremington.concesionaria.repositorys;
import co.edu.uniremington.concesionaria.models.Oportunidad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface OportunidadRepository extends JpaRepository<Oportunidad,Long>{
 List<Oportunidad> findByAsesorIdAsesor(Long id);
 List<Oportunidad> findByClienteIdCliente(Long id);
 Optional<Oportunidad> findBySolicitudAtencionIdSolicitud(Long id);
 long countByEstadoIgnoreCase(String estado);
}