package co.edu.uniremington.concesionaria.repositorys;

import co.edu.uniremington.concesionaria.models.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {

    List<Disponibilidad> findByAsesorIdAsesor(Long idAsesor);
    List<Disponibilidad> findByFechaAndEstadoIgnoreCase(java.time.LocalDate fecha, String estado);

}
