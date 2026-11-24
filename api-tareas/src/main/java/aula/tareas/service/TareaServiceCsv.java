package aula.tareas.service;

import java.util.List;
import java.util.Locale;

import aula.tareas.modelo.Tarea;
import aula.tareas.repository.ITareaRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("tareaServiceCsv")
public class TareaServiceCsv implements ITareaService {
    private final ITareaRepository repository;

    public TareaServiceCsv(@Qualifier("repositoryCsv") ITareaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Tarea> listar() {
        return repository.findAll();
    }

    @Override
    public Tarea buscar(long id) {
        if (id <= 0) {
            return null;
        }
        return repository.findById(id);
    }

    @Override
    public List<Tarea> buscarPorTitulo(String texto) {
        if (texto == null || texto.isBlank()) {
            return listar();
        }
        String buscado = texto.strip().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(tarea -> tarea.titulo().toLowerCase(Locale.ROOT).contains(buscado))
                .toList();
    }

    @Override
    public Tarea crear(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return null;
        }
        return repository.create(titulo.strip());
    }

    @Override
    public boolean delete(long identificador) {
        if (identificador <= 0) {
            return false;
        }
        return repository.delete(identificador);
    }

    @Override
    public Tarea update(Tarea tarea) {
        if (tarea == null || tarea.id() <= 0 || tarea.titulo() == null || tarea.titulo().isBlank()) {
            return null;
        }
        return repository.update(new Tarea(tarea.id(), tarea.titulo().strip()));
    }
}
