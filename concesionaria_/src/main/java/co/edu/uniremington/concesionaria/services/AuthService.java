package co.edu.uniremington.concesionaria.services;

import co.edu.uniremington.concesionaria.dto.AuthResponse;
import co.edu.uniremington.concesionaria.dto.LoginRequest;
import co.edu.uniremington.concesionaria.dto.RegistroClienteRequest;

public interface AuthService {
    AuthResponse registrarCliente(RegistroClienteRequest request);
    AuthResponse iniciarSesion(LoginRequest request);
}
