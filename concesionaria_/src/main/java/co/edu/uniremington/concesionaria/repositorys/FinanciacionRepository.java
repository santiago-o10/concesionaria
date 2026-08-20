package co.edu.uniremington.concesionaria.repositorys;

import co.edu.uniremington.concesionaria.models.Financiacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinanciacionRepository extends JpaRepository<Financiacion, Long> {

    List<Financiacion> findByConcesionariaIdConcesionaria(Long idConcesionaria);

}
