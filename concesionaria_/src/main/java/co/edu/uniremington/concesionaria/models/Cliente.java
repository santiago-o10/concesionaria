package co.edu.uniremington.concesionaria.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @SequenceGenerator(name = "cliente_seq", sequenceName = "cliente_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cliente_seq")
    private Long idCliente;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
    @Column(nullable = false, length = 80)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 80, message = "El apellido debe tener entre 2 y 80 caracteres")
    @Column(nullable = false, length = 80)
    private String apellido;

    @NotBlank(message = "El documento es obligatorio")
    @Size(min = 5, max = 30, message = "El documento debe tener entre 5 y 30 caracteres")
    @Column(nullable = false, unique = true, length = 30)
    private String documento;

    @NotBlank(message = "El teléfono es obligatorio")
    @Column(nullable = false, length = 30)
    private String telefono;

    @Email(message = "El correo no tiene un formato válido")
    @Column(length = 120)
    private String correo;

    @Size(max = 80, message = "La ciudad no puede superar 80 caracteres")
    @Column(length = 80)
    private String ciudad;

    @Column(nullable = false, length = 20)
    private String estado = "ACTIVO";

    @Column(name = "usuario", unique = true, length = 50)
    private String usuario;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "contrasena", length = 100)
    private String contrasena;

    @JsonIgnore
    @OneToMany(mappedBy = "cliente")
    private List<SolicitudAtencion> solicitudes = new ArrayList<>();

    public Cliente() {
    }

    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public List<SolicitudAtencion> getSolicitudes() { return solicitudes; }
    public void setSolicitudes(List<SolicitudAtencion> solicitudes) { this.solicitudes = solicitudes; }
}
