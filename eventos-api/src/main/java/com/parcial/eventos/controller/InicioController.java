package com.parcial.eventos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class InicioController {
    @GetMapping("/")
    public Map<String, String> inicio() {
        return Map.of("api", "API de eventos y asistentes", "estado", "En funcionamiento",
            "eventos", "/api/eventos", "asistentes", "/api/asistentes",
            "salud", "/actuator/health");
    }
}
