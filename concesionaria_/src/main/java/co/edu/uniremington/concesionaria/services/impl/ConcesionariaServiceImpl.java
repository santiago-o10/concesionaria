package co.edu.uniremington.concesionaria.services.impl;

import co.edu.uniremington.concesionaria.exceptions.RecursoNoEncontradoException;
import co.edu.uniremington.concesionaria.exceptions.RecursoDuplicadoException;
import co.edu.uniremington.concesionaria.models.Concesionaria;
import co.edu.uniremington.concesionaria.repositorys.ConcesionariaRepository;
import co.edu.uniremington.concesionaria.services.ConcesionariaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConcesionariaServiceImpl implements ConcesionariaService {

    private final ConcesionariaRepository repository;

    public ConcesionariaServiceImpl(ConcesionariaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Concesionaria> listarTodos() {
        return repository.findAll();
    }

    @Override
    public Concesionaria buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un concesionaria con id " + id));
    }

    @Override
    public Concesionaria crear(Concesionaria entidad) {

        return repository.save(entidad);
    }

    @Override
    public Concesionaria actualizar(Long id, Concesionaria entidad) {
        Concesionaria actual = buscarPorId(id);
        actual.setNombre(entidad.getNombre());
        actual.setDireccion(entidad.getDireccion());
        actual.setTelefono(entidad.getTelefono());
        actual.setCorreo(entidad.getCorreo());
        actual.setHorarioAtencion(entidad.getHorarioAtencion());
        return repository.save(actual);
    }

    @Override
    public void eliminar(Long id) {
        Concesionaria actual = buscarPorId(id);
        repository.delete(actual);
    }
}
