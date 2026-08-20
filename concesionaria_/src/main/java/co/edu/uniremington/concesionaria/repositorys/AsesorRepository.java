package co.edu.uniremington.concesionaria.repositorys;

import co.edu.uniremington.concesionaria.models.Asesor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsesorRepository extends JpaRepository<Asesor, Long> {

    List<Asesor> findByConcesionariaIdConcesionaria(Long idConcesionaria);
    List<Asesor> findByEstadoIgnoreCase(String estado);
    List<Asesor> findAllByNombreIgnoreCaseAndApellidoIgnoreCase(String nombre, String apellido);
    List<Asesor> findAllByUsuario(String usuario);
    long countByUsuario(String usuario);

}
