package co.edu.uniremington.concesionaria.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ResultadoAsesoriaRequest(
 @NotBlank String resultado,
 String observaciones,
 String motivoPerdida,
 BigDecimal presupuesto,
 String formaPago,
 Long financiacionId,
 Boolean crearSeguimiento,
 String fechaSeguimiento,
 String medioSeguimiento,
 BigDecimal cuotaInicial,
 Integer plazoMeses
) {}
