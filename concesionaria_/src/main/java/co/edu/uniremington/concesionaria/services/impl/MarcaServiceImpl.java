package co.edu.uniremington.concesionaria.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.uniremington.concesionaria.exceptions.RecursoDuplicadoException;
import co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException;
import co.edu.uniremington.concesionaria.models.Marca;
import co.edu.uniremington.concesionaria.repositorys.MarcaRepository;
import co.edu.uniremington.concesionaria.services.MarcaService;

@Service
public class MarcaServiceImpl implements MarcaService {

    private final MarcaRepository repository;

    public MarcaServiceImpl(MarcaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Marca> listarTodos() {
        return repository.findAll();
    }

    @Override
    public Marca buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una marca con id " + id));
    }

    @Override
    public Marca crear(Marca entidad) {

        if (repository.countByNombreIgnoreCase(entidad.getNombre()) > 0) {
            throw new RecursoDuplicadoException(
                    "Ya existe una marca con el nombre " + entidad.getNombre()
            );
        }

        return repository.save(entidad);
    }

    @Override
    public Marca actualizar(Long id, Marca entidad) {
        Marca actual = buscarPorId(id);
        actual.setNombre(entidad.getNombre());

        return repository.save(actual);
    }

    @Override
    public void eliminar(Long id) {
        Marca actual = buscarPorId(id);
        repository.delete(actual);
    }
}
