package co.edu.uniremington.concesionaria.services;

import co.edu.uniremington.concesionaria.models.SolicitudAtencion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SolicitudAtencionService {

    List<SolicitudAtencion> listarTodos();
    SolicitudAtencion buscarPorId(Long id);
    SolicitudAtencion crear(SolicitudAtencion solicitud);
    SolicitudAtencion actualizar(Long id, SolicitudAtencion solicitud);
    void eliminar(Long id);
    List<SolicitudAtencion> listarPorCliente(Long idCliente);
    List<SolicitudAtencion> listarPorAsesor(Long idAsesor);
    List<LocalTime> horariosDisponibles(LocalDate fecha);
    SolicitudAtencion cambiarEstado(Long id, String nuevoEstado);
    SolicitudAtencion registrarAtencionPresencial(Long idAsesor, SolicitudAtencion solicitud);

}
