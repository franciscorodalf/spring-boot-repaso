package aula.tareas.repository;

import aula.tareas.modelo.Tarea;

import java.util.List;

/**
 * Operaciones de almacenamiento de tareas. Cada implementación guarda los
 * datos en un sitio distinto (memoria, fichero CSV, fichero JSON...).
 */
public interface ITareaRepository {
    /**
     * Guarda una tarea nueva asignándole el siguiente identificador libre.
     * @param titulo título de la tarea
     * @return la tarea guardada, con su identificador
     */
    public Tarea create(String titulo);

    /**
     * Borra la tarea con ese identificador.
     * @param identificador identificador de la tarea
     * @return {@code true} si existía y se ha borrado, {@code false} si no existía
     */
    public boolean delete(long identificador);

    /**
     * @return todas las tareas ordenadas por identificador
     */
    public List<Tarea> findAll();

    /**
     * @param identificador identificador de la tarea
     * @return la tarea, o {@code null} si no existe
     */
    public Tarea findById(long identificador);

    /**
     * Sustituye la tarea que tenga el mismo identificador.
     * @param tarea tarea con el identificador y los datos nuevos
     * @return la tarea actualizada, o {@code null} si no existe
     */
    public Tarea update(Tarea tarea);
}
