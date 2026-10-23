package com.parcial.eventos.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(Instant fecha, int estado, String error, String mensaje,
                       String ruta, Map<String, String> campos) { }
