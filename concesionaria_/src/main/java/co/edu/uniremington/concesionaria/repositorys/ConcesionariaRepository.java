package co.edu.uniremington.concesionaria.repositorys;

import co.edu.uniremington.concesionaria.models.Concesionaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConcesionariaRepository extends JpaRepository<Concesionaria, Long> {

    List<Concesionaria> findByNombreContainingIgnoreCase(String nombre);

}
