# Recorrido de una petición por las capas

La API de películas (perfil `jdbc`) tiene cuatro capas. Cada una usa su propio tipo y sus propios nombres, así que entre capa y capa siempre hay un mapper que traduce:

```
JSON ─► PeliculaJdbcController ─► PeliculaService ─► PeliculaRepository ─► SQLite
         (controller)              (service)          (PeliculaJdbcRepository)
```

| Dato | JSON / `controller.api` | `service.modelo` | `model` | `repository.jdbc` | Columna SQLite |
|---|---|---|---|---|---|
| Identificador | `codigo` | `referenciaSolicitada` | `identificador` | `codigoFila` | `pelicula_codigo` |
| Título | `titulo` | `nombreSolicitado` | `nombre` | `tituloFila` | `titulo_texto` |
| Año | `anioEstreno` | `anioSolicitado` | `anioLanzamiento` | `anioFila` | `estreno_anio` |
| Duración | `duracionMinutos` | `minutosSolicitados` | `minutosProyeccion` | `minutosFila` | `duracion_minutos` |

---

## 1. POST `/api/v1/jdbc/peliculas` (crear)

Cuerpo de ejemplo:

```json
{ "codigo": "PEL003", "titulo": "Ponyo", "anioEstreno": 2008, "duracionMinutos": 101 }
```

### 1.1 Clases por las que pasa

**Ida (petición):**

1. Spring convierte el JSON en un `PeliculaRequest` (record de `controller.api`) y lo valida por `@Valid`.
2. `PeliculaJdbcController.crear(...)` lo convierte en `DatosCreacionPelicula` (record de `service.modelo`).
3. `PeliculaService.crear(...)` construye una `Pelicula` (clase de `model`) y llama a `PeliculaRepository.insertarSiAusente(...)`.
4. `PeliculaJdbcRepository.insertarSiAusente(...)` convierte la `Pelicula` en `PeliculaEntity` y ejecuta el `INSERT` con `JdbcTemplate.update(...)`.
5. SQLite inserta la fila en la tabla `peliculas`, o no hace nada si el código ya existía.

**Vuelta (respuesta):**

1. SQLite devuelve el número de filas afectadas (`1` o `0`).
2. `PeliculaJdbcRepository` devuelve `true` solo si ese número es exactamente `1`.
3. `PeliculaService` devuelve `Optional.of(pelicula)` con la misma `Pelicula` que ha construido (no vuelve a leerla), u `Optional.empty()` si el repositorio devolvió `false`.
4. `PeliculaJdbcController` convierte la `Pelicula` en `PeliculaResponse` y la envuelve en un `ResponseEntity`.
5. Spring convierte el `PeliculaResponse` en JSON.

### 1.2 Mapper de cada frontera

| Frontera | Mapper | Método |
|---|---|---|
| controller → service | `PeliculaApiMapper` | `aDatosCreacion(PeliculaRequest)` → `DatosCreacionPelicula` |
| service → model | `PeliculaDominioMapper` | `aPelicula(DatosCreacionPelicula)` → `Pelicula` |
| model → fila | `PeliculaEntityMapper` | `aEntity(Pelicula)` → `PeliculaEntity` |
| fila → SQL | (ninguno: el repositorio pasa los getters de `PeliculaEntity` a los `?` del `INSERT`) | |
| model → JSON (vuelta) | `PeliculaApiMapper` | `aRespuesta(Pelicula)` → `PeliculaResponse` |

### 1.3 Nombre de cada dato en cada capa

Ejemplo con el título, que viaja así:

`titulo` (JSON) → `peticion.titulo()` → `nombreSolicitado` → `nombre` → `tituloFila` → columna `titulo_texto`

Los cuatro datos, en el orden en que los ve el `INSERT`:

```sql
INSERT INTO peliculas (pelicula_codigo, titulo_texto, estreno_anio, duracion_minutos)
VALUES (?, ?, ?, ?) ON CONFLICT(pelicula_codigo) DO NOTHING
-- parámetros: codigoFila, tituloFila, anioFila, minutosFila
```

| JSON | `DatosCreacionPelicula` | `Pelicula` | `PeliculaEntity` | Columna |
|---|---|---|---|---|
| `codigo` | `referenciaSolicitada` | `identificador` | `codigoFila` | `pelicula_codigo` |
| `titulo` | `nombreSolicitado` | `nombre` | `tituloFila` | `titulo_texto` |
| `anioEstreno` (`Integer`) | `anioSolicitado` (`int`) | `anioLanzamiento` | `anioFila` | `estreno_anio` |
| `duracionMinutos` (`Integer`) | `minutosSolicitados` (`int`) | `minutosProyeccion` | `minutosFila` | `duracion_minutos` |

En la petición el año y la duración son `Integer` para que un campo ausente llegue como `null` y `@NotNull` lo rechace. A partir de `PeliculaApiMapper` ya son `int`.

### 1.4 Cómo se decide el código HTTP

| Situación | Quién lo decide | Estado |
|---|---|---|
| JSON mal formado o alguna regla de validación incumplida (`@Pattern`, `@NotBlank`, `@Size`, `@Min`, `@Max`, `@NotNull`) | Spring, antes de entrar en el método del controlador | **400** |
| El `INSERT` afecta a 0 filas (el código ya existía) → repositorio `false` → servicio `Optional.empty()` | `PeliculaJdbcController`, con `ProblemDetail` y un `detail` que nombra el código | **409** |
| El `INSERT` afecta a 1 fila → repositorio `true` → servicio `Optional.of(pelicula)` | `PeliculaJdbcController`, con `ResponseEntity.status(HttpStatus.CREATED)` | **201** |

---

## 2. GET `/api/v1/jdbc/peliculas/{codigo}` (consultar por código)

Ejemplo: `GET /api/v1/jdbc/peliculas/PEL001`

### 2.1 Clases por las que pasa

**Ida (petición):**

1. Spring lee `{codigo}` de la URL con `@PathVariable` y comprueba `@Pattern(regexp = "^PEL[0-9]{3}$")`.
2. `PeliculaJdbcController.obtener(codigo)` llama a `PeliculaService.obtener(codigo)`. Solo viaja el `String`, así que no hace falta ningún mapper de ida.
3. `PeliculaService.obtener(...)` llama a `PeliculaRepository.buscarPorCodigo(codigo)`.
4. `PeliculaJdbcRepository.buscarPorCodigo(...)` ejecuta el `SELECT ... WHERE pelicula_codigo = ?` con `JdbcTemplate.query(...)`.

**Vuelta (respuesta):**

1. SQLite devuelve 0 o 1 filas en un `ResultSet`.
2. `PeliculaRowMapper` convierte cada fila en `PeliculaEntity`.
3. `PeliculaJdbcRepository` se queda con la primera (`stream().findFirst()`) y la convierte en `Pelicula` con `map(...)`. Si no hay filas, devuelve `Optional.empty()`. No se usa `queryForObject` porque lanza una excepción cuando no hay fila.
4. `PeliculaService` devuelve ese mismo `Optional<Pelicula>`.
5. `PeliculaJdbcController` convierte la `Pelicula` en `PeliculaResponse`, o construye un `ProblemDetail` si el `Optional` está vacío.
6. Spring convierte la respuesta en JSON.

### 2.2 Mapper de cada frontera

| Frontera | Mapper | Método |
|---|---|---|
| SQL → fila | `PeliculaRowMapper` | `mapRow(ResultSet, int)` → `PeliculaEntity` |
| fila → model | `PeliculaEntityMapper` | `aPelicula(PeliculaEntity)` → `Pelicula` |
| model → JSON | `PeliculaApiMapper` | `aRespuesta(Pelicula)` → `PeliculaResponse` |

En la ida no hay mapper: el código de la URL pasa tal cual como `String` (`codigo` en el controlador y el servicio, `identificador` en el contrato del repositorio).

### 2.3 Nombre de cada dato en cada capa

Aquí el dato viaja en sentido contrario, de la columna al JSON:

| Columna | `ResultSet` | `PeliculaEntity` | `Pelicula` | `PeliculaResponse` / JSON |
|---|---|---|---|---|
| `pelicula_codigo` | `rs.getString("pelicula_codigo")` | `codigoFila` | `identificador` | `codigo` |
| `titulo_texto` | `rs.getString("titulo_texto")` | `tituloFila` | `nombre` | `titulo` |
| `estreno_anio` | `rs.getInt("estreno_anio")` | `anioFila` | `anioLanzamiento` | `anioEstreno` |
| `duracion_minutos` | `rs.getInt("duracion_minutos")` | `minutosFila` | `minutosProyeccion` | `duracionMinutos` |

Respuesta de ejemplo:

```json
{ "codigo": "PEL001", "titulo": "El viaje de Chihiro", "anioEstreno": 2001, "duracionMinutos": 125 }
```

### 2.4 Cómo se decide el código HTTP

| Situación | Quién lo decide | Estado |
|---|---|---|
| El código de la URL no cumple `^PEL[0-9]{3}$` (por ejemplo `/ABC`) | Spring, por el `@Pattern` del `@PathVariable` (validación de métodos integrada; por eso la clase **no** lleva `@Validated`, que convertiría el error en un 500) | **400** |
| El `SELECT` no devuelve filas → `Optional.empty()` | `PeliculaJdbcController`, con `ProblemDetail` de estado 404 y `detail` "No existe ninguna película con el código PEL999" | **404** |
| El `SELECT` devuelve una fila → `Optional.of(pelicula)` | `PeliculaJdbcController`, con `ResponseEntity.ok(...)` | **200** |

---

## Resumen de responsabilidades

- **Controlador**: recibe y valida la entrada HTTP, llama al servicio y traduce `Optional` / `boolean` a 200, 201, 204, 404 o 409.
- **Servicio**: construye la `Pelicula` y decide a qué operación del repositorio llamar. No lanza excepciones ni conoce nada de HTTP.
- **Repositorio JDBC**: solo SQL. Devuelve `Optional` o `true`/`false` según las filas leídas o afectadas.
- **Mappers**: solo trasladan valores entre tipos con constructores. No validan ni deciden nada.
