package aula.tareas.repository;

import aula.tareas.entity.TareaEntityJson;
import aula.tareas.mapper.TareaMapperJson;
import aula.tareas.modelo.Tarea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("tareaRepositoryJson")
public class TareaRepositoryJson implements ITareaRepository {
    private final Path path;
    private final JsonMapper mapper;

    @Autowired
    public TareaRepositoryJson(JsonMapper mapper) {
        this(Path.of("data", "tarea.json"), mapper);
    }

    public TareaRepositoryJson(Path path, JsonMapper mapper) {
        this.path = path;
        this.mapper = mapper;
    }

    @Override
    public synchronized List<Tarea> findAll() {
        File fichero = path.toFile();
        if (!fichero.exists() || fichero.length() == 0) {
            return new ArrayList<>();
        }
        TareaEntityJson[] entidades = mapper.readValue(fichero, TareaEntityJson[].class);
        List<Tarea> resultado = new ArrayList<>();
        for (TareaEntityJson entity : entidades) {
            resultado.add(TareaMapperJson.toDomain(entity));
        }
        return resultado;
    }

    @Override
    public synchronized Optional<Tarea> findById(long id) {
        return findAll().stream()
                .filter(tarea -> tarea.identificador() == id)
                .findFirst();
    }

    @Override
    public synchronized Tarea create(Tarea tarea) {
        List<Tarea> items = findAll();
        long id = items.stream().mapToLong(Tarea::identificador).max().orElse(0L) + 1;
        Tarea tareaCrear = new Tarea(id, tarea.nombre().strip());
        items.add(tareaCrear);
        saveAll(items);
        return tareaCrear;
    }

    @Override
    public synchronized Optional<Tarea> update(Tarea tarea) {
        List<Tarea> items = findAll();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).identificador().equals(tarea.identificador())) {
                items.set(i, tarea);
                saveAll(items);
                return Optional.of(tarea);
            }
        }
        return Optional.empty();
    }

    @Override
    public synchronized boolean delete(long id) {
        List<Tarea> items = findAll();
        boolean borrada = items.removeIf(tarea -> tarea.identificador() == id);
        if (borrada) {
            saveAll(items);
        }
        return borrada;
    }

    private void saveAll(List<Tarea> tareas) {
        List<TareaEntityJson> entidades = new ArrayList<>();
        for (Tarea tarea : tareas) {
            entidades.add(TareaMapperJson.toEntity(tarea));
        }
        mapper.writeValue(path.toFile(), entidades);
    }
}
