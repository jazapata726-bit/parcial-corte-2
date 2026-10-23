package com.parcial.eventos.controller;

import com.parcial.eventos.dto.*;
import com.parcial.eventos.service.EventoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {
    private final EventoService eventoService;

    public EventoController(EventoService eventoService) { this.eventoService = eventoService; }

    @GetMapping
    public List<EventoResponse> listar() { return eventoService.listar(); }

    @GetMapping("/{id}")
    public EventoResponse obtener(@PathVariable Long id) { return eventoService.obtener(id); }

    @PostMapping
    public ResponseEntity<EventoResponse> crear(@Valid @RequestBody EventoRequest datos) {
        EventoResponse evento = eventoService.crear(datos);
        return ResponseEntity.created(URI.create("/api/eventos/" + evento.id())).body(evento);
    }

    @PutMapping("/{id}")
    public EventoResponse actualizar(@PathVariable Long id, @Valid @RequestBody EventoRequest datos) {
        return eventoService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { eventoService.eliminar(id); }

    @PostMapping("/{eventoId}/asistentes/{asistenteId}")
    public ResponseEntity<EventoResponse> inscribir(@PathVariable Long eventoId,
                                                   @PathVariable Long asistenteId) {
        EventoResponse evento = eventoService.inscribir(eventoId, asistenteId);
        return ResponseEntity.created(URI.create("/api/eventos/" + eventoId
            + "/asistentes/" + asistenteId)).body(evento);
    }

    @GetMapping("/{eventoId}/asistentes/{asistenteId}")
    public AsistenteResponse obtenerInscripcion(@PathVariable Long eventoId,
                                                @PathVariable Long asistenteId) {
        return eventoService.obtenerInscripcion(eventoId, asistenteId);
    }

    @GetMapping("/{eventoId}/asistentes")
    public List<AsistenteResponse> listarAsistentes(@PathVariable Long eventoId) {
        return eventoService.listarAsistentes(eventoId);
    }

    @DeleteMapping("/{eventoId}/asistentes/{asistenteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirar(@PathVariable Long eventoId, @PathVariable Long asistenteId) {
        eventoService.retirar(eventoId, asistenteId);
    }
}
