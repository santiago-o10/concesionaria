package co.edu.uniremington.concesionaria.services;
import co.edu.uniremington.concesionaria.models.Venta;
import java.util.*;
public interface VentaService { Venta crear(Venta v); List<Venta> listarTodos(); List<Venta> listarPorAsesor(Long id); }