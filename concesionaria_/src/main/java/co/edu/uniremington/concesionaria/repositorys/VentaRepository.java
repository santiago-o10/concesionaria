package co.edu.uniremington.concesionaria.repositorys;
import co.edu.uniremington.concesionaria.models.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface VentaRepository extends JpaRepository<Venta,Long>{
 List<Venta> findByAsesorIdAsesor(Long id);
 Optional<Venta> findByOportunidadIdOportunidad(Long id);
 long countByAsesorIdAsesor(Long id);
}