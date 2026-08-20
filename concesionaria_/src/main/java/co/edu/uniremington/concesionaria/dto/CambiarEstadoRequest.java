package co.edu.uniremington.concesionaria.dto;

import jakarta.validation.constraints.NotBlank;

public record CambiarEstadoRequest(
        @NotBlank String estado
) {}
