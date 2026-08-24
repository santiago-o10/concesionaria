package co.edu.uniremington.concesionaria.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniremington.concesionaria.dto.CambiarEstadoRequest;
import co.edu.uniremington.concesionaria.models.SolicitudAtencion;
import co.edu.uniremington.concesionaria.services.SolicitudAtencionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudAtencionController {

    private final SolicitudAtencionService service;

    public SolicitudAtencionController(SolicitudAtencionService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<SolicitudAtencion>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SolicitudAtencion> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/horarios-disponibles/{fecha}")
    public ResponseEntity<List<LocalTime>> horariosDisponibles(@PathVariable LocalDate fecha) {
        return ResponseEntity.ok(service.horariosDisponibles(fecha));
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<SolicitudAtencion> crear(
            @Valid @RequestBody SolicitudAtencion entidad,
            Authentication authentication) {

        Long idCliente = idAutenticado(authentication);
        if (entidad.getCliente() == null || !idCliente.equals(entidad.getCliente().getIdCliente())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(entidad));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SolicitudAtencion> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudAtencion entidad) {
        return ResponseEntity.ok(service.actualizar(id, entidad));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<SolicitudAtencion>> listarPorCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(service.listarPorCliente(idCliente));
    }

    @GetMapping("/asesor/{idAsesor}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<SolicitudAtencion>> listarPorAsesor(@PathVariable Long idAsesor) {
        return ResponseEntity.ok(service.listarPorAsesor(idAsesor));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ASESOR', 'CLIENTE')")
    public ResponseEntity<SolicitudAtencion> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest body,
            Authentication authentication) {

        String rol = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");

        SolicitudAtencion actual = service.buscarPorId(id);
        String nuevoEstado = body.estado() == null ? "" : body.estado().toUpperCase();

        if ("ROLE_CLIENTE".equals(rol)) {
            Long idCliente = idAutenticado(authentication);

            if (actual.getCliente() == null || !idCliente.equals(actual.getCliente().getIdCliente())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // El cliente solamente puede cancelar su propia cita.
            if (!"CANCELADA".equals(nuevoEstado)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (actual.getFechaAtencion() != null && actual.getHoraAtencion() != null
                    && !java.time.LocalDateTime.now(ZoneId.of("America/Bogota")).isBefore(
                        java.time.LocalDateTime.of(actual.getFechaAtencion(), actual.getHoraAtencion()).minusHours(1))) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } else if ("ROLE_ASESOR".equals(rol)) {
            Long idAsesor = idAutenticado(authentication);

            if (actual.getAsesor() == null || !idAsesor.equals(actual.getAsesor().getIdAsesor())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // El asesor trabaja la cita; no puede cancelarla.
            if ("CANCELADA".equals(nuevoEstado)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Un asesor puede iniciar o terminar su atención y registrar una inasistencia.
            if (!List.of("ATENDIENDO", "REALIZADA", "NO_ASISTIO").contains(nuevoEstado)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else if ("ROLE_ADMINISTRADOR".equals(rol)) {
            if (!List.of("ATENDIENDO", "REALIZADA", "NO_ASISTIO", "CANCELADA").contains(nuevoEstado)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(service.cambiarEstado(id, nuevoEstado));
    }

    @PostMapping("/presencial")
    @PreAuthorize("hasRole('ASESOR')")
    public ResponseEntity<SolicitudAtencion> registrarPresencial(
            @Valid @RequestBody SolicitudAtencion entidad,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registrarAtencionPresencial(idAutenticado(authentication), entidad));
    }

    @GetMapping("/cliente/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<SolicitudAtencion>> misSolicitudes(Authentication authentication) {
        return ResponseEntity.ok(service.listarPorCliente(idAutenticado(authentication)));
    }

    @GetMapping("/asesor/me")
    @PreAuthorize("hasRole('ASESOR')")
    public ResponseEntity<List<SolicitudAtencion>> misCitas(Authentication authentication) {
        return ResponseEntity.ok(service.listarPorAsesor(idAutenticado(authentication)));
    }

    private Long idAutenticado(Authentication authentication) {
        return (Long) ((org.springframework.security.authentication.UsernamePasswordAuthenticationToken)
                authentication).getDetails();
    }
}
