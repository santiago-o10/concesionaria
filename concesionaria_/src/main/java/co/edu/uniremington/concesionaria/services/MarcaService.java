package co.edu.uniremington.concesionaria.services;

import co.edu.uniremington.concesionaria.models.Marca;

import java.util.List;

public interface MarcaService {

List<Marca> listarTodos();
Marca buscarPorId(Long id);
Marca crear(Marca marca);
Marca actualizar(Long id, Marca marca);
void eliminar(Long id);

}
