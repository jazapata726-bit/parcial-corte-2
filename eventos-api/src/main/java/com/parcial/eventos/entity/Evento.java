package com.parcial.eventos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "eventos")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 150)
    private String lugar;

    // Evento administra la tabla intermedia. No se propaga DELETE a Asistente.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "evento_asistente",
        joinColumns = @JoinColumn(name = "evento_id"),
        inverseJoinColumns = @JoinColumn(name = "asistente_id"))
    private Set<Asistente> asistentes = new LinkedHashSet<>();

    public Evento() { }

    public void agregarAsistente(Asistente asistente) {
        asistentes.add(asistente);
        asistente.getEventos().add(this);
    }

    public void retirarAsistente(Asistente asistente) {
        asistentes.remove(asistente);
        asistente.getEventos().remove(this);
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }
    public Set<Asistente> getAsistentes() { return asistentes; }
}
