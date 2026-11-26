package aula.tareas.repository;

import aula.tareas.modelo.Tarea;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CRUD compartido por los formatos de fichero, con lectura del fichero en cada
 * operación.
 */
abstract class RepositorioTareaFichero implements ITareaRepository {
    abstract List<Tarea> readAll();

    abstract void writeAll(List<Tarea> tareas);

    @Override
    public synchronized List<Tarea> findAll() {
        return readAll();
    }

    @Override
    public synchronized Optional<Tarea> findById(long id) {
        List<Tarea> tareas = new ArrayList<>(readAll());

        for (Tarea tarea : tareas) {
            if (tarea.identificador().equals(id)) {
                return Optional.of(tarea);
            }
        }
        return Optional.empty();
    }

    @Override
    public synchronized Tarea create(Tarea tarea) {
        List<Tarea> tareas = new ArrayList<>(readAll());
        long maxId = 0;

        for (Tarea task : tareas) {
            if (task.identificador() > maxId) {
                maxId = task.identificador();
            }
        }
        long nuevoId = maxId + 1;
        Tarea tareaCreada = new Tarea(nuevoId, tarea.nombre());
        tareas.add(tareaCreada);
        writeAll(tareas);
        return tareaCreada;
    }

    @Override
    public synchronized Optional<Tarea> update(Tarea tarea) {
        List<Tarea> tareas = new ArrayList<>(readAll());

        for (int i = 0; i < tareas.size(); i++) {
            if (tareas.get(i).identificador().equals(tarea.identificador())) {
                tareas.set(i, tarea);
                writeAll(tareas);
                return Optional.of(tarea);
            }
        }
        return Optional.empty();
    }

    @Override
    public synchronized boolean delete(long id) {
        List<Tarea> tareas = new ArrayList<>(readAll());

        for (Tarea tarea : tareas) {
            if (tarea.identificador().equals(id)) {
                tareas.remove(tarea);
                writeAll(tareas);
                return true;
            }
        }
        return false;
    }
}
