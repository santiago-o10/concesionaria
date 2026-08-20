package co.edu.uniremington.concesionaria.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "concesionarias")
public class Concesionaria {

    @Id
    @SequenceGenerator(name = "concesionaria_seq", sequenceName = "concesionaria_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "concesionaria_seq")
    private Long idConcesionaria;

    @NotBlank(message = "El nombre de la concesionaria es obligatorio")
    @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres")
    @Column(nullable = false, length = 120)
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 180, message = "La dirección no puede superar 180 caracteres")
    @Column(nullable = false, length = 180)
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
    @Column(nullable = false, length = 30)
    private String telefono;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 120, message = "El correo no puede superar 120 caracteres")
    @Column(length = 120)
    private String correo;

    @NotBlank(message = "El horario de atención es obligatorio")
    @Column(nullable = false, length = 150)
    private String horarioAtencion;

    @JsonIgnore
    @OneToMany(mappedBy = "concesionaria")
    private List<Vehiculo> vehiculos = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "concesionaria")
    private List<Asesor> asesores = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "concesionaria")
    private List<Financiacion> financiaciones = new ArrayList<>();

    public Concesionaria() {
    }

    public Long getIdConcesionaria() { return idConcesionaria; }
    public void setIdConcesionaria(Long idConcesionaria) { this.idConcesionaria = idConcesionaria; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getHorarioAtencion() { return horarioAtencion; }
    public void setHorarioAtencion(String horarioAtencion) { this.horarioAtencion = horarioAtencion; }

    public List<Vehiculo> getVehiculos() { return vehiculos; }
    public void setVehiculos(List<Vehiculo> vehiculos) { this.vehiculos = vehiculos; }

    public List<Asesor> getAsesores() { return asesores; }
    public void setAsesores(List<Asesor> asesores) { this.asesores = asesores; }

    public List<Financiacion> getFinanciaciones() { return financiaciones; }
    public void setFinanciaciones(List<Financiacion> financiaciones) { this.financiaciones = financiaciones; }
}
