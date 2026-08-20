package co.edu.uniremington.concesionaria.controllers;

import co.edu.uniremington.concesionaria.models.Disponibilidad;
import co.edu.uniremington.concesionaria.services.DisponibilidadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disponibilidades")
public class DisponibilidadController {

    private final DisponibilidadService service;

    public DisponibilidadController(DisponibilidadService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Disponibilidad>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Disponibilidad> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ASESOR')")
    public ResponseEntity<Disponibilidad> crear(@Valid @RequestBody Disponibilidad entidad) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(entidad));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ASESOR')")
    public ResponseEntity<Disponibilidad> actualizar(
            @PathVariable Long id, @Valid @RequestBody Disponibilidad entidad) {
        return ResponseEntity.ok(service.actualizar(id, entidad));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ASESOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/asesor/{idAsesor}")
    public ResponseEntity<List<Disponibilidad>> listarPorAsesor(@PathVariable Long idAsesor) {
        return ResponseEntity.ok(service.listarPorAsesor(idAsesor));
    }

    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<Disponibilidad>> listarPorFecha(@PathVariable java.time.LocalDate fecha) {
        return ResponseEntity.ok(service.listarPorFecha(fecha));
    }

}
