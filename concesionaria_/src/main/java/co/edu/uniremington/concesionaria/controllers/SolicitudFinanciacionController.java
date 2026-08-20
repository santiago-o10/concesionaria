package co.edu.uniremington.concesionaria.controllers;

import co.edu.uniremington.concesionaria.models.SolicitudFinanciacion;
import co.edu.uniremington.concesionaria.services.SolicitudFinanciacionService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/solicitudes-financiacion")
public class SolicitudFinanciacionController {
    private final SolicitudFinanciacionService service;
    public SolicitudFinanciacionController(SolicitudFinanciacionService service){this.service=service;}
    private Long id(Authentication a){return (Long)((org.springframework.security.authentication.UsernamePasswordAuthenticationToken)a).getDetails();}

    @GetMapping @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<SolicitudFinanciacion> all(){return service.listarTodos();}
    @GetMapping("/asesor/me") @PreAuthorize("hasRole('ASESOR')")
    public List<SolicitudFinanciacion> asesor(Authentication a){return service.listarPorAsesor(id(a));}
    @GetMapping("/cliente/me") @PreAuthorize("hasRole('CLIENTE')")
    public List<SolicitudFinanciacion> cliente(Authentication a){return service.listarPorCliente(id(a));}
    @PostMapping @PreAuthorize("hasRole('ASESOR')")
    public ResponseEntity<SolicitudFinanciacion> crear(@RequestBody SolicitudFinanciacion s){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(s));
    }
    @PutMapping("/{id}/estado") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public SolicitudFinanciacion estado(@PathVariable Long id,@RequestBody Map<String,String> body){
        return service.actualizarEstado(id,body.get("estado"),body.get("observaciones"));
    }
}
