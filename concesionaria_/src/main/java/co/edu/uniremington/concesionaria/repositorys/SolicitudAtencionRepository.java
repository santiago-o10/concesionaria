package co.edu.uniremington.concesionaria.repositorys;

import co.edu.uniremington.concesionaria.models.SolicitudAtencion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SolicitudAtencionRepository extends JpaRepository<SolicitudAtencion, Long> {

    List<SolicitudAtencion> findByClienteIdCliente(Long idCliente);

    List<SolicitudAtencion> findByAsesorIdAsesor(Long idAsesor);

    List<SolicitudAtencion> findByAsesorIdAsesorAndFechaAtencion(Long idAsesor, LocalDate fechaAtencion);

    List<SolicitudAtencion> findByVehiculoIdVehiculo(Long idVehiculo);
}
