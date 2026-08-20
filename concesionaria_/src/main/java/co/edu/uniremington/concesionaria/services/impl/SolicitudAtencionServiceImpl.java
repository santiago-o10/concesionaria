package co.edu.uniremington.concesionaria.services.impl;

import co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException;
import co.edu.uniremington.concesionaria.exceptions.ReglaNegocioException;
import co.edu.uniremington.concesionaria.models.Asesor;
import co.edu.uniremington.concesionaria.models.SolicitudAtencion;
import co.edu.uniremington.concesionaria.models.Vehiculo;
import co.edu.uniremington.concesionaria.repositorys.AsesorRepository;
import co.edu.uniremington.concesionaria.repositorys.ClienteRepository;
import co.edu.uniremington.concesionaria.repositorys.SolicitudAtencionRepository;
import co.edu.uniremington.concesionaria.services.SolicitudAtencionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SolicitudAtencionServiceImpl implements SolicitudAtencionService {

    private static final int DURACION_ASESORIA_HORAS = 1;
    private static final int MAX_CITAS_FUTURAS_CLIENTE = 2;
    private static final int TOLERANCIA_MINUTOS = 15;
    private static final int MAX_INASISTENCIAS_BLOQUEO = 3;
    private static final int HORAS_BLOQUEO_INASISTENCIAS = 24;

    private static final List<String> ESTADOS_VALIDOS = List.of(
            "PENDIENTE", "ATENDIENDO", "REALIZADA", "NO_ASISTIO", "CANCELADA"
    );

    private final SolicitudAtencionRepository repository;
    private final AsesorRepository asesorRepository;
    private final ClienteRepository clienteRepository;

    public SolicitudAtencionServiceImpl(
            SolicitudAtencionRepository repository,
            AsesorRepository asesorRepository,
            ClienteRepository clienteRepository) {
        this.repository = repository;
        this.asesorRepository = asesorRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public List<SolicitudAtencion> listarTodos() {
        List<SolicitudAtencion> solicitudes = repository.findAll();
        actualizarEstadosVencidos(solicitudes);
        return solicitudes;
    }

    @Override
    @Transactional
    public SolicitudAtencion buscarPorId(Long id) {
        SolicitudAtencion solicitud = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una solicitud de atención con id " + id));
        actualizarEstadoVencido(solicitud);
        return solicitud;
    }

    @Override
    @Transactional
    public synchronized SolicitudAtencion crear(SolicitudAtencion solicitud) {
        validarDatosSolicitud(solicitud);

        Long idCliente = solicitud.getCliente().getIdCliente();
        var cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente."));
        if (!"ACTIVO".equalsIgnoreCase(cliente.getEstado())) {
            throw new ReglaNegocioException("El cliente está inactivo y no puede reservar asesorías.");
        }
        solicitud.setCliente(cliente);
        List<SolicitudAtencion> delCliente = repository.findByClienteIdCliente(idCliente);
        actualizarEstadosVencidos(delCliente);

        LocalDateTime ahora = LocalDateTime.now();
        long futurasActivas = delCliente.stream()
                .filter(this::esCitaActiva)
                .filter(x -> fechaHoraInicio(x).isAfter(ahora))
                .count();

        if (futurasActivas >= MAX_CITAS_FUTURAS_CLIENTE) {
            throw new ReglaNegocioException(
                    "Cada cliente puede tener máximo 2 citas futuras activas.");
        }

        long inasistenciasRecientes = delCliente.stream()
                .filter(x -> "NO_ASISTIO".equalsIgnoreCase(x.getEstado()))
                .filter(x -> x.getFechaAtencion() != null && x.getHoraAtencion() != null)
                .filter(x -> !fechaHoraInicio(x).isBefore(
                        ahora.minusHours(HORAS_BLOQUEO_INASISTENCIAS)))
                .count();

        if (inasistenciasRecientes >= MAX_INASISTENCIAS_BLOQUEO) {
            throw new ReglaNegocioException(
                    "No puedes reservar nuevas citas temporalmente porque registras 3 inasistencias recientes.");
        }

        if (tieneCruce(delCliente, solicitud, null)) {
            throw new ReglaNegocioException(
                    "Ya tienes otra cita que se cruza con el horario seleccionado.");
        }

        Asesor asesor = buscarAsesorDisponible(
                solicitud.getFechaAtencion(),
                solicitud.getHoraAtencion(),
                null,
                null,
                solicitud.getVehiculo()
        );

        solicitud.setAsesor(asesor);
        solicitud.setDisponibilidad(null);
        solicitud.setFechaSolicitud(LocalDateTime.now());
        solicitud.setEstado("PENDIENTE");

        return repository.save(solicitud);
    }

    @Override
    @Transactional
    public SolicitudAtencion actualizar(Long id, SolicitudAtencion solicitud) {
        SolicitudAtencion actual = buscarPorId(id);
        String estadoActualEdicion = actual.getEstado() == null ? "" : actual.getEstado().toUpperCase();
        if (!"PENDIENTE".equals(estadoActualEdicion)) {
            throw new ReglaNegocioException("Solo una cita pendiente puede ser reprogramada desde administración.");
        }

        if (solicitud.getFechaAtencion() == null || solicitud.getHoraAtencion() == null) {
            throw new ReglaNegocioException("Debe indicar fecha y hora de atención");
        }

        validarFechaHoraFutura(solicitud.getFechaAtencion(), solicitud.getHoraAtencion());

        Long clienteId = actual.getCliente() != null ? actual.getCliente().getIdCliente() : null;
        if (clienteId != null) {
            List<SolicitudAtencion> delCliente = repository.findByClienteIdCliente(clienteId);
            if (tieneCruce(delCliente, solicitud, actual.getIdSolicitud())) {
                throw new ReglaNegocioException(
                        "El cliente ya tiene otra cita que se cruza con el nuevo horario.");
            }
        }

        Asesor asesor = buscarAsesorDisponible(
                solicitud.getFechaAtencion(),
                solicitud.getHoraAtencion(),
                null,
                actual.getIdSolicitud(),
                solicitud.getVehiculo()
        );

        actual.setFechaAtencion(solicitud.getFechaAtencion());
        actual.setHoraAtencion(solicitud.getHoraAtencion());
        actual.setTipoAtencion(solicitud.getTipoAtencion());
        actual.setAsesor(asesor);
        actual.setDisponibilidad(null);

        // El estado no se cambia mediante la edición administrativa.
        // Las transiciones se realizan exclusivamente por /estado o por el cierre comercial.
        actual.setEstado("PENDIENTE");
        return repository.save(actual);
    }

    @Override
    public void eliminar(Long id) {
        repository.delete(buscarPorId(id));
    }

    @Override
    @Transactional
    public List<SolicitudAtencion> listarPorCliente(Long idCliente) {
        List<SolicitudAtencion> solicitudes = repository.findByClienteIdCliente(idCliente);
        actualizarEstadosVencidos(solicitudes);
        return solicitudes;
    }

    @Override
    @Transactional
    public List<SolicitudAtencion> listarPorAsesor(Long idAsesor) {
        List<SolicitudAtencion> solicitudes = repository.findByAsesorIdAsesor(idAsesor);
        actualizarEstadosVencidos(solicitudes);
        return solicitudes;
    }

    @Override
    @Transactional
    public SolicitudAtencion cambiarEstado(Long id, String nuevoEstado) {
        validarEstado(nuevoEstado);

        SolicitudAtencion solicitud = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una solicitud de atención con id " + id));

        actualizarEstadoVencido(solicitud);

        String estadoActual = solicitud.getEstado() == null ? "" : solicitud.getEstado().toUpperCase();
        if (List.of("CANCELADA", "REALIZADA", "NO_ASISTIO").contains(estadoActual)) {
            throw new ReglaNegocioException("Esta cita ya fue cerrada y su estado no puede modificarse.");
        }

        LocalDateTime inicioProgramado = fechaHoraInicio(solicitud);
        LocalDateTime ahora = LocalDateTime.now();
        String estado = nuevoEstado.toUpperCase();

        if ("CANCELADA".equals(estado) && !ahora.isBefore(inicioProgramado.minusHours(1))) {
            throw new ReglaNegocioException("La cita solo puede cancelarse hasta 1 hora antes de su inicio.");
        }

        if ("ATENDIENDO".equals(estado)) {
            if (ahora.isBefore(inicioProgramado)) {
                throw new ReglaNegocioException("La atención no puede iniciarse antes de la hora de la cita.");
            }
            solicitud.setEstado("ATENDIENDO");
            solicitud.setFechaInicioAtencion(
                    solicitud.getFechaInicioAtencion() != null
                            ? solicitud.getFechaInicioAtencion()
                            : ahora);
            return repository.save(solicitud);
        }

        if ("REALIZADA".equals(estado)) {
            if (!"ATENDIENDO".equals(estadoActual)) {
                throw new ReglaNegocioException("La cita debe estar en atención antes de finalizarla.");
            }
            solicitud.setFechaFinAtencion(ahora);
            solicitud.setEstado("REALIZADA");
            return repository.save(solicitud);
        }

        if ("NO_ASISTIO".equals(estado)) {
            if (ahora.isBefore(inicioProgramado.plusMinutes(TOLERANCIA_MINUTOS))) {
                throw new ReglaNegocioException("La cita todavía está dentro de los 15 minutos de tolerancia.");
            }
            if (!"PENDIENTE".equals(estadoActual)) {
                throw new ReglaNegocioException("Solo una cita pendiente puede marcarse como no asistió.");
            }
            solicitud.setEstado("NO_ASISTIO");
            return repository.save(solicitud);
        }

        solicitud.setEstado(estado);
        return repository.save(solicitud);
    }

    @Override
    @Transactional
    public SolicitudAtencion registrarAtencionPresencial(Long idAsesor, SolicitudAtencion solicitud) {
        if (solicitud.getCliente() == null || solicitud.getCliente().getIdCliente() == null
                || solicitud.getVehiculo() == null || solicitud.getVehiculo().getIdVehiculo() == null) {
            throw new ReglaNegocioException("Para registrar una atención presencial debe seleccionar un cliente y un vehículo.");
        }
        Asesor asesor = asesorRepository.findById(idAsesor)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el asesor."));
        if (!"ACTIVO".equalsIgnoreCase(asesor.getEstado())) {
            throw new ReglaNegocioException("El asesor no está activo para atender en este momento.");
        }
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now().withSecond(0).withNano(0);
        if (!estaDentroDelHorarioLaboral(asesor, ahora)) {
            throw new ReglaNegocioException("El asesor está fuera de su horario laboral.");
        }
        if (!estaLibre(asesor, hoy, ahora, null)) {
            throw new ReglaNegocioException("El asesor ya está ocupado en este momento.");
        }
        solicitud.setFechaSolicitud(LocalDateTime.now());
        solicitud.setFechaAtencion(hoy);
        solicitud.setHoraAtencion(ahora);
        solicitud.setTipoAtencion("PRESENCIAL");
        solicitud.setEstado("ATENDIENDO");
        solicitud.setFechaInicioAtencion(LocalDateTime.now());
        solicitud.setAsesor(asesor);
        solicitud.setDisponibilidad(null);
        return repository.save(solicitud);
    }

    private void validarEstado(String estado) {
        if (estado == null || !ESTADOS_VALIDOS.contains(estado.toUpperCase())) {
            throw new ReglaNegocioException(
                    "El estado debe ser uno de: " + String.join(", ", ESTADOS_VALIDOS));
        }
    }

    private void validarDatosSolicitud(SolicitudAtencion solicitud) {
        if (solicitud.getCliente() == null || solicitud.getCliente().getIdCliente() == null) {
            throw new ReglaNegocioException("Debe indicar un cliente válido.");
        }
        if (solicitud.getVehiculo() == null || solicitud.getVehiculo().getIdVehiculo() == null) {
            throw new ReglaNegocioException("Debe indicar un vehículo válido.");
        }
        if (solicitud.getFechaAtencion() == null || solicitud.getHoraAtencion() == null) {
            throw new ReglaNegocioException("Debe indicar fecha y hora de atención.");
        }
        validarFechaHoraFutura(solicitud.getFechaAtencion(), solicitud.getHoraAtencion());
    }

    private void validarFechaHoraFutura(LocalDate fecha, LocalTime hora) {
        LocalDateTime inicio = LocalDateTime.of(fecha, hora);
        if (inicio.isBefore(LocalDateTime.now())) {
            throw new ReglaNegocioException("La cita debe programarse para una fecha y hora futuras.");
        }
    }

    private Asesor buscarAsesorDisponible(
            LocalDate fecha,
            LocalTime horaInicio,
            Long asesorExcluido,
            Long solicitudExcluida,
            Vehiculo vehiculoSolicitado) {

        LocalTime horaFin = horaInicio.plusHours(DURACION_ASESORIA_HORAS);

        if (horaInicio.isBefore(LocalTime.of(8, 0))
                || horaFin.isAfter(LocalTime.of(18, 0))) {
            throw new ReglaNegocioException(
                    "La asesoría debe estar entre las 08:00 y las 18:00 y durar 1 hora.");
        }

        List<Asesor> candidatos = asesorRepository.findByEstadoIgnoreCase("ACTIVO")
                .stream()
                .filter(a -> a.getIdAsesor() != null)
                .filter(a -> asesorExcluido == null || !a.getIdAsesor().equals(asesorExcluido))
                .filter(a -> estaDentroDelHorarioLaboral(a, horaInicio))
                .filter(a -> estaLibre(a, fecha, horaInicio, solicitudExcluida))
                .toList();

        if (candidatos.isEmpty()) {
            throw new ReglaNegocioException(
                    "No hay asesores disponibles para la fecha y hora seleccionadas.");
        }

        return candidatos.stream()
                .sorted(
                        Comparator
                                .comparingInt((Asesor a) -> puntajeEspecialidad(a, vehiculoSolicitado)).reversed()
                                .thenComparingInt(a -> contarCitasDelDia(a.getIdAsesor(), fecha))
                                .thenComparingInt(a -> contarCitasHistoricas(a.getIdAsesor()))
                                .thenComparing(Asesor::getIdAsesor)
                )
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(
                        "No fue posible asignar un asesor."));
    }

    private int puntajeEspecialidad(Asesor asesor, Vehiculo vehiculo) {
        if (asesor == null || vehiculo == null || asesor.getEspecialidad() == null) {
            return 0;
        }

        String especialidad = normalizar(asesor.getEspecialidad());
        String modelo = "";
        String marca = "";

        if (vehiculo.getModelo() != null) {
            modelo = normalizar(vehiculo.getModelo().getNombre());

            if (vehiculo.getModelo().getMarca() != null) {
                marca = normalizar(vehiculo.getModelo().getMarca().getNombre());
            }
        }

        int puntos = 0;

        if (!modelo.isBlank() && especialidad.contains(modelo)) {
            puntos += 5;
        }

        if (!marca.isBlank() && especialidad.contains(marca)) {
            puntos += 3;
        }

        return puntos;
    }

    private int contarCitasDelDia(Long idAsesor, LocalDate fecha) {
        return (int) repository
                .findByAsesorIdAsesorAndFechaAtencion(idAsesor, fecha)
                .stream()
                .filter(this::esCitaActiva)
                .count();
    }

    private int contarCitasHistoricas(Long idAsesor) {
        return (int) repository
                .findByAsesorIdAsesor(idAsesor)
                .stream()
                .filter(this::esCitaActiva)
                .count();
    }

    private String normalizar(String valor) {
        return java.text.Normalizer.normalize(
                        valor,
                        java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }

    private boolean estaDentroDelHorarioLaboral(Asesor asesor, LocalTime horaInicio) {
        // Jornada oficial de todos los asesores: 08:00 a 18:00.
        // No dependemos de los valores antiguos almacenados en la BD para calcular
        // disponibilidad, evitando que un dato antiguo (por ejemplo 12:00) cierre
        // artificialmente la jornada.
        if (asesor == null || asesor.getIdAsesor() == null) return false;

        LocalTime inicioJornada = LocalTime.of(8, 0);
        LocalTime finJornada = LocalTime.of(18, 0);
        LocalTime horaFin = horaInicio.plusHours(DURACION_ASESORIA_HORAS);

        return !horaInicio.isBefore(inicioJornada)
                && !horaFin.isAfter(finJornada);
    }

    private boolean estaLibre(Asesor asesor, LocalDate fecha, LocalTime horaInicio, Long solicitudExcluida) {
        LocalTime horaFin = horaInicio.plusHours(DURACION_ASESORIA_HORAS);

        return repository.findByAsesorIdAsesorAndFechaAtencion(asesor.getIdAsesor(), fecha)
                .stream()
                .filter(x -> solicitudExcluida == null || !x.getIdSolicitud().equals(solicitudExcluida))
                .filter(this::esCitaActiva)
                .noneMatch(x -> {
                    LocalTime existenteInicio = x.getHoraAtencion();
                    LocalTime existenteFin = existenteInicio.plusHours(DURACION_ASESORIA_HORAS);
                    return existenteInicio.isBefore(horaFin) && horaInicio.isBefore(existenteFin);
                });
    }

    private boolean tieneCruce(List<SolicitudAtencion> citas, SolicitudAtencion nueva, Long excluida) {
        LocalTime inicio = nueva.getHoraAtencion();
        LocalTime fin = inicio.plusHours(DURACION_ASESORIA_HORAS);

        return citas.stream()
                .filter(x -> excluida == null || !x.getIdSolicitud().equals(excluida))
                .filter(this::esCitaActiva)
                .filter(x -> x.getFechaAtencion().equals(nueva.getFechaAtencion()))
                .anyMatch(x -> {
                    LocalTime existenteInicio = x.getHoraAtencion();
                    LocalTime existenteFin = existenteInicio.plusHours(DURACION_ASESORIA_HORAS);
                    return existenteInicio.isBefore(fin) && inicio.isBefore(existenteFin);
                });
    }

    private boolean esCitaActiva(SolicitudAtencion x) {
        String estado = x.getEstado() == null ? "" : x.getEstado().toUpperCase();
        return !"CANCELADA".equals(estado) && !"NO_ASISTIO".equals(estado) && !"REALIZADA".equals(estado);
    }

    private LocalDateTime fechaHoraInicio(SolicitudAtencion x) {
        return LocalDateTime.of(x.getFechaAtencion(), x.getHoraAtencion());
    }

    private void actualizarEstadosVencidos(List<SolicitudAtencion> solicitudes) {
        boolean cambios = false;
        for (SolicitudAtencion x : solicitudes) cambios = actualizarEstadoVencido(x) || cambios;
        if (cambios) repository.saveAll(solicitudes);
    }

    private boolean actualizarEstadoVencido(SolicitudAtencion x) {
        if (!"PENDIENTE".equalsIgnoreCase(x.getEstado())
                || x.getFechaAtencion() == null || x.getHoraAtencion() == null) return false;

        LocalDateTime limite = fechaHoraInicio(x).plusMinutes(TOLERANCIA_MINUTOS);
        if (LocalDateTime.now().isAfter(limite)) {
            x.setEstado("NO_ASISTIO");
            return true;
        }
        return false;
    }

    @Override
    public List<LocalTime> horariosDisponibles(LocalDate fecha) {
        if (fecha == null || fecha.isBefore(LocalDate.now())) return List.of();

        List<LocalTime> horarios = new ArrayList<>();
        LocalTime ahora = LocalTime.now();
        for (LocalTime hora = LocalTime.of(8, 0);
             hora.plusHours(DURACION_ASESORIA_HORAS).compareTo(LocalTime.of(18, 0)) <= 0;
             hora = hora.plusHours(DURACION_ASESORIA_HORAS)) {

            final LocalTime inicio = hora;
            if (fecha.equals(LocalDate.now()) && !inicio.isAfter(ahora)) continue;

            boolean libre = asesorRepository.findByEstadoIgnoreCase("ACTIVO").stream()
                    .anyMatch(a -> estaDentroDelHorarioLaboral(a, inicio)
                            && estaLibre(a, fecha, inicio, null));
            if (libre) horarios.add(inicio);
        }
        return horarios;
    }
}
