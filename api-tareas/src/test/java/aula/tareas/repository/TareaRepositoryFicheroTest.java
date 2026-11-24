package aula.tareas.repository;

import aula.tareas.modelo.Tarea;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprueba que los repositorios CSV y JSON se comportan igual y que los
 * datos sobreviven a un "reinicio" (crear otra instancia sobre el mismo fichero).
 */
class TareaRepositoryFicheroTest {

    @TempDir
    Path carpeta;

    private ITareaRepository abrir(String tipo) {
        return switch (tipo) {
            case "csv" -> new TareaRepositoryCsv(carpeta.resolve("tarea.csv").toString());
            case "json" -> new TareaRepositoryJson(JsonMapper.builder().build(),
                    carpeta.resolve("tarea.json").toString());
            default -> throw new IllegalArgumentException(tipo);
        };
    }

    @ParameterizedTest
    @ValueSource(strings = {"csv", "json"})
    void ficheroNuevoEmpiezaVacio(String tipo) {
        assertEquals(List.of(), abrir(tipo).findAll());
    }

    @ParameterizedTest
    @ValueSource(strings = {"csv", "json"})
    void creaConIdsConsecutivosYPersiste(String tipo) {
        ITareaRepository repo = abrir(tipo);
        assertEquals(new Tarea(1, "Preparar entrega"), repo.create("  Preparar entrega "));
        assertEquals(new Tarea(2, "Estudiar"), repo.create("Estudiar"));

        ITareaRepository reabierto = abrir(tipo);
        assertEquals(List.of(new Tarea(1, "Preparar entrega"), new Tarea(2, "Estudiar")),
                reabierto.findAll());
        assertEquals(new Tarea(3, "Otra"), reabierto.create("Otra"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"csv", "json"})
    void buscaPorId(String tipo) {
        ITareaRepository repo = abrir(tipo);
        repo.create("Uno");
        repo.create("Dos");
        assertEquals(new Tarea(2, "Dos"), repo.findById(2));
        assertNull(repo.findById(99));
    }

    @ParameterizedTest
    @ValueSource(strings = {"csv", "json"})
    void actualizaSoloSiExiste(String tipo) {
        ITareaRepository repo = abrir(tipo);
        repo.create("Original");

        assertEquals(new Tarea(1, "Cambiada"), repo.update(new Tarea(1, "Cambiada")));
        assertEquals(new Tarea(1, "Cambiada"), abrir(tipo).findById(1));
        assertNull(repo.update(new Tarea(99, "No existe")));
        assertEquals(1, repo.findAll().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"csv", "json"})
    void borraSoloSiExiste(String tipo) {
        ITareaRepository repo = abrir(tipo);
        repo.create("Uno");
        repo.create("Dos");

        assertTrue(repo.delete(1));
        assertFalse(repo.delete(1));
        assertEquals(List.of(new Tarea(2, "Dos")), abrir(tipo).findAll());
    }

    @ParameterizedTest
    @ValueSource(strings = {"csv", "json"})
    void guardaTitulosConCaracteresEspeciales(String tipo) {
        String titulo = "Comprar pan, leche y \"huevos\" — ñandú";
        abrir(tipo).create(titulo);
        assertEquals(titulo, abrir(tipo).findById(1).titulo());
    }

    @Test
    void csvContinuaDesdeElMayorIdExistente() throws Exception {
        Path csv = carpeta.resolve("tarea.csv");
        Files.writeString(csv, "id,titulo\r\n2,pepe\r\n");

        ITareaRepository repo = new TareaRepositoryCsv(csv.toString());
        assertEquals(new Tarea(2, "pepe"), repo.findById(2));
        assertEquals(new Tarea(3, "nueva"), repo.create("nueva"));
    }

    @Test
    void jsonCorruptoNoSeSobrescribe() throws Exception {
        Path json = carpeta.resolve("tarea.json");
        Files.writeString(json, "esto no es json");

        ITareaRepository repo = new TareaRepositoryJson(JsonMapper.builder().build(), json.toString());
        assertThrows(IllegalStateException.class, () -> repo.create("x"));
        assertEquals("esto no es json", Files.readString(json));
    }

    @Test
    void memoriaActualizaYBorra() {
        ITareaRepository repo = new TareaRepository();
        repo.create("Uno");
        assertEquals(new Tarea(1, "Dos"), repo.update(new Tarea(1, "Dos")));
        assertNull(repo.update(new Tarea(5, "No")));
        assertTrue(repo.delete(1));
        assertFalse(repo.delete(1));
    }
}
