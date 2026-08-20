package co.edu.uniremington.concesionaria.services.impl;

import co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException;
import co.edu.uniremington.concesionaria.exceptions.RecursoDuplicadoException;
import co.edu.uniremington.concesionaria.models.Asesor;
import co.edu.uniremington.concesionaria.repositorys.AsesorRepository;
import co.edu.uniremington.concesionaria.repositorys.SolicitudAtencionRepository;
import co.edu.uniremington.concesionaria.models.SolicitudAtencion;
import java.time.LocalDateTime;
import java.time.LocalTime;
import co.edu.uniremington.concesionaria.services.AsesorService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsesorServiceImpl implements AsesorService {
    private static final LocalTime HORA_INICIO_JORNADA = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN_JORNADA = LocalTime.of(18, 0);


    private final AsesorRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final SolicitudAtencionRepository solicitudRepository;

    public AsesorServiceImpl(AsesorRepository repository, PasswordEncoder passwordEncoder,
                             SolicitudAtencionRepository solicitudRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.solicitudRepository = solicitudRepository;
    }

    @Override
    public List<Asesor> listarTodos() {
        return repository.findAll();
    }

    @Override
    public Asesor buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un asesor con id " + id));
    }

    @Override
    public Asesor crear(Asesor entidad) {
        if (entidad.getUsuario() == null || entidad.getUsuario().isBlank()) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        if (entidad.getContrasena() == null || entidad.getContrasena().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (repository.countByUsuario(entidad.getUsuario()) > 0) {
            throw new RecursoDuplicadoException(
                    "Ya existe un asesor con el usuario " + entidad.getUsuario());
        }

        entidad.setUsuario(entidad.getUsuario().trim());
        entidad.setContrasena(passwordEncoder.encode(entidad.getContrasena()));

        // La jornada de los asesores es fija: 08:00 a 18:00.
        entidad.setHoraInicioTrabajo(HORA_INICIO_JORNADA);
        entidad.setHoraFinTrabajo(HORA_FIN_JORNADA);

        return repository.save(entidad);
    }

    @Override
    public Asesor actualizar(Long id, Asesor entidad) {
        Asesor actual = buscarPorId(id);

        String usuarioNuevo = entidad.getUsuario();
        if (usuarioNuevo == null || usuarioNuevo.isBlank()) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        usuarioNuevo = usuarioNuevo.trim();

        if (!usuarioNuevo.equalsIgnoreCase(actual.getUsuario())
                && repository.countByUsuario(usuarioNuevo) > 0) {
            throw new RecursoDuplicadoException(
                    "Ya existe un asesor con el usuario " + usuarioNuevo);
        }

        actual.setNombre(entidad.getNombre());
        actual.setApellido(entidad.getApellido());
        actual.setTelefono(entidad.getTelefono());
        actual.setCorreo(entidad.getCorreo());
        actual.setEspecialidad(entidad.getEspecialidad());
        String estadoNuevo = entidad.getEstado() == null ? actual.getEstado() : entidad.getEstado().toUpperCase();
        if ("INACTIVO".equals(estadoNuevo) && "ACTIVO".equalsIgnoreCase(actual.getEstado())) {
            validarPuedeDesactivar(id);
        }
        actual.setEstado(estadoNuevo);

        // No se permite guardar una jornada distinta a 08:00–18:00.
        actual.setHoraInicioTrabajo(HORA_INICIO_JORNADA);
        actual.setHoraFinTrabajo(HORA_FIN_JORNADA);

        actual.setUsuario(usuarioNuevo);
        actual.setConcesionaria(entidad.getConcesionaria());

        // En edición, una contraseña vacía significa "conservar la actual".
        if (entidad.getContrasena() != null && !entidad.getContrasena().isBlank()) {
            actual.setContrasena(passwordEncoder.encode(entidad.getContrasena()));
        }

        return repository.save(actual);
    }

    private void validarPuedeDesactivar(Long id) {
        LocalDateTime ahora = LocalDateTime.now();
        boolean tieneCitaFutura = solicitudRepository.findByAsesorIdAsesor(id).stream()
                .filter(x -> x.getFechaAtencion() != null && x.getHoraAtencion() != null)
                .filter(x -> x.getEstado() != null)
                .filter(x -> List.of("PENDIENTE", "ATENDIENDO").contains(x.getEstado().toUpperCase()))
                .anyMatch(x -> LocalDateTime.of(x.getFechaAtencion(), x.getHoraAtencion()).isAfter(ahora));

        if (tieneCitaFutura) {
            throw new co.edu.uniremington.concesionaria.exceptions.ReglaNegocioException(
                    "No puedes desactivar este asesor porque tiene citas futuras pendientes.");
        }
    }

    @Override
    public List<Asesor> listarPorConcesionaria(Long idConcesionaria) {
        return repository.findByConcesionariaIdConcesionaria(idConcesionaria);
    }

    @Override
    public List<Asesor> listarActivos() {
        return repository.findByEstadoIgnoreCase("ACTIVO");
    }

    @Override
    public void cambiarEstado(Long id, String estado) {
        if (estado == null || !List.of("ACTIVO", "INACTIVO").contains(estado.toUpperCase())) {
            throw new IllegalArgumentException("El estado debe ser ACTIVO o INACTIVO.");
        }

        Asesor actual = buscarPorId(id);
        String nuevoEstado = estado.toUpperCase();

        if ("INACTIVO".equals(nuevoEstado) && "ACTIVO".equalsIgnoreCase(actual.getEstado())) {
            validarPuedeDesactivar(id);
        }

        actual.setEstado(nuevoEstado);
        repository.save(actual);
    }
}
