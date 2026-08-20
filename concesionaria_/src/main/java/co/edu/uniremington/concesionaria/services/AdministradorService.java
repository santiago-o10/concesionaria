package co.edu.uniremington.concesionaria.services;

import co.edu.uniremington.concesionaria.models.Administrador;

import java.util.List;

public interface AdministradorService {

List<Administrador> listarTodos();
Administrador buscarPorId(Long id);
Administrador crear(Administrador administrador);
Administrador actualizar(Long id, Administrador administrador);
void eliminar(Long id);
Administrador iniciarSesion(String usuario, String contrasena);

}
