package aula.tareas.repository;

import aula.tareas.modelo.Tarea;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Repositorio que guarda las tareas en un fichero JSON con forma
 * {@code [{"id":1,"titulo":"..."}]}. Cada operación lee el fichero completo y,
 * si modifica datos, lo vuelve a escribir.
 */
@Repository("repositoryJson")
public class TareaRepositoryJson implements ITareaRepository {
    private static final TypeReference<List<Tarea>> LISTA_TAREAS = new TypeReference<>() {};

    private final Path path;
    private final JsonMapper mapper;

    /**
     * Spring inyecta el {@link JsonMapper} (Jackson 3) que configura Spring Boot
     * y la ruta del fichero desde {@code application.properties}.
     */
    public TareaRepositoryJson(JsonMapper mapper,
                               @Value("${tareas.json.ruta:data/tarea.json}") String ruta) {
        this.mapper = mapper;
        this.path = Path.of(ruta);
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se puede crear el fichero " + path, e);
        }
    }

    @Override
    public synchronized Tarea create(String titulo) {
        List<Tarea> items = leer();
        long id = items.stream().mapToLong(Tarea::id).max().orElse(0L) + 1;

        Tarea tarea = new Tarea(id, titulo.strip());
        items.add(tarea);
        guardar(items);
        return tarea;
    }

    @Override
    public synchronized boolean delete(long identificador) {
        List<Tarea> items = leer();
        boolean borrada = items.removeIf(tarea -> tarea.id() == identificador);
        if (borrada) {
            guardar(items);
        }
        return borrada;
    }

    @Override
    public synchronized List<Tarea> findAll() {
        return List.copyOf(leer());
    }

    @Override
    public synchronized Tarea findById(long identificador) {
        return leer().stream()
                .filter(tarea -> tarea.id() == identificador)
                .findFirst()
                .orElse(null);
    }

    @Override
    public synchronized Tarea update(Tarea tarea) {
        List<Tarea> items = leer();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id() == tarea.id()) {
                items.set(i, tarea);
                guardar(items);
                return tarea;
            }
        }
        return null;
    }

    /** Lee el fichero JSON. Un fichero vacío equivale a una lista vacía. */
    private List<Tarea> leer() {
        try {
            if (Files.size(path) == 0) {
                return new ArrayList<>();
            }
            List<Tarea> resultado = new ArrayList<>(mapper.readValue(path.toFile(), LISTA_TAREAS));
            resultado.sort(Comparator.comparingLong(Tarea::id));
            return resultado;
        } catch (IOException e) {
            throw new UncheckedIOException("No se puede leer " + path, e);
        } catch (JacksonException e) {
            throw new IllegalStateException("El fichero " + path + " no contiene un JSON válido", e);
        }
    }

    /** Reescribe el fichero completo con todas las tareas. */
    private void guardar(List<Tarea> items) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), items);
        } catch (JacksonException e) {
            throw new IllegalStateException("No se puede escribir " + path, e);
        }
    }
}
