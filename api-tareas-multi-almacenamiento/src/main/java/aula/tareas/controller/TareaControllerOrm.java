package aula.tareas.controller;

import aula.tareas.api.TareaRequest;
import aula.tareas.api.TareaResponse;
import aula.tareas.mapper.TareaMapperApi;
import aula.tareas.modelo.Tarea;
import aula.tareas.service.ITareaService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orm/tareas")
public class TareaControllerOrm {
    private final ITareaService servicio;

    public TareaControllerOrm(@Qualifier("tareaServiceOrm") ITareaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<TareaResponse> listar() {
        return servicio.listar().stream().map(TareaMapperApi::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TareaResponse> buscar(long id) {
        return ResponseEntity.of(servicio.buscar(id).map(TareaMapperApi::toResponse));
    }

    @PostMapping()
    public ResponseEntity<TareaResponse> crear(TareaRequest request) {
        Tarea tareaCreada = servicio.crear(request);
        URI uri = URI.create("/api/orm/tareas/" + tareaCreada.identificador());
        return ResponseEntity.created(uri).body(TareaMapperApi.toResponse(tareaCreada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TareaResponse> actualizar(long id, TareaRequest request) {
        return ResponseEntity.of(servicio.actualizar(id, request).map(TareaMapperApi::toResponse));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(long id) {
        boolean borrado = servicio.eliminar(id);
        if (borrado) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
