package aula.tareas.controller;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo JSON que reciben {@code POST} y {@code PUT}: {@code {"titulo":"..."}}.
 * Si el título falta o está en blanco, Spring responde 400 automáticamente.
 */
public record PeticionTarea(@NotBlank String titulo) {}
