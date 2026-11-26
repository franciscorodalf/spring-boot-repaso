package aula.tareas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.*;
import jakarta.persistence.Table;

@Entity
@Table(name = "tareas_orm")
public class TareaEntityOrm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long idTarea;

    @Column(name = "titulo")
    private String nombreTarea;

    public TareaEntityOrm() {
    }

    public Long getIdTarea() {
        return idTarea;
    }

    public String getNombreTarea() {
        return nombreTarea;
    }

    public void setIdTarea(Long idTarea) {
        this.idTarea = idTarea;
    }

    public void setNombreTarea(String nombreTarea) {
        this.nombreTarea = nombreTarea;
    }

}
