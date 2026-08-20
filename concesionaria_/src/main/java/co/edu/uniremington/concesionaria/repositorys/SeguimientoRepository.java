package co.edu.uniremington.concesionaria.repositorys;
import co.edu.uniremington.concesionaria.models.Seguimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;
public interface SeguimientoRepository extends JpaRepository<Seguimiento,Long>{
 List<Seguimiento> findByOportunidadIdOportunidad(Long id);
 List<Seguimiento> findByOportunidadAsesorIdAsesor(Long id);
 long countByEstadoIgnoreCaseAndFechaProgramadaLessThanEqual(String estado, LocalDate fecha);
}