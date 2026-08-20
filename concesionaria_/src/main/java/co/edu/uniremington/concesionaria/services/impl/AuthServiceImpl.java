package co.edu.uniremington.concesionaria.services.impl;

import co.edu.uniremington.concesionaria.dto.*;
import co.edu.uniremington.concesionaria.exceptions.RecursoDuplicadoException;
import co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException;
import co.edu.uniremington.concesionaria.models.*;
import co.edu.uniremington.concesionaria.repositorys.*;
import co.edu.uniremington.concesionaria.security.JwtService;
import co.edu.uniremington.concesionaria.services.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final ClienteRepository clienteRepository;
    private final AsesorRepository asesorRepository;
    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(ClienteRepository clienteRepository, AsesorRepository asesorRepository,
                           AdministradorRepository administradorRepository, PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.clienteRepository = clienteRepository;
        this.asesorRepository = asesorRepository;
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse registrarCliente(RegistroClienteRequest r) {
        if (clienteRepository.countByDocumento(r.documento()) > 0)
            throw new RecursoDuplicadoException("Ya existe un cliente con ese documento");
        if (r.correo() != null && !r.correo().isBlank()
                && clienteRepository.countByCorreoIgnoreCase(r.correo()) > 0)
            throw new RecursoDuplicadoException("Ya existe un cliente con ese correo");
        if (r.telefono() != null && !r.telefono().isBlank()
                && clienteRepository.countByTelefono(r.telefono()) > 0)
            throw new RecursoDuplicadoException("Ya existe un cliente con ese teléfono");
        if (clienteRepository.countByUsuario(r.usuario()) > 0
                || asesorRepository.countByUsuario(r.usuario()) > 0
                || administradorRepository.countByUsuario(r.usuario()) > 0) {
            throw new RecursoDuplicadoException("Ese usuario ya está registrado");
        }

        Cliente c = new Cliente();
        c.setNombre(r.nombre());
        c.setApellido(r.apellido());
        c.setDocumento(r.documento());
        c.setTelefono(r.telefono());
        c.setCorreo(r.correo());
        c.setCiudad(r.ciudad());
        c.setUsuario(r.usuario());
        c.setContrasena(passwordEncoder.encode(r.contrasena()));
        c = clienteRepository.save(c);
        return respuesta(c.getIdCliente(), c.getNombre() + " " + c.getApellido(), c.getUsuario(), "CLIENTE");
    }

    @Override
    public AuthResponse iniciarSesion(LoginRequest r) {
        // El rol se determina automáticamente buscando el usuario
        // entre clientes, asesores y administradores. El usuario no
        // necesita seleccionar su tipo de cuenta.
        var cliente = clienteRepository.findAllByUsuario(r.usuario()).stream().findFirst();
        if (cliente.isPresent()) {
            Cliente c = cliente.get();
            if (!"ACTIVO".equalsIgnoreCase(c.getEstado()))
                throw new RecursoNoEncontradoException("El cliente está inactivo");
            verificar(c.getContrasena(), r.contrasena());
            return respuesta(c.getIdCliente(), c.getNombre() + " " + c.getApellido(), c.getUsuario(), "CLIENTE");
        }

        var asesor = asesorRepository.findAllByUsuario(r.usuario()).stream().findFirst();
        if (asesor.isPresent()) {
            Asesor a = asesor.get();
            if (!"ACTIVO".equalsIgnoreCase(a.getEstado()))
                throw new RecursoNoEncontradoException("El asesor no está activo");
            verificar(a.getContrasena(), r.contrasena());
            return respuesta(a.getIdAsesor(), a.getNombre() + " " + a.getApellido(), a.getUsuario(), "ASESOR");
        }

        var administrador = administradorRepository.findAllByUsuario(r.usuario()).stream().findFirst();
        if (administrador.isPresent()) {
            Administrador a = administrador.get();
            verificar(a.getContrasena(), r.contrasena());
            return respuesta(a.getIdAdministrador(), a.getNombre(), a.getUsuario(), "ADMINISTRADOR");
        }

        throw new RecursoNoEncontradoException("Usuario o contraseña incorrectos");
    }

    private void verificar(String hash, String raw) {
        if (hash == null || !passwordEncoder.matches(raw, hash))
            throw new RecursoNoEncontradoException("Usuario o contraseña incorrectos");
    }

    private AuthResponse respuesta(Long id, String nombre, String usuario, String rol) {
        return new AuthResponse(jwtService.crearToken(id, usuario, rol), rol, id, nombre, usuario);
    }
}
