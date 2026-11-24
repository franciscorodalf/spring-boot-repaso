package ies.puesto.peliculas.repository.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import ies.puesto.peliculas.model.Pelicula;

/**
 * Prueba el SQL contra un fichero SQLite nuevo y vacío en una carpeta temporal,
 * así no depende de datos previos ni toca data/peliculas.sqlite3.
 */
class PeliculaJdbcRepositoryTest {

    @TempDir
    private Path carpeta;

    private PeliculaJdbcRepository repositorio;

    @BeforeEach
    void crearBaseVacia() {
        DriverManagerDataSource dataSource =
                new DriverManagerDataSource("jdbc:sqlite:" + carpeta.resolve("prueba.sqlite3"));
        new ResourceDatabasePopulator(new ClassPathResource("schema-sqlite.sql")).execute(dataSource);
        repositorio = new PeliculaJdbcRepository(
                new JdbcTemplate(dataSource), new PeliculaRowMapper(), new PeliculaEntityMapper());
    }

    @Test
    void insertarYBuscarPorCodigoConservaLosCuatroValores() {
        assertTrue(repositorio.insertarSiAusente(new Pelicula("PEL001", "El viaje de Chihiro", 2001, 125)));

        Pelicula leida = repositorio.buscarPorCodigo("PEL001").orElseThrow();

        assertEquals("PEL001", leida.getIdentificador());
        assertEquals("El viaje de Chihiro", leida.getNombre());
        assertEquals(2001, leida.getAnioLanzamiento());
        assertEquals(125, leida.getMinutosProyeccion());
    }

    @Test
    void buscarPorCodigoInexistenteDevuelveVacio() {
        assertEquals(Optional.empty(), repositorio.buscarPorCodigo("PEL999"));
    }

    @Test
    void buscarTodasDevuelveLasPeliculasOrdenadasPorCodigo() {
        repositorio.insertarSiAusente(new Pelicula("PEL002", "Mi vecino Totoro", 1988, 86));
        repositorio.insertarSiAusente(new Pelicula("PEL001", "El viaje de Chihiro", 2001, 125));

        List<String> codigos = repositorio.buscarTodas().stream()
                .map(Pelicula::getIdentificador)
                .toList();

        assertEquals(List.of("PEL001", "PEL002"), codigos);
    }

    @Test
    void insertarCodigoDuplicadoDevuelveFalseYNoCambiaLaFila() {
        repositorio.insertarSiAusente(new Pelicula("PEL001", "El viaje de Chihiro", 2001, 125));

        assertFalse(repositorio.insertarSiAusente(new Pelicula("PEL001", "Otro título", 1999, 90)));
        assertEquals("El viaje de Chihiro", repositorio.buscarPorCodigo("PEL001").orElseThrow().getNombre());
    }

    @Test
    void actualizarExistenteCambiaTituloAnioYDuracion() {
        repositorio.insertarSiAusente(new Pelicula("PEL001", "El viaje de Chihiro", 2001, 125));

        assertTrue(repositorio.actualizarSiExiste(new Pelicula("PEL001", "Chihiro", 2002, 126)));

        Pelicula leida = repositorio.buscarPorCodigo("PEL001").orElseThrow();
        assertEquals("Chihiro", leida.getNombre());
        assertEquals(2002, leida.getAnioLanzamiento());
        assertEquals(126, leida.getMinutosProyeccion());
    }

    @Test
    void actualizarInexistenteDevuelveFalseYNoCreaNingunaFila() {
        assertFalse(repositorio.actualizarSiExiste(new Pelicula("PEL999", "Fantasma", 2000, 100)));

        assertTrue(repositorio.buscarTodas().isEmpty());
    }

    @Test
    void eliminarDevuelveTrueLaPrimeraVezYFalseLaSegunda() {
        repositorio.insertarSiAusente(new Pelicula("PEL001", "El viaje de Chihiro", 2001, 125));

        assertTrue(repositorio.eliminar("PEL001"));
        assertFalse(repositorio.eliminar("PEL001"));
        assertTrue(repositorio.buscarPorCodigo("PEL001").isEmpty());
    }
}
