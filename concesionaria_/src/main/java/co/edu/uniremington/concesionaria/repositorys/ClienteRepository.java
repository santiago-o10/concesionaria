package co.edu.uniremington.concesionaria.repositorys;

import co.edu.uniremington.concesionaria.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    long countByDocumento(String documento);
    long countByUsuario(String usuario);
    long countByCorreoIgnoreCase(String correo);
    long countByTelefono(String telefono);
    List<Cliente> findAllByUsuario(String usuario);
    List<Cliente> findAllByDocumento(String documento);
}
