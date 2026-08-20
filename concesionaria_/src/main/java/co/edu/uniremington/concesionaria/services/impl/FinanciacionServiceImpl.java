package co.edu.uniremington.concesionaria.services.impl;

import co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException;
import co.edu.uniremington.concesionaria.exceptions.RecursoDuplicadoException;
import co.edu.uniremington.concesionaria.models.Financiacion;
import co.edu.uniremington.concesionaria.repositorys.FinanciacionRepository;
import co.edu.uniremington.concesionaria.services.FinanciacionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinanciacionServiceImpl implements FinanciacionService {

    private final FinanciacionRepository repository;

    public FinanciacionServiceImpl(FinanciacionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Financiacion> listarTodos() {
        return repository.findAll();
    }

    @Override
    public Financiacion buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un financiacion con id " + id));
    }

    @Override
    public Financiacion crear(Financiacion entidad) {

        return repository.save(entidad);
    }

    @Override
    public Financiacion actualizar(Long id, Financiacion entidad) {
        Financiacion actual = buscarPorId(id);
        actual.setNombre(entidad.getNombre());
        actual.setDescripcion(entidad.getDescripcion());
        actual.setPorcentajeInicial(entidad.getPorcentajeInicial());
        actual.setPlazo(entidad.getPlazo());
        actual.setCondiciones(entidad.getCondiciones());
        actual.setConcesionaria(entidad.getConcesionaria());
        return repository.save(actual);
    }

    @Override
    public List<Financiacion> listarPorConcesionaria(Long idConcesionaria) {
        return repository.findByConcesionariaIdConcesionaria(idConcesionaria);
    }

    @Override
    public void eliminar(Long id) {
        Financiacion actual = buscarPorId(id);
        repository.delete(actual);
    }
}
