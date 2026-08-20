package co.edu.uniremington.concesionaria.services;

import co.edu.uniremington.concesionaria.models.Financiacion;

import java.util.List;

public interface FinanciacionService {

List<Financiacion> listarTodos();
Financiacion buscarPorId(Long id);
Financiacion crear(Financiacion financiacion);
Financiacion actualizar(Long id, Financiacion financiacion);
void eliminar(Long id);
List<Financiacion> listarPorConcesionaria(Long idConcesionaria);

}
