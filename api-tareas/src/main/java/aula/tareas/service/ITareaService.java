package aula.tareas.service;

import aula.tareas.modelo.Tarea;

import java.util.List;

/**
 * Lógica de negocio de las tareas. Valida los datos de entrada y delega el
 * almacenamiento en un {@link aula.tareas.repository.ITareaRepository}.
 */
public interface ITareaService {
    /**
     * @return todas las tareas
     */
    public List<Tarea> listar();

    /**
     * @param id identificador de la tarea
     * @return la tarea, o {@code null} si no existe o el id no es válido
     */
    public Tarea buscar(long id);

    /**
     * Busca las tareas cuyo título contiene el texto indicado, sin distinguir
     * mayúsculas de minúsculas. Un texto vacío devuelve todas las tareas.
     * @param texto texto a buscar dentro del título
     * @return tareas que coinciden
     */
    public List<Tarea> buscarPorTitulo(String texto);

    /**
     * @param titulo título de la tarea nueva
     * @return la tarea creada, o {@code null} si el título está vacío
     */
    public Tarea crear(String titulo);

    /**
     * @param identificador identificador de la tarea
     * @return {@code true} si se ha borrado, {@code false} si no existía
     */
    public boolean delete(long identificador);

    /**
     * @param tarea tarea con el identificador a modificar y el título nuevo
     * @return la tarea actualizada, o {@code null} si no existe o los datos no son válidos
     */
    public Tarea update(Tarea tarea);
}
