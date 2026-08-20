package co.edu.uniremington.concesionaria.controllers;

import co.edu.uniremington.concesionaria.models.Cliente;
import co.edu.uniremington.concesionaria.dto.ActualizarPerfilClienteRequest;
import co.edu.uniremington.concesionaria.repositorys.ClienteRepository;
import co.edu.uniremington.concesionaria.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService service;
    public ClienteController(ClienteService service, ClienteRepository repository) { this.service = service; }

    @GetMapping @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Cliente>> listarTodos() { return ResponseEntity.ok(service.listarTodos()); }

    @GetMapping("/para-asesor")
    @PreAuthorize("hasRole('ASESOR')")
    public ResponseEntity<List<Cliente>> listarParaAsesor() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/me") @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Cliente> miPerfil(Authentication authentication) {
        Long id = (Long) ((org.springframework.security.authentication.UsernamePasswordAuthenticationToken) authentication).getDetails();
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/{id}") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable Long id) { return ResponseEntity.ok(service.buscarPorId(id)); }

    @PutMapping("/me") @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Cliente> actualizarMiPerfil(
            @Valid @RequestBody ActualizarPerfilClienteRequest request,
            Authentication authentication) {
        Long id = (Long) ((org.springframework.security.authentication.UsernamePasswordAuthenticationToken) authentication).getDetails();
        return ResponseEntity.ok(service.actualizarMiPerfil(id, request));
    }


    @PostMapping @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Cliente> crear(@Valid @RequestBody Cliente entidad) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(entidad));
    }

    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Cliente> actualizar(@PathVariable Long id, @Valid @RequestBody Cliente entidad) {
        return ResponseEntity.ok(service.actualizar(id, entidad));
    }

    @PutMapping("/{id}/estado") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Cliente> cambiarEstado(@PathVariable Long id, @Valid @RequestBody co.edu.uniremington.concesionaria.dto.CambiarEstadoRequest body) {
        service.cambiarEstado(id, body.estado());
        return ResponseEntity.ok(service.buscarPorId(id));
    }
}
