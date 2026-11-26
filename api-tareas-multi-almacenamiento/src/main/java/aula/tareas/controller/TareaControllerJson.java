package aula.tareas.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import aula.tareas.api.TareaRequest;
import aula.tareas.api.TareaResponse;
import aula.tareas.mapper.TareaMapperApi;
import aula.tareas.modelo.Tarea;
import aula.tareas.service.ITareaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/json/tareas")
public class TareaControllerJson {
    private final ITareaService servicio;

    public TareaControllerJson(@Qualifier("tareaServiceJson") ITareaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<TareaResponse> listar() {
        return servicio.listar().stream().map(TareaMapperApi::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TareaResponse> buscar(@PathVariable long id) {
        return ResponseEntity.of(servicio.buscar(id).map(TareaMapperApi::toResponse));
    }

    @PostMapping()
    public ResponseEntity<TareaResponse> crear(@Valid @RequestBody TareaRequest request) {
        Tarea tareaCreada = servicio.crear(request);
        URI uri = URI.create("/api/json/tareas/" + tareaCreada.identificador());
        return ResponseEntity.created(uri).body(TareaMapperApi.toResponse(tareaCreada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TareaResponse> actualizar(@PathVariable long id, @Valid @RequestBody TareaRequest request) {
        return ResponseEntity.of(servicio.actualizar(id, request).map(TareaMapperApi::toResponse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable long id) {
        boolean borrado = servicio.eliminar(id);
        if (borrado) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();

    }
}
