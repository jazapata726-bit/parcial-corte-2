package com.parcial.eventos.dto;

import com.parcial.eventos.entity.Asistente;
import com.parcial.eventos.entity.Evento;
import java.util.List;

public record AsistenteResponse(Long id, String nombre, String correo,
                                String telefono, List<Long> eventosIds) {
    public static AsistenteResponse from(Asistente asistente) {
        return new AsistenteResponse(asistente.getId(), asistente.getNombre(),
            asistente.getCorreo(), asistente.getTelefono(), asistente.getEventos().stream()
                .map(Evento::getId).sorted().toList());
    }
}
