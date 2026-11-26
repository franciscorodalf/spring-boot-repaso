package aula.tareas.mapper;

import aula.tareas.entity.TareaEntityXml;
import aula.tareas.modelo.Tarea;

public final class TareaMapperXml {
    private TareaMapperXml() {
    }

    public static TareaEntityXml toEntity(Tarea tarea) {
        if (tarea == null) {
            return null;
        }
        TareaEntityXml tareaEntityXml = new TareaEntityXml();
        tareaEntityXml.setIdTarea(tarea.identificador());
        tareaEntityXml.setNombreTarea(tarea.nombre());
        return tareaEntityXml;
    }

    public static Tarea toDomain(TareaEntityXml entity) {
        if (entity == null) {
            return null;
        }

        return new Tarea(entity.getIdTarea(), entity.getNombreTarea());
    }
}