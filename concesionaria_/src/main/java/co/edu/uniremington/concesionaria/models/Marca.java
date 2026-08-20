package co.edu.uniremington.concesionaria.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "marcas")
public class Marca {

    @Id
    @SequenceGenerator(name = "marca_seq", sequenceName = "marca_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "marca_seq")
    private Long idMarca;

    @NotBlank(message = "El nombre de la marca es obligatorio")
    @Size(min = 2, max = 80, message = "El nombre de la marca debe tener entre 2 y 80 caracteres")
    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @JsonIgnore
    @OneToMany(mappedBy = "marca")
    private List<Modelo> modelos = new ArrayList<>();

    public Marca() {
    }

    public Long getIdMarca() { return idMarca; }
    public void setIdMarca(Long idMarca) { this.idMarca = idMarca; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<Modelo> getModelos() { return modelos; }
    public void setModelos(List<Modelo> modelos) { this.modelos = modelos; }
}
