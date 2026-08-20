package co.edu.uniremington.concesionaria.services;
import co.edu.uniremington.concesionaria.models.Seguimiento;
import java.util.*;
public interface SeguimientoService { Seguimiento crear(Seguimiento s); List<Seguimiento> listarTodos(); List<Seguimiento> listarPorAsesor(Long id); List<Seguimiento> listarPorOportunidad(Long id); Seguimiento actualizar(Long id,Seguimiento s); }