package aula.tareas.mapper;

import aula.tareas.entity.TareaEntityOrm;
import aula.tareas.modelo.Tarea;

public final class TareaMapperOrm {
    private TareaMapperOrm() {
    }

    public static TareaEntityOrm toEntity(Tarea tarea) {
        if (tarea == null) {
            return null;
        }
        TareaEntityOrm tareaEntityorm = new TareaEntityOrm();
        tareaEntityorm.setIdTarea(tarea.identificador());
        tareaEntityorm.setNombreTarea(tarea.nombre());
        return tareaEntityorm;
    }

    public static Tarea toDomain(TareaEntityOrm entity) {
        if (entity == null) {
            return null;
        }
        return new Tarea(entity.getIdTarea(), entity.getNombreTarea());
    }
}