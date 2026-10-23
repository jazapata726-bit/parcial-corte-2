package com.parcial.eventos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record EventoRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre admite máximo 120 caracteres")
    String nombre,
    @NotNull(message = "La fecha es obligatoria") LocalDateTime fecha,
    @NotBlank(message = "El lugar es obligatorio")
    @Size(max = 150, message = "El lugar admite máximo 150 caracteres")
    String lugar
) { }
