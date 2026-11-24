package ies.puesto.peliculas.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ies.puesto.peliculas.controller.mapper.PeliculaApiMapper;
import ies.puesto.peliculas.model.Pelicula;
import ies.puesto.peliculas.service.PeliculaService;
import ies.puesto.peliculas.service.modelo.DatosCreacionPelicula;
import ies.puesto.peliculas.service.modelo.DatosEdicionPelicula;

@WebMvcTest(PeliculaJdbcController.class)
@ActiveProfiles("jdbc")
@Import(PeliculaApiMapper.class)
class PeliculaJdbcControllerTest {

    private static final String RUTA = "/api/v1/jdbc/peliculas";

    private static final Pelicula CHIHIRO = new Pelicula("PEL001", "El viaje de Chihiro", 2001, 125);
    private static final Pelicula TOTORO = new Pelicula("PEL002", "Mi vecino Totoro", 1988, 86);

    private static final String PONYO_JSON = """
            {"codigo": "PEL003", "titulo": "Ponyo", "anioEstreno": 2008, "duracionMinutos": 101}
            """;
    private static final String EDICION_JSON = """
            {"titulo": "Chihiro (reestreno)", "anioEstreno": 2002, "duracionMinutos": 126}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PeliculaService servicio;

    @Test
    void listarDevuelve200ConLaLista() throws Exception {
        when(servicio.listar()).thenReturn(List.of(CHIHIRO, TOTORO));

        mockMvc.perform(get(RUTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].codigo").value("PEL001"))
                .andExpect(jsonPath("$[1].codigo").value("PEL002"));
    }

    @Test
    void obtenerExistenteDevuelve200ConLaPelicula() throws Exception {
        when(servicio.obtener("PEL001")).thenReturn(Optional.of(CHIHIRO));

        mockMvc.perform(get(RUTA + "/PEL001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("PEL001"))
                .andExpect(jsonPath("$.titulo").value("El viaje de Chihiro"))
                .andExpect(jsonPath("$.anioEstreno").value(2001))
                .andExpect(jsonPath("$.duracionMinutos").value(125));
    }

    @Test
    void obtenerConCodigoInvalidoDevuelve400() throws Exception {
        mockMvc.perform(get(RUTA + "/ABC"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(servicio);
    }

    @Test
    void obtenerInexistenteDevuelve404ConProblemDetail() throws Exception {
        when(servicio.obtener("PEL999")).thenReturn(Optional.empty());

        mockMvc.perform(get(RUTA + "/PEL999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(containsString("PEL999")));
    }

    @Test
    void crearValidaDevuelve201ConLaPelicula() throws Exception {
        when(servicio.crear(new DatosCreacionPelicula("PEL003", "Ponyo", 2008, 101)))
                .thenReturn(Optional.of(new Pelicula("PEL003", "Ponyo", 2008, 101)));

        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(PONYO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("PEL003"))
                .andExpect(jsonPath("$.titulo").value("Ponyo"))
                .andExpect(jsonPath("$.anioEstreno").value(2008))
                .andExpect(jsonPath("$.duracionMinutos").value(101));
    }

    @Test
    void crearConAnioFueraDeRangoDevuelve400() throws Exception {
        String json = """
                {"codigo": "PEL003", "titulo": "Ponyo", "anioEstreno": 1500, "duracionMinutos": 101}
                """;

        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(servicio);
    }

    @Test
    void crearConCodigoInvalidoDevuelve400() throws Exception {
        String json = """
                {"codigo": "ABC", "titulo": "Ponyo", "anioEstreno": 2008, "duracionMinutos": 101}
                """;

        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(servicio);
    }

    @Test
    void crearConTituloEnBlancoDevuelve400() throws Exception {
        String json = """
                {"codigo": "PEL003", "titulo": "   ", "anioEstreno": 2008, "duracionMinutos": 101}
                """;

        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(servicio);
    }

    @Test
    void crearSinDuracionDevuelve400() throws Exception {
        String json = """
                {"codigo": "PEL003", "titulo": "Ponyo", "anioEstreno": 2008}
                """;

        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(servicio);
    }

    @Test
    void crearDuplicadaDevuelve409ConProblemDetail() throws Exception {
        when(servicio.crear(new DatosCreacionPelicula("PEL003", "Ponyo", 2008, 101)))
                .thenReturn(Optional.empty());

        mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(PONYO_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value(containsString("PEL003")));
    }

    @Test
    void actualizarExistenteDevuelve200ConLaPelicula() throws Exception {
        when(servicio.actualizar("PEL001", new DatosEdicionPelicula("Chihiro (reestreno)", 2002, 126)))
                .thenReturn(Optional.of(new Pelicula("PEL001", "Chihiro (reestreno)", 2002, 126)));

        mockMvc.perform(put(RUTA + "/PEL001").contentType(MediaType.APPLICATION_JSON).content(EDICION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("PEL001"))
                .andExpect(jsonPath("$.titulo").value("Chihiro (reestreno)"))
                .andExpect(jsonPath("$.anioEstreno").value(2002))
                .andExpect(jsonPath("$.duracionMinutos").value(126));
    }

    @Test
    void actualizarConCodigoInvalidoDevuelve400() throws Exception {
        mockMvc.perform(put(RUTA + "/ABC").contentType(MediaType.APPLICATION_JSON).content(EDICION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(servicio);
    }

    @Test
    void actualizarConCuerpoInvalidoDevuelve400() throws Exception {
        String json = """
                {"titulo": "Chihiro", "anioEstreno": 2002, "duracionMinutos": 0}
                """;

        mockMvc.perform(put(RUTA + "/PEL001").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(servicio);
    }

    @Test
    void actualizarInexistenteDevuelve404ConProblemDetail() throws Exception {
        when(servicio.actualizar("PEL999", new DatosEdicionPelicula("Chihiro (reestreno)", 2002, 126)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put(RUTA + "/PEL999").contentType(MediaType.APPLICATION_JSON).content(EDICION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsString("PEL999")));
    }

    @Test
    void eliminarExistenteDevuelve204SinCuerpo() throws Exception {
        when(servicio.eliminar("PEL001")).thenReturn(true);

        mockMvc.perform(delete(RUTA + "/PEL001"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void eliminarConCodigoInvalidoDevuelve400() throws Exception {
        mockMvc.perform(delete(RUTA + "/ABC"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(servicio);
    }

    @Test
    void eliminarInexistenteDevuelve404ConProblemDetail() throws Exception {
        when(servicio.eliminar("PEL999")).thenReturn(false);

        mockMvc.perform(delete(RUTA + "/PEL999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsString("PEL999")));
    }
}
