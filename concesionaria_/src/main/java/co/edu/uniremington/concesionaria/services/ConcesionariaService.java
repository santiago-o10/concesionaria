package co.edu.uniremington.concesionaria.services;

import co.edu.uniremington.concesionaria.models.Concesionaria;

import java.util.List;

public interface ConcesionariaService {

List<Concesionaria> listarTodos();
Concesionaria buscarPorId(Long id);
Concesionaria crear(Concesionaria concesionaria);
Concesionaria actualizar(Long id, Concesionaria concesionaria);
void eliminar(Long id);

}
