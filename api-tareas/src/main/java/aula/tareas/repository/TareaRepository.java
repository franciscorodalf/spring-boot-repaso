package aula.tareas.repository;

import aula.tareas.modelo.Tarea;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repositorio en memoria: los datos se pierden al reiniciar la aplicación.
 */
@Repository("repositoryMemoria")
public class TareaRepository implements ITareaRepository {
    private final AtomicLong secuencia = new AtomicLong();
    private final ConcurrentSkipListMap<Long, Tarea> tareas = new ConcurrentSkipListMap<>();

    @Override
    public Tarea create(String titulo) {
        long id = secuencia.incrementAndGet();
        var tarea = new Tarea(id, titulo.strip());
        tareas.put(id, tarea);
        return tarea;
    }

    @Override
    public boolean delete(long identificador) {
        return tareas.remove(identificador) != null;
    }

    @Override
    public List<Tarea> findAll() {
        return List.copyOf(tareas.values());
    }

    @Override
    public Tarea findById(long identificador) {
        return tareas.get(identificador);
    }

    @Override
    public Tarea update(Tarea tarea) {
        // replace solo sustituye si ya existe una tarea con ese id
        return tareas.replace(tarea.id(), tarea) != null ? tarea : null;
    }
}
