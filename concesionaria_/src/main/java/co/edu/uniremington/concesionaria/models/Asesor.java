package co.edu.uniremington.concesionaria.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "asesores")
public class Asesor {

    @Id
    @SequenceGenerator(name = "asesor_seq", sequenceName = "asesor_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "asesor_seq")
    private Long idAsesor;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
    @Column(nullable = false, length = 80)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 80, message = "El apellido debe tener entre 2 y 80 caracteres")
    @Column(nullable = false, length = 80)
    private String apellido;

    @NotBlank(message = "El teléfono es obligatorio")
    @Column(nullable = false, length = 30)
    private String telefono;

    @Email(message = "El correo no tiene un formato válido")
    @Column(length = 120)
    private String correo;

    @Size(max = 100, message = "La especialidad no puede superar 100 caracteres")
    @Column(length = 100)
    private String especialidad;

    @NotBlank(message = "El estado es obligatorio")
    @Column(nullable = false, length = 30)
    private String estado = "ACTIVO";

    @Column(name = "hora_inicio_trabajo")
    private LocalTime horaInicioTrabajo;

    @Column(name = "hora_fin_trabajo")
    private LocalTime horaFinTrabajo;

    @Column(name = "usuario", unique = true, length = 50)
    private String usuario;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "contrasena", length = 100)
    private String contrasena;

    @NotNull(message = "La concesionaria es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "concesionaria_id", nullable = false)
    private Concesionaria concesionaria;

    @JsonIgnore
    @OneToMany(mappedBy = "asesor")
    private List<Disponibilidad> disponibilidades = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "asesor")
    private List<SolicitudAtencion> solicitudes = new ArrayList<>();

    public Asesor() {
    }

    public Long getIdAsesor() { return idAsesor; }
    public void setIdAsesor(Long idAsesor) { this.idAsesor = idAsesor; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalTime getHoraInicioTrabajo() { return horaInicioTrabajo; }
    public void setHoraInicioTrabajo(LocalTime horaInicioTrabajo) { this.horaInicioTrabajo = horaInicioTrabajo; }

    public LocalTime getHoraFinTrabajo() { return horaFinTrabajo; }
    public void setHoraFinTrabajo(LocalTime horaFinTrabajo) { this.horaFinTrabajo = horaFinTrabajo; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public Concesionaria getConcesionaria() { return concesionaria; }
    public void setConcesionaria(Concesionaria concesionaria) { this.concesionaria = concesionaria; }

    public List<Disponibilidad> getDisponibilidades() { return disponibilidades; }
    public void setDisponibilidades(List<Disponibilidad> disponibilidades) { this.disponibilidades = disponibilidades; }

    public List<SolicitudAtencion> getSolicitudes() { return solicitudes; }
    public void setSolicitudes(List<SolicitudAtencion> solicitudes) { this.solicitudes = solicitudes; }
}
