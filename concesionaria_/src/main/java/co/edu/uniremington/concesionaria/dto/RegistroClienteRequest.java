package co.edu.uniremington.concesionaria.dto;

import jakarta.validation.constraints.*;

public record RegistroClienteRequest(
        @NotBlank @Size(min=2, max=80) String nombre,
        @NotBlank @Size(min=2, max=80) String apellido,
        @NotBlank @Size(min=5, max=30) String documento,
        @NotBlank String telefono,
        @Email String correo,
        @Size(max=80) String ciudad,
        @NotBlank @Size(min=4, max=50) String usuario,
        @NotBlank @Size(min=6, max=100) String contrasena
) {}
