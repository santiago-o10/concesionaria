package co.edu.uniremington.concesionaria.repositorys;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniremington.concesionaria.models.Modelo;

public interface ModeloRepository extends JpaRepository<Modelo, Long> {

    List<Modelo> findByMarcaIdMarca(Long idMarca);

    long countByNombreIgnoreCaseAndMarcaIdMarca(String nombre, Long idMarca);

}