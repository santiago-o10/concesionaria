package co.edu.uniremington.concesionaria.controllers;

import co.edu.uniremington.concesionaria.models.Concesionaria;
import co.edu.uniremington.concesionaria.services.ConcesionariaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/concesionarias")
public class ConcesionariaController {

    private final ConcesionariaService service;

    public ConcesionariaController(ConcesionariaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Concesionaria>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Concesionaria> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Concesionaria> crear(@Valid @RequestBody Concesionaria entidad) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(entidad));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Concesionaria> actualizar(
            @PathVariable Long id, @Valid @RequestBody Concesionaria entidad) {
        return ResponseEntity.ok(service.actualizar(id, entidad));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
