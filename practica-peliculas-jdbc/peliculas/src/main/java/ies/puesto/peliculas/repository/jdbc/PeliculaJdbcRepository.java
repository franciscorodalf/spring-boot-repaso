package ies.puesto.peliculas.repository.jdbc;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ies.puesto.peliculas.model.Pelicula;
import ies.puesto.peliculas.repository.PeliculaRepository;

@Repository
@Profile("jdbc")
public class PeliculaJdbcRepository implements PeliculaRepository {

    private static final String SQL_LISTADO =
            "SELECT pelicula_codigo, titulo_texto, estreno_anio, duracion_minutos FROM peliculas ORDER BY pelicula_codigo";

    private static final String SQL_POR_CODIGO =
            "SELECT pelicula_codigo, titulo_texto, estreno_anio, duracion_minutos FROM peliculas WHERE pelicula_codigo = ?";

    private static final String SQL_CREAR =
            "INSERT INTO peliculas (pelicula_codigo, titulo_texto, estreno_anio, duracion_minutos) "
            + "VALUES (?, ?, ?, ?) ON CONFLICT(pelicula_codigo) DO NOTHING";

    private static final String SQL_ACTUALIZAR =
            "UPDATE peliculas SET titulo_texto = ?, estreno_anio = ?, duracion_minutos = ? WHERE pelicula_codigo = ?";

    private static final String SQL_BORRAR =
            "DELETE FROM peliculas WHERE pelicula_codigo = ?";

    private final JdbcTemplate jdbcTemplate;
    private final PeliculaRowMapper rowMapper;
    private final PeliculaEntityMapper entityMapper;

    PeliculaJdbcRepository(JdbcTemplate jdbcTemplate, PeliculaRowMapper rowMapper, PeliculaEntityMapper entityMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
        this.entityMapper = entityMapper;
    }

    @Override
    public List<Pelicula> buscarTodas() {
        return jdbcTemplate.query(SQL_LISTADO, rowMapper).stream()
                .map(entityMapper::aPelicula)
                .toList();
    }

    @Override
    public Optional<Pelicula> buscarPorCodigo(String identificador) {
        return jdbcTemplate.query(SQL_POR_CODIGO, rowMapper, identificador).stream()
                .findFirst()
                .map(entityMapper::aPelicula);
    }

    @Override
    public boolean insertarSiAusente(Pelicula pelicula) {
        PeliculaEntity fila = entityMapper.aEntity(pelicula);
        return jdbcTemplate.update(SQL_CREAR,
                fila.getCodigoFila(), fila.getTituloFila(), fila.getAnioFila(), fila.getMinutosFila()) == 1;
    }

    @Override
    public boolean actualizarSiExiste(Pelicula pelicula) {
        PeliculaEntity fila = entityMapper.aEntity(pelicula);
        return jdbcTemplate.update(SQL_ACTUALIZAR,
                fila.getTituloFila(), fila.getAnioFila(), fila.getMinutosFila(), fila.getCodigoFila()) == 1;
    }

    @Override
    public boolean eliminar(String identificador) {
        return jdbcTemplate.update(SQL_BORRAR, identificador) == 1;
    }
}
