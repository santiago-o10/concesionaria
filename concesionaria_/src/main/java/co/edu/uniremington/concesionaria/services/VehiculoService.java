package co.edu.uniremington.concesionaria.services;

import co.edu.uniremington.concesionaria.models.Vehiculo;

import java.util.List;

public interface VehiculoService {

List<Vehiculo> listarTodos();
Vehiculo buscarPorId(Long id);
Vehiculo crear(Vehiculo vehiculo);
Vehiculo actualizar(Long id, Vehiculo vehiculo);
void eliminar(Long id);
List<Vehiculo> listarPorEstado(String estado);
List<Vehiculo> listarPorModelo(Long idModelo);

}
