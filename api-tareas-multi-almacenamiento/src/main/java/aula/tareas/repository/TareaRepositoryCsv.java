package aula.tareas.repository;

import aula.tareas.entity.TareaEntityCsv;
import aula.tareas.mapper.TareaMapperCsv;
import aula.tareas.modelo.Tarea;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

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
import java.util.Optional;

@Repository
@Qualifier("tareaRepositoryCsv")
public class TareaRepositoryCsv implements ITareaRepository {
    private final Path path;

    public TareaRepositoryCsv() {
        this(Path.of("data", "tarea.csv"));
    }

    public TareaRepositoryCsv(Path path) {
        this.path = path;
    }

    private static final CSVFormat ENTRADA = CSVFormat.DEFAULT.builder()
            .setHeader().setSkipHeaderRecord(true).get();
    private static final CSVFormat SALIDA = CSVFormat.DEFAULT.builder()
            .setHeader("id", "titulo").get();

    @Override
    public synchronized List<Tarea> findAll() {
        List<Tarea> resultado = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
                CSVParser parser = ENTRADA.parse(reader)) {
            for (CSVRecord row : parser) {
                TareaEntityCsv tareaEntity = new TareaEntityCsv();
                tareaEntity.setIdTarea(Long.parseLong(row.get("id")));
                tareaEntity.setNombreTarea(row.get("titulo"));
                resultado.add(TareaMapperCsv.toDomain(tareaEntity));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se puede leer " + path, e);
        }
        resultado.sort(Comparator.comparingLong(Tarea::identificador));
        return resultado;

    }

    @Override
    public synchronized Optional<Tarea> findById(long id) {
        return findAll().stream()
                .filter(tarea -> tarea.identificador() == id)
                .findFirst();
    }

    @Override
    public synchronized Tarea create(Tarea tarea) {
        List<Tarea> items = findAll();
        long id = items.stream().mapToLong(Tarea::identificador).max().orElse(0L) + 1;
        Tarea tareaCrear = new Tarea(id, tarea.nombre().strip());
        items.add(tareaCrear);
        saveAll(items);
        return tareaCrear;
    }

    @Override
    public synchronized Optional<Tarea> update(Tarea tarea) {
        List<Tarea> items = findAll();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).identificador().equals(tarea.identificador())) {
                items.set(i, tarea);
                saveAll(items);
                return Optional.of(tarea);
            }
        }
        return Optional.empty();
    }

    @Override
    public synchronized boolean delete(long id) {
        List<Tarea> items = findAll();
        boolean borrada = items.removeIf(tarea -> tarea.identificador() == id);
        if (borrada) {
            saveAll(items);
        }
        return borrada;
    }

    private void saveAll(List<Tarea> tareas) {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
                CSVPrinter printer = new CSVPrinter(writer, SALIDA)) {
            for (Tarea tarea : tareas) {
                TareaEntityCsv tareaEntity = TareaMapperCsv.toEntity(tarea);
                printer.printRecord(tareaEntity.getIdTarea(), tareaEntity.getNombreTarea());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se puede escribir " + path, e);
        }
    }
}
