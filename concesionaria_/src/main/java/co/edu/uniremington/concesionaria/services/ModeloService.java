package co.edu.uniremington.concesionaria.services;

import co.edu.uniremington.concesionaria.models.Modelo;

import java.util.List;

public interface ModeloService {

List<Modelo> listarTodos();
Modelo buscarPorId(Long id);
Modelo crear(Modelo modelo);
Modelo actualizar(Long id, Modelo modelo);
void eliminar(Long id);
List<Modelo> listarPorMarca(Long idMarca);

}
