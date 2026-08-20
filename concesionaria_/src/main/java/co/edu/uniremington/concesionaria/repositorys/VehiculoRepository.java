package co.edu.uniremington.concesionaria.repositorys;

import co.edu.uniremington.concesionaria.models.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    List<Vehiculo> findByEstadoIgnoreCase(String estado);
    List<Vehiculo> findByModeloIdModelo(Long idModelo);

}
