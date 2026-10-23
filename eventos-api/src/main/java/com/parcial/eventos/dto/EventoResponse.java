package com.parcial.eventos.dto;

import com.parcial.eventos.entity.Asistente;
import com.parcial.eventos.entity.Evento;
import java.time.LocalDateTime;
import java.util.List;

public record EventoResponse(Long id, String nombre, LocalDateTime fecha,
                             String lugar, List<Long> asistentesIds) {
    public static EventoResponse from(Evento evento) {
        return new EventoResponse(evento.getId(), evento.getNombre(), evento.getFecha(),
            evento.getLugar(), evento.getAsistentes().stream()
                .map(Asistente::getId).sorted().toList());
    }
}
