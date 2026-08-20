package co.edu.uniremington.concesionaria.services.impl;

import co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException;
import co.edu.uniremington.concesionaria.exceptions.RecursoDuplicadoException;
import co.edu.uniremington.concesionaria.models.Cliente;
import co.edu.uniremington.concesionaria.dto.ActualizarPerfilClienteRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import co.edu.uniremington.concesionaria.repositorys.ClienteRepository;
import co.edu.uniremington.concesionaria.services.ClienteService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;
    private final PasswordEncoder passwordEncoder;

    public ClienteServiceImpl(ClienteRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Cliente> listarTodos() {
        return repository.findAll();
    }

    @Override
    public Cliente buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un cliente con id " + id));
    }

    @Override
    public Cliente crear(Cliente entidad) {

        if (repository.countByDocumento(entidad.getDocumento()) > 0) {
            throw new RecursoDuplicadoException("Ya existe un cliente con el documento " + entidad.getDocumento());
        }
        if (entidad.getUsuario() != null && !entidad.getUsuario().isBlank()
                && repository.countByUsuario(entidad.getUsuario()) > 0) {
            throw new RecursoDuplicadoException("Ya existe un cliente con ese usuario.");
        }
        if (entidad.getCorreo() != null && !entidad.getCorreo().isBlank()
                && repository.countByCorreoIgnoreCase(entidad.getCorreo()) > 0) {
            throw new RecursoDuplicadoException("Ya existe un cliente con ese correo.");
        }
        if (entidad.getTelefono() != null && !entidad.getTelefono().isBlank()
                && repository.countByTelefono(entidad.getTelefono()) > 0) {
            throw new RecursoDuplicadoException("Ya existe un cliente con ese teléfono.");
        }

        if (entidad.getEstado() == null || entidad.getEstado().isBlank()) entidad.setEstado("ACTIVO");
        return repository.save(entidad);
    }

    @Override
    public Cliente actualizar(Long id, Cliente entidad) {

        Cliente actual = buscarPorId(id);

        if (!actual.getDocumento().equals(entidad.getDocumento())
                && repository.countByDocumento(entidad.getDocumento()) > 0) {
            throw new RecursoDuplicadoException("Ya existe un cliente con el documento " + entidad.getDocumento());
        }
        if (entidad.getUsuario() != null && !entidad.getUsuario().equals(actual.getUsuario())
                && repository.countByUsuario(entidad.getUsuario()) > 0) {
            throw new RecursoDuplicadoException("Ya existe un cliente con ese usuario.");
        }
        if (entidad.getCorreo() != null && !entidad.getCorreo().isBlank()
                && !entidad.getCorreo().equalsIgnoreCase(actual.getCorreo())
                && repository.countByCorreoIgnoreCase(entidad.getCorreo()) > 0) {
            throw new RecursoDuplicadoException("Ya existe un cliente con ese correo.");
        }
        if (entidad.getTelefono() != null && !entidad.getTelefono().isBlank()
                && !entidad.getTelefono().equals(actual.getTelefono())
                && repository.countByTelefono(entidad.getTelefono()) > 0) {
            throw new RecursoDuplicadoException("Ya existe un cliente con ese teléfono.");
        }

        actual.setNombre(entidad.getNombre());
        actual.setApellido(entidad.getApellido());
        actual.setDocumento(entidad.getDocumento());
        actual.setTelefono(entidad.getTelefono());
        actual.setCorreo(entidad.getCorreo());
        actual.setCiudad(entidad.getCiudad());
        if (entidad.getEstado() != null && !entidad.getEstado().isBlank()) {
            actual.setEstado(entidad.getEstado().toUpperCase());
        }

        return repository.save(actual);
    }


    @Override
    public Cliente actualizarMiPerfil(Long id, ActualizarPerfilClienteRequest request) {
        Cliente actual = buscarPorId(id);

        actual.setNombre(request.nombre());
        actual.setApellido(request.apellido());
        actual.setTelefono(request.telefono());
        actual.setCorreo(request.correo());
        actual.setCiudad(request.ciudad());

        if (request.contrasena() != null && !request.contrasena().isBlank()) {
            actual.setContrasena(passwordEncoder.encode(request.contrasena()));
        }

        return repository.save(actual);
    }

    @Override
    public void cambiarEstado(Long id, String estado) {
        if (estado == null || !List.of("ACTIVO", "INACTIVO").contains(estado.toUpperCase())) {
            throw new IllegalArgumentException("El estado debe ser ACTIVO o INACTIVO.");
        }
        Cliente actual = buscarPorId(id);
        actual.setEstado(estado.toUpperCase());
        repository.save(actual);
    }
}