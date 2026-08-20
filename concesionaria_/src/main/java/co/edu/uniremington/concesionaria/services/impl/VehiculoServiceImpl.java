package co.edu.uniremington.concesionaria.services.impl;

import co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException;
import co.edu.uniremington.concesionaria.exceptions.RecursoDuplicadoException;
import co.edu.uniremington.concesionaria.models.Vehiculo;
import co.edu.uniremington.concesionaria.repositorys.VehiculoRepository;
import co.edu.uniremington.concesionaria.services.VehiculoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository repository;

    public VehiculoServiceImpl(VehiculoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Vehiculo> listarTodos() {
        return repository.findAll();
    }

    @Override
    public Vehiculo buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un vehiculo con id " + id));
    }

    @Override
    public Vehiculo crear(Vehiculo entidad) {

        return repository.save(entidad);
    }

    @Override
    public Vehiculo actualizar(Long id, Vehiculo entidad) {
        Vehiculo actual = buscarPorId(id);
        actual.setAnio(entidad.getAnio());
        actual.setPrecio(entidad.getPrecio());
        actual.setTipo(entidad.getTipo());
        actual.setColor(entidad.getColor());
        actual.setMotor(entidad.getMotor());
        actual.setTransmision(entidad.getTransmision());
        actual.setCombustible(entidad.getCombustible());
        actual.setDescripcion(entidad.getDescripcion());
        actual.setEstado(entidad.getEstado());
        actual.setModelo(entidad.getModelo());
        actual.setConcesionaria(entidad.getConcesionaria());
        return repository.save(actual);
    }

    @Override
    public List<Vehiculo> listarPorEstado(String estado) {
        return repository.findByEstadoIgnoreCase(estado);
    }

    @Override
    public List<Vehiculo> listarPorModelo(Long idModelo) {
        return repository.findByModeloIdModelo(idModelo);
    }

    @Override
    public void eliminar(Long id) {
        Vehiculo actual = buscarPorId(id);
        repository.delete(actual);
    }
}
