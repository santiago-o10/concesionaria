package co.edu.uniremington.concesionaria.services;

import co.edu.uniremington.concesionaria.models.SolicitudFinanciacion;
import java.util.*;

public interface SolicitudFinanciacionService {
    List<SolicitudFinanciacion> listarTodos();
    List<SolicitudFinanciacion> listarPorAsesor(Long id);
    List<SolicitudFinanciacion> listarPorCliente(Long id);
    SolicitudFinanciacion crear(SolicitudFinanciacion solicitud);
    SolicitudFinanciacion actualizarEstado(Long id, String estado, String observaciones);
}
