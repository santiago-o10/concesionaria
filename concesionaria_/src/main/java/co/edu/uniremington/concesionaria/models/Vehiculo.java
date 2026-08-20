package co.edu.uniremington.concesionaria.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehiculos")
public class Vehiculo {

    @Id
    @SequenceGenerator(name = "vehiculo_seq", sequenceName = "vehiculo_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vehiculo_seq")
    private Long idVehiculo;

    @NotNull(message = "El año es obligatorio")
    @Min(value = 1900, message = "El año no es válido")
    private Integer anio;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero")
    @Digits(integer = 12, fraction = 2, message = "El precio no tiene un formato válido")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precio;

    @NotBlank(message = "El tipo de vehículo es obligatorio")
    @Size(max = 50, message = "El tipo no puede superar 50 caracteres")
    @Column(nullable = false, length = 50)
    private String tipo;

    @NotBlank(message = "El color es obligatorio")
    @Size(max = 40, message = "El color no puede superar 40 caracteres")
    @Column(nullable = false, length = 40)
    private String color;

    @NotBlank(message = "El motor es obligatorio")
    @Size(max = 80, message = "El motor no puede superar 80 caracteres")
    @Column(nullable = false, length = 80)
    private String motor;

    @NotBlank(message = "La transmisión es obligatoria")
    @Size(max = 40, message = "La transmisión no puede superar 40 caracteres")
    @Column(nullable = false, length = 40)
    private String transmision;

    @NotBlank(message = "El combustible es obligatorio")
    @Size(max = 40, message = "El combustible no puede superar 40 caracteres")
    @Column(nullable = false, length = 40)
    private String combustible;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    @Column(length = 500)
    private String descripcion;

    @Size(max = 120, message = "La cilindrada no puede superar 120 caracteres")
    @Column(length = 120)
    private String cilindrada;

    @Size(max = 120, message = "La potencia no puede superar 120 caracteres")
    @Column(length = 120)
    private String potencia;

    @Size(max = 120, message = "El torque no puede superar 120 caracteres")
    @Column(length = 120)
    private String torque;

    @Size(max = 80, message = "La tracción no puede superar 80 caracteres")
    @Column(length = 80)
    private String traccion;

    @Size(max = 120, message = "El rendimiento no puede superar 120 caracteres")
    @Column(length = 120)
    private String rendimiento;

    private String pasajeros;

    private String capacidadBaul;

    private String largo;

    private String ancho;

    private String alto;

    private String peso;

    @Size(max = 1000, message = "La seguridad no puede superar 1000 caracteres")
    @Column(length = 1000)
    private String seguridad;

    @Size(max = 1500, message = "El equipamiento no puede superar 1500 caracteres")
    @Column(length = 1500)
    private String equipamiento;

    @Size(max = 1000, message = "La URL de imagen no puede superar 1000 caracteres")
    @Column(length = 1000)
    private String imagenUrl;


    @NotBlank(message = "El estado es obligatorio")
    @Column(nullable = false, length = 30)
    private String estado = "DISPONIBLE";

    @NotNull(message = "El modelo es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "modelo_id", nullable = false)
    private Modelo modelo;

    @NotNull(message = "La concesionaria es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "concesionaria_id", nullable = false)
    private Concesionaria concesionaria;

    @JsonIgnore
    @OneToMany(mappedBy = "vehiculo")
    private List<SolicitudAtencion> solicitudes = new ArrayList<>();

    public Vehiculo() {
    }

    public Long getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(Long idVehiculo) { this.idVehiculo = idVehiculo; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getMotor() { return motor; }
    public void setMotor(String motor) { this.motor = motor; }

    public String getTransmision() { return transmision; }
    public void setTransmision(String transmision) { this.transmision = transmision; }

    public String getCombustible() { return combustible; }
    public void setCombustible(String combustible) { this.combustible = combustible; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCilindrada() { return cilindrada; }
    public void setCilindrada(String cilindrada) { this.cilindrada = cilindrada; }

    public String getPotencia() { return potencia; }
    public void setPotencia(String potencia) { this.potencia = potencia; }

    public String getTorque() { return torque; }
    public void setTorque(String torque) { this.torque = torque; }

    public String getTraccion() { return traccion; }
    public void setTraccion(String traccion) { this.traccion = traccion; }

    public String getRendimiento() { return rendimiento; }
    public void setRendimiento(String rendimiento) { this.rendimiento = rendimiento; }

    public String getPasajeros() { return pasajeros; }
    public void setPasajeros(String pasajeros) { this.pasajeros = pasajeros; }

    public String getCapacidadBaul() { return capacidadBaul; }
    public void setCapacidadBaul(String capacidadBaul) { this.capacidadBaul = capacidadBaul; }

    public String getLargo() { return largo; }
    public void setLargo(String largo) { this.largo = largo; }

    public String getAncho() { return ancho; }
    public void setAncho(String ancho) { this.ancho = ancho; }

    public String getAlto() { return alto; }
    public void setAlto(String alto) { this.alto = alto; }

    public String getPeso() { return peso; }
    public void setPeso(String peso) { this.peso = peso; }

    public String getSeguridad() { return seguridad; }
    public void setSeguridad(String seguridad) { this.seguridad = seguridad; }

    public String getEquipamiento() { return equipamiento; }
    public void setEquipamiento(String equipamiento) { this.equipamiento = equipamiento; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }


    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Modelo getModelo() { return modelo; }
    public void setModelo(Modelo modelo) { this.modelo = modelo; }

    public Concesionaria getConcesionaria() { return concesionaria; }
    public void setConcesionaria(Concesionaria concesionaria) { this.concesionaria = concesionaria; }

    public List<SolicitudAtencion> getSolicitudes() { return solicitudes; }
    public void setSolicitudes(List<SolicitudAtencion> solicitudes) { this.solicitudes = solicitudes; }
}
