package ies.puesto.peliculas.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ies.puesto.peliculas.model.Pelicula;
import ies.puesto.peliculas.service.modelo.DatosCreacionPelicula;
import ies.puesto.peliculas.service.modelo.DatosEdicionPelicula;

class PeliculaDominioMapperTest {

    private final PeliculaDominioMapper mapper = new PeliculaDominioMapper();

    @Test
    void aPeliculaDesdeDatosDeCreacionConservaLosCuatroValores() {
        DatosCreacionPelicula datos = new DatosCreacionPelicula("PEL010", "Akira", 1988, 124);

        Pelicula pelicula = mapper.aPelicula(datos);

        assertEquals("PEL010", pelicula.getIdentificador());
        assertEquals("Akira", pelicula.getNombre());
        assertEquals(1988, pelicula.getAnioLanzamiento());
        assertEquals(124, pelicula.getMinutosProyeccion());
    }

    @Test
    void aPeliculaDesdeDatosDeEdicionUsaElCodigoDeLaUrl() {
        DatosEdicionPelicula datos = new DatosEdicionPelicula("Paprika", 2006, 90);

        Pelicula pelicula = mapper.aPelicula("PEL020", datos);

        assertEquals("PEL020", pelicula.getIdentificador());
        assertEquals("Paprika", pelicula.getNombre());
        assertEquals(2006, pelicula.getAnioLanzamiento());
        assertEquals(90, pelicula.getMinutosProyeccion());
    }
}
