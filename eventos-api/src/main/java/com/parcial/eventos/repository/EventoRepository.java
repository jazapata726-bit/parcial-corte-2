package com.parcial.eventos.repository;

import com.parcial.eventos.entity.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, Long> { }
