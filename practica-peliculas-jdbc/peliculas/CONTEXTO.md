# Contexto de la práctica «peliculas» (API REST con Spring Boot + JDBC + SQLite)

## Cómo quiero que me ayudes
- Soy estudiante de 2º DAM. Piensa paso a paso y explícame el razonamiento.
- NO me des la solución directamente: guíame con preguntas y pistas para que yo escriba el código.
- La rúbrica exige poder explicar el código en una posible defensa.
- Sé crítico y neutral; pregunta antes de empezar si algo no está claro.
- No borres ni modifiques `../data/peliculas.sqlite3` (penalización).

## Enunciado
Lo define `../README_PELICULAS.md` (fuente de verdad). Resumen:
- Java 21, Spring Boot, Maven, SQLite. `groupId ies.puesto`, `artifactId peliculas`.
- Paquete raíz: `ies.puesto.peliculas`.
- Capas: controller (api, mapper), service (modelo, mapper), model, repository (jdbc).
- Nombres distintos por capa; ningún objeto pasa de capa sin mapper (sin BeanUtils/ModelMapper).

| Dato | controller.api | service.modelo | model | repository.jdbc | Columna |
|---|---|---|---|---|---|
| Identificador | codigo | referenciaSolicitada | identificador | codigoFila | pelicula_codigo |
| Título | titulo | nombreSolicitado | nombre | tituloFila | titulo_texto |
| Año | anioEstreno | anioSolicitado | anioLanzamiento | anioFila | estreno_anio |
| Duración | duracionMinutos | minutosSolicitados | minutosProyeccion | minutosFila | duracion_minutos |

Tipos: `record` en controller.api y service.modelo; `class` (atributos private final, constructor, getters, sin setters) en model y repository.jdbc. En las peticiones año/duración son `Integer`; en el resto, `int`.
Validación (en las peticiones): codigo `^PEL[0-9]{3}$`; titulo obligatorio, @NotBlank, máx. 120; anioEstreno 1888–2100; duracionMinutos 1–1000.
Visibilidad de paquete (sin `public`): PeliculaEntity, PeliculaRowMapper, PeliculaEntityMapper.

## Estado actual
- [x] pom.xml: Spring Boot 3.5.16, Web, Validation, JDBC, springdoc 2.8.13, sqlite-jdbc 3.51.2.0 (aviso de fin de soporte OSS de 3.5.x: ignorado a propósito).
- [x] Paquetes movidos a `ies.puesto.peliculas`; `PeliculaDominioMapper` en `service.mapper`.
- [x] Borrar `src/main/java/ies/puesto/Main.java` (sobrante) y la carpeta vacía `service/modelo/mapper`.
- [x] Copiar (no mover) `../data` a esta carpeta, junto al `pom.xml` (copia idéntica, original intacto).
- [ ] Añadir `application.properties` (url `jdbc:sqlite:data/peliculas.sqlite3`, driver `org.sqlite.JDBC`, pool 1) y perfil `jdbc` (`-Dspring-boot.run.profiles=jdbc`).
- [ ] `PeliculasApplication` con `@SpringBootApplication` y `main`.
- [ ] Convertir PeliculaRequest, PeliculaEdicionRequest y PeliculaResponse a `record` (ahora son `class`) con validación.
- [ ] Resto de tipos, 4 mappers, repositorio JDBC, servicio, controlador (@Profile("jdbc"), @RequestMapping("/api/v1/jdbc/peliculas")).
- [ ] Pruebas (mappers, MockMvc), Swagger UI, `docs/SERVICIO.md`, commits frecuentes.

## Avisos
- Arrancar siempre desde esta carpeta (ruta relativa de SQLite). Si `data/peliculas.sqlite3` no existe, SQLite crea un fichero vacío sin tabla.
- Sin `@RestControllerAdvice` ni `@Validated` en la clase. 404 y 409 con `ProblemDetail` desde el controlador.
- El comando del README para arrancar está cortado: falta indicar el perfil `jdbc`.
