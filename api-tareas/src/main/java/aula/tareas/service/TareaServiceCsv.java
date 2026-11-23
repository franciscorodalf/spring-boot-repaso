package aula.tareas.service;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import aula.tareas.modelo.Tarea;
import aula.tareas.repository.ITareaRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("tareaServiceCsv")
public class TareaServiceCsv implements ITareaService {
    private final ConcurrentSkipListMap<Long, Tarea> tareas = new ConcurrentSkipListMap<>();
    private final ITareaRepository repository;

    public TareaServiceCsv(@Qualifier("repositoryCsv") ITareaRepository repository) {
        this.repository = repository;
    }

    public List<Tarea> listar() {
        return repository.findAll();
    }

    public Tarea buscar(long id) {
        if (id < 0) {
            return null;
        }
        return repository.findById(id);
    }

    public Tarea crear(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return null;
        }
        return repository.create(titulo);
    }

    @Override
    public boolean delete(long identificador) {
       if (identificador < 0) {
        return false;
    }
    return repository.delete(identificador);
}

    @Override
    public Tarea update(Tarea tarea) {
        if (tarea == null || tarea.id() < 0 || tarea.titulo() == null || tarea.titulo().isBlank()) {
        return null;
    }
    return repository.update(tarea);
}
}
