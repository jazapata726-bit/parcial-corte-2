package com.parcial.eventos.service;

import com.parcial.eventos.dto.*;
import com.parcial.eventos.entity.Asistente;
import com.parcial.eventos.entity.Evento;
import com.parcial.eventos.exception.ConflictoException;
import com.parcial.eventos.exception.RecursoNoEncontradoException;
import com.parcial.eventos.repository.AsistenteRepository;
import com.parcial.eventos.repository.EventoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EventoService {
    private final EventoRepository eventoRepository;
    private final AsistenteRepository asistenteRepository;

    public EventoService(EventoRepository eventoRepository, AsistenteRepository asistenteRepository) {
        this.eventoRepository = eventoRepository;
        this.asistenteRepository = asistenteRepository;
    }

    public List<EventoResponse> listar() {
        return eventoRepository.findAll(Sort.by("id")).stream().map(EventoResponse::from).toList();
    }

    public EventoResponse obtener(Long id) { return EventoResponse.from(buscarEvento(id)); }

    @Transactional
    public EventoResponse crear(EventoRequest datos) {
        Evento evento = new Evento();
        asignarDatos(evento, datos);
        return EventoResponse.from(eventoRepository.save(evento));
    }

    @Transactional
    public EventoResponse actualizar(Long id, EventoRequest datos) {
        Evento evento = buscarEvento(id);
        asignarDatos(evento, datos);
        return EventoResponse.from(eventoRepository.save(evento));
    }

    @Transactional
    public void eliminar(Long id) {
        Evento evento = buscarEvento(id);
        new ArrayList<>(evento.getAsistentes()).forEach(evento::retirarAsistente);
        eventoRepository.delete(evento);
    }

    @Transactional
    public EventoResponse inscribir(Long eventoId, Long asistenteId) {
        Evento evento = buscarEvento(eventoId);
        Asistente asistente = buscarAsistente(asistenteId);
        if (evento.getAsistentes().contains(asistente)) {
            throw new ConflictoException("El asistente ya está inscrito en este evento");
        }
        evento.agregarAsistente(asistente);
        return EventoResponse.from(eventoRepository.save(evento));
    }

    @Transactional
    public void retirar(Long eventoId, Long asistenteId) {
        Evento evento = buscarEvento(eventoId);
        Asistente asistente = buscarAsistente(asistenteId);
        if (!evento.getAsistentes().contains(asistente)) {
            throw new RecursoNoEncontradoException("El asistente no está inscrito en este evento");
        }
        evento.retirarAsistente(asistente);
        eventoRepository.save(evento);
    }

    public List<AsistenteResponse> listarAsistentes(Long eventoId) {
        return buscarEvento(eventoId).getAsistentes().stream()
            .sorted(Comparator.comparing(Asistente::getId)).map(AsistenteResponse::from).toList();
    }

    public AsistenteResponse obtenerInscripcion(Long eventoId, Long asistenteId) {
        Evento evento = buscarEvento(eventoId);
        Asistente asistente = buscarAsistente(asistenteId);
        if (!evento.getAsistentes().contains(asistente)) {
            throw new RecursoNoEncontradoException("El asistente no está inscrito en este evento");
        }
        return AsistenteResponse.from(asistente);
    }

    private Evento buscarEvento(Long id) {
        return eventoRepository.findById(id).orElseThrow(() ->
            new RecursoNoEncontradoException("No existe un evento con id " + id));
    }

    private Asistente buscarAsistente(Long id) {
        return asistenteRepository.findById(id).orElseThrow(() ->
            new RecursoNoEncontradoException("No existe un asistente con id " + id));
    }

    private void asignarDatos(Evento evento, EventoRequest datos) {
        evento.setNombre(datos.nombre().strip());
        evento.setFecha(datos.fecha());
        evento.setLugar(datos.lugar().strip());
    }
}
