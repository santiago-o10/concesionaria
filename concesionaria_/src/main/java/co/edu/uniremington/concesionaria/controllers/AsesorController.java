package co.edu.uniremington.concesionaria.controllers;

import co.edu.uniremington.concesionaria.models.Asesor;
import co.edu.uniremington.concesionaria.services.AsesorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asesores")
public class AsesorController {

    private final AsesorService service;

    public AsesorController(AsesorService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Asesor>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/activos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Asesor>> listarActivos() {
        return ResponseEntity.ok(service.listarActivos());
    }

    @GetMapping("/concesionaria/{idConcesionaria}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Asesor>> listarPorConcesionaria(@PathVariable Long idConcesionaria) {
        return ResponseEntity.ok(service.listarPorConcesionaria(idConcesionaria));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Asesor> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Asesor> crear(@Valid @RequestBody Asesor entidad) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(entidad));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Asesor> actualizar(
            @PathVariable Long id, @Valid @RequestBody Asesor entidad) {
        return ResponseEntity.ok(service.actualizar(id, entidad));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Asesor> cambiarEstado(@PathVariable Long id,
                                                 @Valid @RequestBody co.edu.uniremington.concesionaria.dto.CambiarEstadoRequest body) {
        service.cambiarEstado(id, body.estado());
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ASESOR')")
    public ResponseEntity<Asesor> miPerfil(Authentication authentication) {
        Long id = (Long) ((org.springframework.security.authentication.UsernamePasswordAuthenticationToken) authentication).getDetails();
        return ResponseEntity.ok(service.buscarPorId(id));
    }
}
