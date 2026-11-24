package ies.puesto.peliculas.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ies.puesto.peliculas.controller.api.PeliculaEdicionRequest;
import ies.puesto.peliculas.controller.api.PeliculaRequest;
import ies.puesto.peliculas.controller.api.PeliculaResponse;
import ies.puesto.peliculas.controller.mapper.PeliculaApiMapper;
import ies.puesto.peliculas.model.Pelicula;
import ies.puesto.peliculas.service.PeliculaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

@RestController
@Profile("jdbc")
@RequestMapping("/api/v1/jdbc/peliculas")
public class PeliculaJdbcController {

    private static final String PATRON_CODIGO = "^PEL[0-9]{3}$";

    private final PeliculaService servicio;
    private final PeliculaApiMapper mapper;

    public PeliculaJdbcController(PeliculaService servicio, PeliculaApiMapper mapper) {
        this.servicio = servicio;
        this.mapper = mapper;
    }

    @Operation(summary = "Listar todas las películas")
    @ApiResponse(responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PeliculaResponse.class))))
    @GetMapping
    public List<PeliculaResponse> listar() {
        return mapper.aRespuestas(servicio.listar());
    }

    @Operation(summary = "Obtener una película por su código")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PeliculaResponse.class)))
    @ApiResponse(responseCode = "400", content = @Content)
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{codigo}")
    public ResponseEntity<?> obtener(@PathVariable @Pattern(regexp = PATRON_CODIGO) String codigo) {
        Optional<Pelicula> pelicula = servicio.obtener(codigo);
        if (pelicula.isEmpty()) {
            return noExiste(codigo);
        }
        return ResponseEntity.ok(mapper.aRespuesta(pelicula.get()));
    }

    @Operation(summary = "Crear una película")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = PeliculaResponse.class)))
    @ApiResponse(responseCode = "400", content = @Content)
    @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody PeliculaRequest peticion) {
        Optional<Pelicula> creada = servicio.crear(mapper.aDatosCreacion(peticion));
        if (creada.isEmpty()) {
            return problema(HttpStatus.CONFLICT, "Ya existe una película con el código " + peticion.codigo());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.aRespuesta(creada.get()));
    }

    @Operation(summary = "Actualizar una película existente")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PeliculaResponse.class)))
    @ApiResponse(responseCode = "400", content = @Content)
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PutMapping("/{codigo}")
    public ResponseEntity<?> actualizar(@PathVariable @Pattern(regexp = PATRON_CODIGO) String codigo,
            @Valid @RequestBody PeliculaEdicionRequest peticion) {
        Optional<Pelicula> actualizada = servicio.actualizar(codigo, mapper.aDatosEdicion(peticion));
        if (actualizada.isEmpty()) {
            return noExiste(codigo);
        }
        return ResponseEntity.ok(mapper.aRespuesta(actualizada.get()));
    }

    @Operation(summary = "Eliminar una película")
    @ApiResponse(responseCode = "204", content = @Content)
    @ApiResponse(responseCode = "400", content = @Content)
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @DeleteMapping("/{codigo}")
    public ResponseEntity<?> eliminar(@PathVariable @Pattern(regexp = PATRON_CODIGO) String codigo) {
        if (!servicio.eliminar(codigo)) {
            return noExiste(codigo);
        }
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<ProblemDetail> noExiste(String codigo) {
        return problema(HttpStatus.NOT_FOUND, "No existe ninguna película con el código " + codigo);
    }

    private ResponseEntity<ProblemDetail> problema(HttpStatus estado, String detalle) {
        return ResponseEntity.status(estado).body(ProblemDetail.forStatusAndDetail(estado, detalle));
    }
}
