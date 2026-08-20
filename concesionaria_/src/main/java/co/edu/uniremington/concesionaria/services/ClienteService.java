package co.edu.uniremington.concesionaria.services;

import co.edu.uniremington.concesionaria.models.Cliente;
import co.edu.uniremington.concesionaria.dto.ActualizarPerfilClienteRequest;

import java.util.List;

public interface ClienteService {

List<Cliente> listarTodos();
Cliente buscarPorId(Long id);
Cliente crear(Cliente cliente);
Cliente actualizar(Long id, Cliente cliente);
Cliente actualizarMiPerfil(Long id, ActualizarPerfilClienteRequest request);
void cambiarEstado(Long id, String estado);

}
