package com.parcial.eventos.controller;

import com.parcial.eventos.dto.*;
import com.parcial.eventos.service.AsistenteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/asistentes")
public class AsistenteController {
    private final AsistenteService asistenteService;

    public AsistenteController(AsistenteService asistenteService) {
        this.asistenteService = asistenteService;
    }

    @GetMapping
    public List<AsistenteResponse> listar() { return asistenteService.listar(); }

    @GetMapping("/{id}")
    public AsistenteResponse obtener(@PathVariable Long id) { return asistenteService.obtener(id); }

    @PostMapping
    public ResponseEntity<AsistenteResponse> crear(@Valid @RequestBody AsistenteRequest datos) {
        AsistenteResponse asistente = asistenteService.crear(datos);
        return ResponseEntity.created(URI.create("/api/asistentes/" + asistente.id())).body(asistente);
    }

    @PutMapping("/{id}")
    public AsistenteResponse actualizar(@PathVariable Long id,
                                        @Valid @RequestBody AsistenteRequest datos) {
        return asistenteService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { asistenteService.eliminar(id); }

    @GetMapping("/{id}/eventos")
    public List<EventoResponse> listarEventos(@PathVariable Long id) {
        return asistenteService.listarEventos(id);
    }
}
