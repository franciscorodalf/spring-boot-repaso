package aula.tareas.repository;

import aula.tareas.modelo.Tarea;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Repositorio que guarda las tareas en un fichero CSV con cabecera {@code id,titulo}.
 * Cada operación lee el fichero completo y, si modifica datos, lo vuelve a escribir.
 */
@Repository("repositoryCsv")
public class TareaRepositoryCsv implements ITareaRepository {
    private final Path path;
    private final CSVFormat inputFormat = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .build();
    private final CSVFormat outputFormat = CSVFormat.DEFAULT.builder()
            .setHeader("id", "titulo")
            .build();

    public TareaRepositoryCsv(@Value("${tareas.csv.ruta:data/tarea.csv}") String ruta) {
        this.path = Path.of(ruta);
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se puede crear el fichero " + path, e);
        }
    }

    @Override
    public synchronized Tarea create(String titulo) {
        List<Tarea> items = leer();
        long id = items.stream().mapToLong(Tarea::id).max().orElse(0L) + 1;

        Tarea tarea = new Tarea(id, titulo.strip());
        items.add(tarea);
        guardar(items);
        return tarea;
    }

    @Override
    public synchronized boolean delete(long identificador) {
        List<Tarea> items = leer();
        boolean borrada = items.removeIf(tarea -> tarea.id() == identificador);
        if (borrada) {
            guardar(items);
        }
        return borrada;
    }

    @Override
    public synchronized List<Tarea> findAll() {
        return List.copyOf(leer());
    }

    @Override
    public synchronized Tarea findById(long identificador) {
        return leer().stream()
                .filter(tarea -> tarea.id() == identificador)
                .findFirst()
                .orElse(null);
    }

    @Override
    public synchronized Tarea update(Tarea tarea) {
        List<Tarea> items = leer();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id() == tarea.id()) {
                items.set(i, tarea);
                guardar(items);
                return tarea;
            }
        }
        return null;
    }

    /** Lee todas las filas del CSV. Un fichero vacío equivale a una lista vacía. */
    private List<Tarea> leer() {
        List<Tarea> resultado = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
                CSVParser parser = inputFormat.parse(reader)) {
            for (CSVRecord row : parser) {
                resultado.add(new Tarea(Long.parseLong(row.get("id")), row.get("titulo")));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se puede leer " + path, e);
        }
        resultado.sort(Comparator.comparingLong(Tarea::id));
        return resultado;
    }

    /** Reescribe el fichero completo con la cabecera y todas las tareas. */
    private void guardar(List<Tarea> items) {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
                CSVPrinter printer = new CSVPrinter(writer, outputFormat)) {
            for (Tarea tarea : items) {
                printer.printRecord(tarea.id(), tarea.titulo());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se puede escribir " + path, e);
        }
    }
}
