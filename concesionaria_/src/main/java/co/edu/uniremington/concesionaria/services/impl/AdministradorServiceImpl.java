package co.edu.uniremington.concesionaria.services.impl;

import co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException;
import co.edu.uniremington.concesionaria.exceptions.RecursoDuplicadoException;
import co.edu.uniremington.concesionaria.models.Administrador;
import co.edu.uniremington.concesionaria.repositorys.AdministradorRepository;
import co.edu.uniremington.concesionaria.services.AdministradorService;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class AdministradorServiceImpl implements AdministradorService {

    private final AdministradorRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AdministradorServiceImpl(AdministradorRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Administrador> listarTodos() {
        return repository.findAll();
    }

    @Override
    public Administrador buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un administrador con id " + id));
    }

    @Override
    public Administrador crear(Administrador entidad) {
        if (repository.countByUsuario(entidad.getUsuario()) > 0) {
            throw new RecursoDuplicadoException("Ya existe un administrador con el usuario " + entidad.getUsuario());
        }
        if (repository.countByCorreo(entidad.getCorreo()) > 0) {
            throw new RecursoDuplicadoException("Ya existe un administrador con el correo " + entidad.getCorreo());
        }
        entidad.setContrasena(passwordEncoder.encode(entidad.getContrasena()));
        return repository.save(entidad);
    }

    @Override
    public Administrador actualizar(Long id, Administrador entidad) {
        Administrador actual = buscarPorId(id);
        if (!actual.getUsuario().equals(entidad.getUsuario())
                && repository.countByUsuario(entidad.getUsuario()) > 0) {
            throw new RecursoDuplicadoException("Ya existe un administrador con el usuario " + entidad.getUsuario());
        }
        if (!actual.getCorreo().equals(entidad.getCorreo())
                && repository.countByCorreo(entidad.getCorreo()) > 0) {
            throw new RecursoDuplicadoException("Ya existe un administrador con el correo " + entidad.getCorreo());
        }
        actual.setNombre(entidad.getNombre());
        actual.setCorreo(entidad.getCorreo());
        actual.setUsuario(entidad.getUsuario());
        actual.setContrasena(passwordEncoder.encode(entidad.getContrasena()));
        return repository.save(actual);
    }

    @Override
    public Administrador iniciarSesion(String usuario, String contrasena) {
        Administrador administrador = repository.findAllByUsuario(usuario)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un administrador con ese usuario"));

        if (!passwordEncoder.matches(contrasena, administrador.getContrasena())) {
            throw new RecursoNoEncontradoException("Usuario o contraseña incorrectos");
        }

        return administrador;
    }

    @Override
    public void eliminar(Long id) {
        Administrador actual = buscarPorId(id);
        repository.delete(actual);
    }
}
