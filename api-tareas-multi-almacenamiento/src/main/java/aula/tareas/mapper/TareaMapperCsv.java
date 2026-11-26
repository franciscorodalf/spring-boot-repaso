package aula.tareas.mapper;

import aula.tareas.entity.TareaEntityCsv;
import aula.tareas.modelo.Tarea;

public final class TareaMapperCsv {
    private TareaMapperCsv() {
    }

    public static TareaEntityCsv toEntity(Tarea tarea) {
        if (tarea == null) {
            return null;
        }
        TareaEntityCsv tareaEntityCsv = new TareaEntityCsv();
        tareaEntityCsv.setIdTarea(tarea.identificador());
        tareaEntityCsv.setNombreTarea(tarea.nombre());
        return tareaEntityCsv;
    }

    public static Tarea toDomain(TareaEntityCsv entity) {
        if (entity == null) {
            return null;
        }

        return new Tarea(entity.getIdTarea(), entity.getNombreTarea());
    }

}
