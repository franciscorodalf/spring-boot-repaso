package aula.tareas.service;

import aula.tareas.modelo.Tarea;
import aula.tareas.repository.ITareaRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service("tareaServiceJson")
public class TareaServiceJson implements ITareaService {

    private final ITareaRepository tareaRepositoryJson;

    public TareaServiceJson(@Qualifier("repositoryJson") ITareaRepository tareaRepositoryJson) {
        this.tareaRepositoryJson = tareaRepositoryJson;
    }

    @Override
    public List<Tarea> listar() {
        return tareaRepositoryJson.findAll();
    }

    @Override
    public Tarea buscar(long id) {
        if (id <= 0) {
            return null;
        }
        return tareaRepositoryJson.findById(id);
    }

    @Override
    public List<Tarea> buscarPorTitulo(String texto) {
        if (texto == null || texto.isBlank()) {
            return listar();
        }
        String buscado = texto.strip().toLowerCase(Locale.ROOT);
        return tareaRepositoryJson.findAll().stream()
                .filter(tarea -> tarea.titulo().toLowerCase(Locale.ROOT).contains(buscado))
                .toList();
    }

    @Override
    public Tarea crear(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return null;
        }
        return tareaRepositoryJson.create(titulo.strip());
    }

    @Override
    public boolean delete(long identificador) {
        if (identificador <= 0) {
            return false;
        }
        return tareaRepositoryJson.delete(identificador);
    }

    @Override
    public Tarea update(Tarea tarea) {
        if (tarea == null || tarea.id() <= 0 || tarea.titulo() == null || tarea.titulo().isBlank()) {
            return null;
        }
        return tareaRepositoryJson.update(new Tarea(tarea.id(), tarea.titulo().strip()));
    }
}
