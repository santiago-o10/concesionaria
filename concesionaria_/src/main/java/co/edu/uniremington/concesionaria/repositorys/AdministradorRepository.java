package co.edu.uniremington.concesionaria.repositorys;

import co.edu.uniremington.concesionaria.models.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    List<Administrador> findAllByUsuario(String usuario);
    long countByUsuario(String usuario);
    long countByCorreo(String correo);

}
