package co.edu.uniremington.concesionaria.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniremington.concesionaria.models.Marca;

public interface MarcaRepository extends JpaRepository<Marca, Long> {

    long countByNombreIgnoreCase(String nombre);

}