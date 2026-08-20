package co.edu.uniremington.concesionaria.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "oportunidades")
public class Oportunidad {
    @Id @SequenceGenerator(name="oportunidad_seq", sequenceName="oportunidad_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="oportunidad_seq")
    private Long idOportunidad;

    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="cliente_id", nullable=false)
    private Cliente cliente;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="asesor_id", nullable=false)
    private Asesor asesor;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="vehiculo_id", nullable=false)
    private Vehiculo vehiculo;
    @OneToOne(fetch=FetchType.EAGER) @JoinColumn(name="solicitud_atencion_id", unique=true, nullable=false)
    private SolicitudAtencion solicitudAtencion;

    @Column(nullable=false, length=30) private String estado = "INTERESADO";
    @Column(precision=14, scale=2) private BigDecimal presupuesto;
    @Column(name="forma_pago", length=30) private String formaPago;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="financiacion_id")
    private Financiacion financiacion;
    @Column(length=1000) private String observaciones;
    @Column(name="motivo_perdida", length=300) private String motivoPerdida;
    @Column(nullable=false) private LocalDateTime fechaCreacion;
    @Column(nullable=false) private LocalDateTime fechaActualizacion;

    @PrePersist void prePersist(){ fechaCreacion=LocalDateTime.now(); fechaActualizacion=fechaCreacion; }
    @PreUpdate void preUpdate(){ fechaActualizacion=LocalDateTime.now(); }

    public Long getIdOportunidad(){return idOportunidad;} public void setIdOportunidad(Long v){idOportunidad=v;}
    public Cliente getCliente(){return cliente;} public void setCliente(Cliente v){cliente=v;}
    public Asesor getAsesor(){return asesor;} public void setAsesor(Asesor v){asesor=v;}
    public Vehiculo getVehiculo(){return vehiculo;} public void setVehiculo(Vehiculo v){vehiculo=v;}
    public SolicitudAtencion getSolicitudAtencion(){return solicitudAtencion;} public void setSolicitudAtencion(SolicitudAtencion v){solicitudAtencion=v;}
    public String getEstado(){return estado;} public void setEstado(String v){estado=v;}
    public BigDecimal getPresupuesto(){return presupuesto;} public void setPresupuesto(BigDecimal v){presupuesto=v;}
    public String getFormaPago(){return formaPago;} public void setFormaPago(String v){formaPago=v;}
    public Financiacion getFinanciacion(){return financiacion;} public void setFinanciacion(Financiacion v){financiacion=v;}
    public String getObservaciones(){return observaciones;} public void setObservaciones(String v){observaciones=v;}
    public String getMotivoPerdida(){return motivoPerdida;} public void setMotivoPerdida(String v){motivoPerdida=v;}
    public LocalDateTime getFechaCreacion(){return fechaCreacion;} public LocalDateTime getFechaActualizacion(){return fechaActualizacion;}
}