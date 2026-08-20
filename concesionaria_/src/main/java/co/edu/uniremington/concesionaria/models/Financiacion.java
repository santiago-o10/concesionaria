package co.edu.uniremington.concesionaria.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
@Table(name = "financiaciones")
public class Financiacion {

    @Id
    @SequenceGenerator(name = "financiacion_seq", sequenceName = "financiacion_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "financiacion_seq")
    private Long idFinanciacion;

    @NotBlank(message = "El nombre de la financiación es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 400, message = "La descripción no puede superar 400 caracteres")
    @Column(length = 400)
    private String descripcion;

    @NotNull(message = "El porcentaje inicial es obligatorio")
    @DecimalMin(value = "0.0", message = "El porcentaje inicial no puede ser negativo")
    @DecimalMax(value = "100.0", message = "El porcentaje inicial no puede superar 100")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeInicial;

    @NotNull(message = "El plazo es obligatorio")
    @Positive(message = "El plazo debe ser mayor que cero")
    @Column(nullable = false)
    private Integer plazo;

    @Size(max = 400, message = "Las condiciones no pueden superar 400 caracteres")
    @Column(length = 400)
    private String condiciones;

    @NotNull(message = "La concesionaria es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "concesionaria_id", nullable = false)
    private Concesionaria concesionaria;

    public Financiacion() {
    }

    public Long getIdFinanciacion() { return idFinanciacion; }
    public void setIdFinanciacion(Long idFinanciacion) { this.idFinanciacion = idFinanciacion; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPorcentajeInicial() { return porcentajeInicial; }
    public void setPorcentajeInicial(BigDecimal porcentajeInicial) { this.porcentajeInicial = porcentajeInicial; }

    public Integer getPlazo() { return plazo; }
    public void setPlazo(Integer plazo) { this.plazo = plazo; }

    public String getCondiciones() { return condiciones; }
    public void setCondiciones(String condiciones) { this.condiciones = condiciones; }

    public Concesionaria getConcesionaria() { return concesionaria; }
    public void setConcesionaria(Concesionaria concesionaria) { this.concesionaria = concesionaria; }
}
