package aula.tareas;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Recorre el ciclo completo de la API (listar, crear, consultar, buscar,
 * actualizar, borrar y errores) contra los dos almacenamientos. Los ficheros
 * se crean en una carpeta temporal para no tocar los de {@code data/}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TareaApiTest {

    @DynamicPropertySource
    static void ficherosTemporales(DynamicPropertyRegistry registry) throws IOException {
        Path carpeta = Files.createTempDirectory("api-tareas-test");
        registry.add("tareas.csv.ruta", () -> carpeta.resolve("tarea.csv").toString());
        registry.add("tareas.json.ruta", () -> carpeta.resolve("tarea.json").toString());
    }

    @Autowired
    MockMvc mvc;

    @ParameterizedTest
    @ValueSource(strings = {"/api/csv/tareas", "/api/json/tareas"})
    void cicloCompleto(String ruta) throws Exception {
        mvc.perform(get(ruta))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        mvc.perform(post(ruta).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Preparar entrega\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", ruta + "/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Preparar entrega"));

        mvc.perform(post(ruta).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Estudiar Spring\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", ruta + "/2"));

        mvc.perform(get(ruta + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Preparar entrega"));

        mvc.perform(get(ruta).param("titulo", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(2));

        mvc.perform(put(ruta + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Entrega hecha\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Entrega hecha"));

        mvc.perform(get(ruta + "/1"))
                .andExpect(jsonPath("$.titulo").value("Entrega hecha"));

        mvc.perform(delete(ruta + "/1"))
                .andExpect(status().isNoContent());

        mvc.perform(get(ruta + "/1"))
                .andExpect(status().isNotFound());

        mvc.perform(get(ruta))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/csv/tareas", "/api/json/tareas"})
    void errores(String ruta) throws Exception {
        mvc.perform(post(ruta).contentType(MediaType.APPLICATION_JSON).content("{\"titulo\":\" \"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post(ruta).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get(ruta + "/999"))
                .andExpect(status().isNotFound());
        mvc.perform(put(ruta + "/999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"x\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(put(ruta + "/999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(delete(ruta + "/999"))
                .andExpect(status().isNotFound());
    }
}
