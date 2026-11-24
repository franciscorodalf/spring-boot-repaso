package ies.puesto.peliculas.controller.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import ies.puesto.peliculas.controller.api.PeliculaEdicionRequest;
import ies.puesto.peliculas.controller.api.PeliculaRequest;
import ies.puesto.peliculas.controller.api.PeliculaResponse;
import ies.puesto.peliculas.model.Pelicula;
import ies.puesto.peliculas.service.modelo.DatosCreacionPelicula;
import ies.puesto.peliculas.service.modelo.DatosEdicionPelicula;

class PeliculaApiMapperTest {

    private final PeliculaApiMapper mapper = new PeliculaApiMapper();

    @Test
    void aDatosCreacionConservaLosCuatroValores() {
        PeliculaRequest peticion = new PeliculaRequest("PEL010", "Akira", 1988, 124);

        DatosCreacionPelicula datos = mapper.aDatosCreacion(peticion);

        assertEquals("PEL010", datos.referenciaSolicitada());
        assertEquals("Akira", datos.nombreSolicitado());
        assertEquals(1988, datos.anioSolicitado());
        assertEquals(124, datos.minutosSolicitados());
    }

    @Test
    void aDatosEdicionConservaLosTresValores() {
        PeliculaEdicionRequest peticion = new PeliculaEdicionRequest("Paprika", 2006, 90);

        DatosEdicionPelicula datos = mapper.aDatosEdicion(peticion);

        assertEquals("Paprika", datos.nombreSolicitado());
        assertEquals(2006, datos.anioSolicitado());
        assertEquals(90, datos.minutosSolicitados());
    }

    @Test
    void aRespuestaConservaLosCuatroValores() {
        Pelicula pelicula = new Pelicula("PEL001", "El viaje de Chihiro", 2001, 125);

        PeliculaResponse respuesta = mapper.aRespuesta(pelicula);

        assertEquals("PEL001", respuesta.codigo());
        assertEquals("El viaje de Chihiro", respuesta.titulo());
        assertEquals(2001, respuesta.anioEstreno());
        assertEquals(125, respuesta.duracionMinutos());
    }

    @Test
    void aRespuestasConvierteCadaPeliculaEnOrden() {
        List<Pelicula> peliculas = List.of(
                new Pelicula("PEL001", "El viaje de Chihiro", 2001, 125),
                new Pelicula("PEL002", "Mi vecino Totoro", 1988, 86));

        List<PeliculaResponse> respuestas = mapper.aRespuestas(peliculas);

        assertEquals(List.of(
                new PeliculaResponse("PEL001", "El viaje de Chihiro", 2001, 125),
                new PeliculaResponse("PEL002", "Mi vecino Totoro", 1988, 86)), respuestas);
    }
}
