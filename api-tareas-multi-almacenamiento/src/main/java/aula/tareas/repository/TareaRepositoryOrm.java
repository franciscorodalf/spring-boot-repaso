package aula.tareas.repository;

import aula.tareas.entity.TareaEntityJson;
import aula.tareas.entity.TareaEntityOrm;
import aula.tareas.mapper.TareaMapperJson;
import aula.tareas.mapper.TareaMapperOrm;
import aula.tareas.modelo.Tarea;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("tareaRepositoryOrm")
public class TareaRepositoryOrm implements ITareaRepository {
    private final ITareaRepositoryJpa jpa;

    public TareaRepositoryOrm(ITareaRepositoryJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Tarea> findAll() {

        List<TareaEntityOrm> resultado = jpa.findAll();
        List<Tarea> tareas = new ArrayList<>();
        for (TareaEntityOrm tarea : resultado) {
            tareas.add(TareaMapperOrm.toDomain(tarea));
        }
        return tareas;
    }

    @Override
    public Optional<Tarea> findById(long id) {
        return jpa.findById(id).map(TareaMapperOrm::toDomain);
    }

    @Override
    public Tarea create(Tarea tarea) {
      TareaEntityOrm tareaEntity = new TareaEntityOrm();
      return tarea;
    }

    @Override
    public Optional<Tarea> update(Tarea tarea) {
        // TODO: Actualizar una tarea existente.
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public boolean delete(long id) {
        // TODO: Eliminar la tarea y comunicar si existía.
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}
