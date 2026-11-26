package aula.tareas.repository;

import aula.tareas.entity.TareaEntityXml;
import aula.tareas.mapper.TareaMapperXml;
import aula.tareas.modelo.Tarea;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Repository
@Qualifier("tareaRepositoryXml")
public class TareaRepositoryXml extends RepositorioTareaFichero {
    private final Path path;
    private final XmlMapper mapper = XmlMapper.builder()
            .defaultUseWrapper(false)
            .build();

    public TareaRepositoryXml() {
        this(Path.of("data", "tarea.xml"));
    }

    public TareaRepositoryXml(Path path) {
        this.path = path;
        try {
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se puede crear " + path, e);
        }
    }

    @JsonRootName("tareas")
    public static class DocumentoXml {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "tarea")
        private List<TareaEntityXml> tareas;

        public DocumentoXml() {
        }

        public DocumentoXml(List<TareaEntityXml> tareas) {
            this.tareas = tareas;
        }

        public List<TareaEntityXml> getTareas() {
            return tareas;
        }

        public void setTareas(List<TareaEntityXml> tareas) {
            this.tareas = tareas;
        }
    }

    @Override
    List<Tarea> readAll() {
        File fichero = path.toFile();
        if (!fichero.exists() || fichero.length() == 0) {
            return new ArrayList<>();
        }
        DocumentoXml documento = mapper.readValue(fichero, DocumentoXml.class);
        List<Tarea> resultado = new ArrayList<>();
        if (documento.getTareas() != null) {
            for (TareaEntityXml entity : documento.getTareas()) {
                resultado.add(TareaMapperXml.toDomain(entity));
            }
        }
        return resultado;
    }

    @Override
    void writeAll(List<Tarea> tareas) {
        List<TareaEntityXml> entidades = new ArrayList<>();
        for (Tarea tarea : tareas) {
            entidades.add(TareaMapperXml.toEntity(tarea));
        }
        mapper.writeValue(path.toFile(), new DocumentoXml(entidades));
    }
}
