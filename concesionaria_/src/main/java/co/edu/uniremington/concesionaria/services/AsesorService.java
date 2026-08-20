package co.edu.uniremington.concesionaria.services;

import co.edu.uniremington.concesionaria.models.Asesor;

import java.util.List;

public interface AsesorService {

List<Asesor> listarTodos();
Asesor buscarPorId(Long id);
Asesor crear(Asesor asesor);
Asesor actualizar(Long id, Asesor asesor);
void cambiarEstado(Long id, String estado);
List<Asesor> listarPorConcesionaria(Long idConcesionaria);
List<Asesor> listarActivos();

}
