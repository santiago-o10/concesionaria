package co.edu.uniremington.concesionaria.services.impl;

import co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException;
import co.edu.uniremington.concesionaria.exceptions.RecursoDuplicadoException;
import co.edu.uniremington.concesionaria.models.Modelo;
import co.edu.uniremington.concesionaria.repositorys.ModeloRepository;
import co.edu.uniremington.concesionaria.services.ModeloService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModeloServiceImpl implements ModeloService {

    private final ModeloRepository repository;

    public ModeloServiceImpl(ModeloRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Modelo> listarTodos() {
        return repository.findAll();
    }

    @Override
    public Modelo buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un modelo con id " + id));
    }

    @Override
    public Modelo crear(Modelo entidad) {

        if (entidad.getMarca() == null || entidad.getMarca().getIdMarca() == null) {
            throw new RecursoNoEncontradoException(
                    "Debe indicar una marca válida"
            );
        }

        if (repository.countByNombreIgnoreCaseAndMarcaIdMarca(
                entidad.getNombre(),
                entidad.getMarca().getIdMarca()) > 0) {

            throw new RecursoDuplicadoException(
                    "Ya existe ese modelo para la marca indicada"
            );
        }

        return repository.save(entidad);
    }

    @Override
    public Modelo actualizar(Long id, Modelo entidad) {
        Modelo actual = buscarPorId(id);
        actual.setNombre(entidad.getNombre());
        actual.setMarca(entidad.getMarca());

        return repository.save(actual);
    }

    @Override
    public List<Modelo> listarPorMarca(Long idMarca) {
        return repository.findByMarcaIdMarca(idMarca);
    }

    @Override
    public void eliminar(Long id) {
        Modelo actual = buscarPorId(id);
        repository.delete(actual);
    }
}