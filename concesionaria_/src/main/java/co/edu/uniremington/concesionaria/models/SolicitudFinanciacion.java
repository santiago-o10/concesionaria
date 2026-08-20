package co.edu.uniremington.concesionaria.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_financiacion")
public class SolicitudFinanciacion {
    @Id
    @SequenceGenerator(name="solicitud_financiacion_seq", sequenceName="solicitud_financiacion_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="solicitud_financiacion_seq")
    private Long idSolicitudFinanciacion;

    @OneToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="oportunidad_id", nullable=false, unique=true)
    private Oportunidad oportunidad;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="financiacion_id", nullable=false)
    private Financiacion financiacion;

    @NotNull @DecimalMin("0.01")
    @Column(name="monto_vehiculo", nullable=false, precision=14, scale=2)
    private BigDecimal montoVehiculo;

    @NotNull @DecimalMin("0.00")
    @Column(name="cuota_inicial", nullable=false, precision=14, scale=2)
    private BigDecimal cuotaInicial;

    @NotNull @DecimalMin("0.01")
    @Column(name="monto_solicitado", nullable=false, precision=14, scale=2)
    private BigDecimal montoSolicitado;

    @NotNull @Positive
    @Column(name="plazo_meses", nullable=false)
    private Integer plazoMeses;

    @Column(nullable=false, length=30)
    private String estado = "PENDIENTE";

    @Column(length=1000)
    private String observaciones;

    @Column(name="fecha_solicitud", nullable=false)
    private LocalDateTime fechaSolicitud;

    @Column(name="fecha_actualizacion", nullable=false)
    private LocalDateTime fechaActualizacion;

    @PrePersist void prePersist() {
        fechaSolicitud = LocalDateTime.now();
        fechaActualizacion = fechaSolicitud;
    }
    @PreUpdate void preUpdate() { fechaActualizacion = LocalDateTime.now(); }

    public Long getIdSolicitudFinanciacion(){return idSolicitudFinanciacion;}
    public void setIdSolicitudFinanciacion(Long v){idSolicitudFinanciacion=v;}
    public Oportunidad getOportunidad(){return oportunidad;}
    public void setOportunidad(Oportunidad v){oportunidad=v;}
    public Financiacion getFinanciacion(){return financiacion;}
    public void setFinanciacion(Financiacion v){financiacion=v;}
    public BigDecimal getMontoVehiculo(){return montoVehiculo;}
    public void setMontoVehiculo(BigDecimal v){montoVehiculo=v;}
    public BigDecimal getCuotaInicial(){return cuotaInicial;}
    public void setCuotaInicial(BigDecimal v){cuotaInicial=v;}
    public BigDecimal getMontoSolicitado(){return montoSolicitado;}
    public void setMontoSolicitado(BigDecimal v){montoSolicitado=v;}
    public Integer getPlazoMeses(){return plazoMeses;}
    public void setPlazoMeses(Integer v){plazoMeses=v;}
    public String getEstado(){return estado;}
    public void setEstado(String v){estado=v;}
    public String getObservaciones(){return observaciones;}
    public void setObservaciones(String v){observaciones=v;}
    public LocalDateTime getFechaSolicitud(){return fechaSolicitud;}
    public LocalDateTime getFechaActualizacion(){return fechaActualizacion;}
}
