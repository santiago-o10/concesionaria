package co.edu.uniremington.concesionaria.controllers;

import co.edu.uniremington.concesionaria.models.Financiacion;
import co.edu.uniremington.concesionaria.services.FinanciacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financiaciones")
public class FinanciacionController {

    private final FinanciacionService service;

    public FinanciacionController(FinanciacionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Financiacion>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Financiacion> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Financiacion> crear(@Valid @RequestBody Financiacion entidad) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(entidad));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Financiacion> actualizar(
            @PathVariable Long id, @Valid @RequestBody Financiacion entidad) {
        return ResponseEntity.ok(service.actualizar(id, entidad));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/concesionaria/{idConcesionaria}")
    public ResponseEntity<List<Financiacion>> listarPorConcesionaria(@PathVariable Long idConcesionaria) {
        return ResponseEntity.ok(service.listarPorConcesionaria(idConcesionaria));
    }

}
