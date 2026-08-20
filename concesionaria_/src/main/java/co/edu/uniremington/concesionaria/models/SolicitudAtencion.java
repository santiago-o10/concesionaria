package co.edu.uniremington.concesionaria.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "solicitudes_atencion")
public class SolicitudAtencion {

    @Id
    @SequenceGenerator(name = "solicitudatencion_seq", sequenceName = "solicitudatencion_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "solicitudatencion_seq")
    private Long idSolicitud;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    @NotNull(message = "La fecha de atención es obligatoria")
    @Column(nullable = false)
    private LocalDate fechaAtencion;

    @NotNull(message = "La hora de atención es obligatoria")
    @Column(nullable = false)
    private LocalTime horaAtencion;

    @Column(name = "fecha_inicio_atencion")
    private LocalDateTime fechaInicioAtencion;

    @Column(name = "fecha_fin_atencion")
    private LocalDateTime fechaFinAtencion;

    @NotBlank(message = "El tipo de atención es obligatorio")
    @Size(max = 100, message = "El tipo de atención no puede superar 100 caracteres")
    @Column(nullable = false, length = 100)
    private String tipoAtencion;

    @NotBlank(message = "El estado es obligatorio")
    @Column(nullable = false, length = 30)
    private String estado = "PENDIENTE";

    @NotNull(message = "El cliente es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull(message = "El vehículo es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asesor_id")
    private Asesor asesor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "disponibilidad_id")
    private Disponibilidad disponibilidad;

    public SolicitudAtencion() {
    }

    public Long getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Long idSolicitud) { this.idSolicitud = idSolicitud; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public LocalDate getFechaAtencion() { return fechaAtencion; }
    public void setFechaAtencion(LocalDate fechaAtencion) { this.fechaAtencion = fechaAtencion; }

    public LocalTime getHoraAtencion() { return horaAtencion; }
    public void setHoraAtencion(LocalTime horaAtencion) { this.horaAtencion = horaAtencion; }

    public LocalDateTime getFechaInicioAtencion() { return fechaInicioAtencion; }
    public void setFechaInicioAtencion(LocalDateTime fechaInicioAtencion) { this.fechaInicioAtencion = fechaInicioAtencion; }

    public LocalDateTime getFechaFinAtencion() { return fechaFinAtencion; }
    public void setFechaFinAtencion(LocalDateTime fechaFinAtencion) { this.fechaFinAtencion = fechaFinAtencion; }

    public String getTipoAtencion() { return tipoAtencion; }
    public void setTipoAtencion(String tipoAtencion) { this.tipoAtencion = tipoAtencion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }

    public Asesor getAsesor() { return asesor; }
    public void setAsesor(Asesor asesor) { this.asesor = asesor; }

    public Disponibilidad getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(Disponibilidad disponibilidad) { this.disponibilidad = disponibilidad; }
}
