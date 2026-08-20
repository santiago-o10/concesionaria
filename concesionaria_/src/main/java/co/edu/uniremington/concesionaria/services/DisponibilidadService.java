package co.edu.uniremington.concesionaria.services;

import co.edu.uniremington.concesionaria.models.Disponibilidad;

import java.util.List;

public interface DisponibilidadService {

List<Disponibilidad> listarTodos();
Disponibilidad buscarPorId(Long id);
Disponibilidad crear(Disponibilidad disponibilidad);
Disponibilidad actualizar(Long id, Disponibilidad disponibilidad);
void eliminar(Long id);
List<Disponibilidad> listarPorAsesor(Long idAsesor);
List<Disponibilidad> listarPorFecha(java.time.LocalDate fecha);

}
