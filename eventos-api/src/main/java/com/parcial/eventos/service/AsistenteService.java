package com.parcial.eventos.service;

import com.parcial.eventos.dto.*;
import com.parcial.eventos.entity.Asistente;
import com.parcial.eventos.entity.Evento;
import com.parcial.eventos.exception.ConflictoException;
import com.parcial.eventos.exception.RecursoNoEncontradoException;
import com.parcial.eventos.repository.AsistenteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AsistenteService {
    private final AsistenteRepository asistenteRepository;

    public AsistenteService(AsistenteRepository asistenteRepository) {
        this.asistenteRepository = asistenteRepository;
    }

    public List<AsistenteResponse> listar() {
        return asistenteRepository.findAll(Sort.by("id")).stream()
            .map(AsistenteResponse::from).toList();
    }

    public AsistenteResponse obtener(Long id) { return AsistenteResponse.from(buscar(id)); }

    @Transactional
    public AsistenteResponse crear(AsistenteRequest datos) {
        String correo = datos.correo().strip().toLowerCase(Locale.ROOT);
        if (asistenteRepository.existsByCorreo(correo)) {
            throw new ConflictoException("Ya existe un asistente con ese correo");
        }
        Asistente asistente = new Asistente();
        asignarDatos(asistente, datos, correo);
        return AsistenteResponse.from(asistenteRepository.save(asistente));
    }

    @Transactional
    public AsistenteResponse actualizar(Long id, AsistenteRequest datos) {
        Asistente asistente = buscar(id);
        String correo = datos.correo().strip().toLowerCase(Locale.ROOT);
        if (asistenteRepository.existsByCorreoAndIdNot(correo, id)) {
            throw new ConflictoException("Ya existe otro asistente con ese correo");
        }
        asignarDatos(asistente, datos, correo);
        return AsistenteResponse.from(asistenteRepository.save(asistente));
    }

    @Transactional
    public void eliminar(Long id) {
        Asistente asistente = buscar(id);
        // Se eliminan las inscripciones, conservando los eventos.
        new ArrayList<>(asistente.getEventos()).forEach(evento -> evento.retirarAsistente(asistente));
        asistenteRepository.delete(asistente);
    }

    public List<EventoResponse> listarEventos(Long asistenteId) {
        return buscar(asistenteId).getEventos().stream()
            .sorted(Comparator.comparing(Evento::getId)).map(EventoResponse::from).toList();
    }

    private Asistente buscar(Long id) {
        return asistenteRepository.findById(id).orElseThrow(() ->
            new RecursoNoEncontradoException("No existe un asistente con id " + id));
    }

    private void asignarDatos(Asistente asistente, AsistenteRequest datos, String correo) {
        asistente.setNombre(datos.nombre().strip());
        asistente.setCorreo(correo);
        asistente.setTelefono(datos.telefono() == null ? null : datos.telefono().strip());
    }
}
