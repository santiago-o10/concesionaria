package co.edu.uniremington.concesionaria.services.impl;

import co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException;
import co.edu.uniremington.concesionaria.exceptions.RecursoDuplicadoException;
import co.edu.uniremington.concesionaria.models.Disponibilidad;
import co.edu.uniremington.concesionaria.repositorys.DisponibilidadRepository;
import co.edu.uniremington.concesionaria.services.DisponibilidadService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DisponibilidadServiceImpl implements DisponibilidadService {

    private final DisponibilidadRepository repository;

    public DisponibilidadServiceImpl(DisponibilidadRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Disponibilidad> listarTodos() {
        return repository.findAll();
    }

    @Override
    public Disponibilidad buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un disponibilidad con id " + id));
    }

    @Override
    public Disponibilidad crear(Disponibilidad entidad) {

        return repository.save(entidad);
    }

    @Override
    public Disponibilidad actualizar(Long id, Disponibilidad entidad) {
        Disponibilidad actual = buscarPorId(id);
        if (!entidad.getHoraFin().isAfter(entidad.getHoraInicio())) {
            throw new co.edu.uniremington.concesionaria.exceptions.ReglaNegocioException(
                    "La hora de fin debe ser posterior a la hora de inicio");
        }
        actual.setFecha(entidad.getFecha());
        actual.setHoraInicio(entidad.getHoraInicio());
        actual.setHoraFin(entidad.getHoraFin());
        actual.setEstado(entidad.getEstado());
        actual.setAsesor(entidad.getAsesor());
        return repository.save(actual);
    }

    @Override
    public List<Disponibilidad> listarPorAsesor(Long idAsesor) {
        return repository.findByAsesorIdAsesor(idAsesor);
    }

    @Override
    public List<Disponibilidad> listarPorFecha(java.time.LocalDate fecha) {
        return repository.findByFechaAndEstadoIgnoreCase(fecha, "DISPONIBLE");
    }

    @Override
    public void eliminar(Long id) {
        Disponibilidad actual = buscarPorId(id);
        repository.delete(actual);
    }
}
