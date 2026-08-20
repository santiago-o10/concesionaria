package co.edu.uniremington.concesionaria.dto;

public record AuthResponse(
        String token,
        String rol,
        Long id,
        String nombre,
        String usuario
) {}
