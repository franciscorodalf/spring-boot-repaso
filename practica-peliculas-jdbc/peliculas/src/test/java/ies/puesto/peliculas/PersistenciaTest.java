package ies.puesto.peliculas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import ies.puesto.peliculas.model.Pelicula;
import ies.puesto.peliculas.service.PeliculaService;
import ies.puesto.peliculas.service.modelo.DatosCreacionPelicula;

/**
 * Arranca la aplicación completa, crea una película, la para y la vuelve a arrancar
 * sobre el mismo fichero SQLite: la película tiene que seguir ahí.
 * Usa un fichero temporal para no tocar data/peliculas.sqlite3.
 */
class PersistenciaTest {

    @TempDir
    private Path carpeta;

    @Test
    void losDatosSeConservanAlReiniciarLaAplicacion() {
        String url = "jdbc:sqlite:" + carpeta.resolve("peliculas.sqlite3");

        try (ConfigurableApplicationContext app = arrancar(url)) {
            app.getBean(PeliculaService.class)
                    .crear(new DatosCreacionPelicula("PEL003", "Ponyo", 2008, 101))
                    .orElseThrow();
        }

        try (ConfigurableApplicationContext app = arrancar(url)) {
            Pelicula leida = app.getBean(PeliculaService.class).obtener("PEL003").orElseThrow();

            assertEquals("Ponyo", leida.getNombre());
            assertEquals(2008, leida.getAnioLanzamiento());
            assertEquals(101, leida.getMinutosProyeccion());
        }
    }

    private ConfigurableApplicationContext arrancar(String url) {
        return new SpringApplicationBuilder(PeliculasApplication.class).run(
                "--spring.profiles.active=jdbc",
                "--server.port=0",
                "--spring.datasource.url=" + url,
                "--spring.sql.init.mode=always",
                "--spring.sql.init.schema-locations=classpath:schema-sqlite.sql");
    }
}
