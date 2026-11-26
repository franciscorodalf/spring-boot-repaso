package aula.tareas.mapper;

import aula.tareas.entity.TareaEntityJson;
import aula.tareas.modelo.Tarea;

public final class TareaMapperJson {
    private TareaMapperJson() {
    }

    public static TareaEntityJson toEntity(Tarea tarea) {
        if (tarea == null) {
            return null;
        }
        TareaEntityJson tareaEntityjson = new TareaEntityJson();
        tareaEntityjson.setIdTarea(tarea.identificador());
        tareaEntityjson.setNombreTarea(tarea.nombre());
        return tareaEntityjson;
    }

    public static Tarea toDomain(TareaEntityJson entity) {
        if (entity == null) {
            return null;
        }
        return new Tarea(entity.getIdTarea(), entity.getNombreTarea());
    }
}