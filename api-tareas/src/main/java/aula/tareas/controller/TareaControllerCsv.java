package aula.tareas.controller;

import java.net.URI;
import java.util.List;

import aula.tareas.modelo.Tarea;
import aula.tareas.service.ITareaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(TareaControllerCsv.RUTA)
@Tag(name = "Tareas CSV", description = "Operaciones CRUD de tareas guardadas en data/tarea.csv")
public class TareaControllerCsv {
    static final String RUTA = "/api/csv/tareas";

    private final ITareaService servicio;

    public TareaControllerCsv(@Qualifier("tareaServiceCsv") ITareaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Listar las tareas; con ?titulo= filtra las que contienen ese texto")
    @ApiResponse(responseCode = "200", description = "Listado de tareas")
    List<Tarea> listar(@RequestParam(required = false) String titulo) {
        return titulo == null ? servicio.listar() : servicio.buscarPorTitulo(titulo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una tarea por su id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarea encontrada"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    ResponseEntity<Tarea> buscar(@PathVariable long id) {
        return ResponseEntity.ofNullable(servicio.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Crear una tarea")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarea creada"),
            @ApiResponse(responseCode = "400", description = "Título vacío o ausente")
    })
    ResponseEntity<Tarea> crear(@Valid @RequestBody PeticionTarea peticion) {
        Tarea tarea = servicio.crear(peticion.titulo());
        if (tarea == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.created(URI.create(RUTA + "/" + tarea.id())).body(tarea);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cambiar el título de una tarea")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarea actualizada"),
            @ApiResponse(responseCode = "400", description = "Título vacío o ausente"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    ResponseEntity<Tarea> actualizar(@PathVariable long id, @Valid @RequestBody PeticionTarea peticion) {
        return ResponseEntity.ofNullable(servicio.update(new Tarea(id, peticion.titulo())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Borrar una tarea")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tarea borrada"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    ResponseEntity<Void> borrar(@PathVariable long id) {
        return servicio.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
