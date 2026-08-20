package co.edu.uniremington.concesionaria.dto;

import jakarta.validation.constraints.NotBlank;

public record CierreOportunidadRequest(
    @NotBlank String accion,
    String motivo,
    String observaciones,
    String fechaSeguimiento,
    String medioSeguimiento
) {}
