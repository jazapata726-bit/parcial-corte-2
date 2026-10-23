package com.parcial.eventos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventosApiIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private JdbcTemplate jdbc;

    // Sin transacción de prueba: cada petición confirma sus cambios en la base.
    @BeforeEach
    void limpiarDatos() {
        jdbc.update("DELETE FROM evento_asistente");
        jdbc.update("DELETE FROM eventos");
        jdbc.update("DELETE FROM asistentes");
    }

    @Test
    void crudCompletoDeEventos() throws Exception {
        long id = crearEvento("Conferencia Java");
        mvc.perform(get("/api/eventos"))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/eventos/{id}", id))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Conferencia Java"));
        mvc.perform(put("/api/eventos/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(eventoJson("Conferencia actualizada")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Conferencia actualizada"));
        mvc.perform(delete("/api/eventos/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/eventos/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void crudCompletoDeAsistentes() throws Exception {
        long id = crearAsistente("Ana", "ana@example.com");
        mvc.perform(get("/api/asistentes"))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/asistentes/{id}", id))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Ana"));
        mvc.perform(put("/api/asistentes/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(asistenteJson("Ana María", "ana.maria@example.com")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.correo").value("ana.maria@example.com"));
        mvc.perform(delete("/api/asistentes/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/asistentes/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void relacionMuchosAMuchosEnAmbasDirecciones() throws Exception {
        long e1 = crearEvento("Java");
        long e2 = crearEvento("Docker");
        long a1 = crearAsistente("Ana", "ana@example.com");
        long a2 = crearAsistente("Luis", "luis@example.com");
        inscribir(e1, a1);
        inscribir(e1, a2);
        inscribir(e2, a1);
        mvc.perform(get("/api/eventos/{id}/asistentes", e1))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/asistentes/{id}/eventos", a1))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/eventos/{e}/asistentes/{a}", e1, a1))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(a1));
    }

    @Test
    void retirarInscripcionConservaLosRegistrosYLasOtrasInscripciones() throws Exception {
        long e1 = crearEvento("Java");
        long e2 = crearEvento("Docker");
        long a = crearAsistente("Ana", "ana@example.com");
        inscribir(e1, a);
        inscribir(e2, a);
        mvc.perform(delete("/api/eventos/{e}/asistentes/{a}", e1, a))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/eventos/{id}", e1))
            .andExpect(status().isOk()).andExpect(jsonPath("$.asistentesIds", empty()));
        mvc.perform(get("/api/asistentes/{id}/eventos", a))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(e2));
        mvc.perform(delete("/api/eventos/{e}/asistentes/{a}", e1, a))
            .andExpect(status().isNotFound());
    }

    @Test
    void borrarEventoConservaAsistentesYLosDemasEventos() throws Exception {
        long e1 = crearEvento("Java");
        long e2 = crearEvento("Docker");
        long a1 = crearAsistente("Ana", "ana@example.com");
        long a2 = crearAsistente("Luis", "luis@example.com");
        inscribir(e1, a1);
        inscribir(e1, a2);
        inscribir(e2, a1);
        mvc.perform(delete("/api/eventos/{id}", e1)).andExpect(status().isNoContent());
        mvc.perform(get("/api/asistentes"))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/eventos/{id}", e2))
            .andExpect(status().isOk()).andExpect(jsonPath("$.asistentesIds", contains((int) a1)));
        mvc.perform(get("/api/asistentes/{id}", a2))
            .andExpect(status().isOk()).andExpect(jsonPath("$.eventosIds", empty()));
    }

    @Test
    void borrarAsistenteConservaEventosYLosDemasAsistentes() throws Exception {
        long e1 = crearEvento("Java");
        long e2 = crearEvento("Docker");
        long a1 = crearAsistente("Ana", "ana@example.com");
        long a2 = crearAsistente("Luis", "luis@example.com");
        inscribir(e1, a1);
        inscribir(e1, a2);
        inscribir(e2, a1);
        mvc.perform(delete("/api/asistentes/{id}", a1)).andExpect(status().isNoContent());
        mvc.perform(get("/api/eventos"))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/eventos/{id}", e1))
            .andExpect(status().isOk()).andExpect(jsonPath("$.asistentesIds", contains((int) a2)));
        mvc.perform(get("/api/eventos/{id}", e2))
            .andExpect(status().isOk()).andExpect(jsonPath("$.asistentesIds", empty()));
    }

    @Test
    void rechazaCorreosDuplicadosAlCrearYActualizar() throws Exception {
        crearAsistente("Ana", "ana@example.com");
        long luis = crearAsistente("Luis", "luis@example.com");
        mvc.perform(post("/api/asistentes").contentType(MediaType.APPLICATION_JSON)
                .content(asistenteJson("Otra Ana", "ANA@example.com")))
            .andExpect(status().isConflict());
        mvc.perform(put("/api/asistentes/{id}", luis).contentType(MediaType.APPLICATION_JSON)
                .content(asistenteJson("Luis", "ana@example.com")))
            .andExpect(status().isConflict());
        mvc.perform(put("/api/asistentes/{id}", luis).contentType(MediaType.APPLICATION_JSON)
                .content(asistenteJson("Luis Pérez", "luis@example.com")))
            .andExpect(status().isOk());
    }

    @Test
    void validaCamposObligatoriosDeEvento() throws Exception {
        mvc.perform(post("/api/eventos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\" \",\"lugar\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.nombre", notNullValue()))
            .andExpect(jsonPath("$.campos.fecha", notNullValue()))
            .andExpect(jsonPath("$.campos.lugar", notNullValue()));
    }

    @Test
    void validaNombreYCorreoDeAsistente() throws Exception {
        mvc.perform(post("/api/asistentes").contentType(MediaType.APPLICATION_JSON)
                .content(asistenteJson(" ", "correo-invalido")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.nombre", notNullValue()))
            .andExpect(jsonPath("$.campos.correo", notNullValue()));
    }

    @Test
    void recursosInexistentesDevuelven404() throws Exception {
        for (String recurso : new String[]{"eventos", "asistentes"}) {
            mvc.perform(get("/api/" + recurso + "/999999")).andExpect(status().isNotFound());
            mvc.perform(delete("/api/" + recurso + "/999999")).andExpect(status().isNotFound());
            String body = recurso.equals("eventos") ? eventoJson("Java")
                : asistenteJson("Ana", "ana@example.com");
            mvc.perform(put("/api/" + recurso + "/999999").contentType(MediaType.APPLICATION_JSON)
                    .content(body)).andExpect(status().isNotFound());
        }
    }

    @Test
    void controlaInscripcionDuplicadaYRecursosInexistentes() throws Exception {
        long e = crearEvento("Java");
        long a = crearAsistente("Ana", "ana@example.com");
        inscribir(e, a);
        mvc.perform(post("/api/eventos/{e}/asistentes/{a}", e, a))
            .andExpect(status().isConflict());
        mvc.perform(post("/api/eventos/{e}/asistentes/{a}", e, 999999))
            .andExpect(status().isNotFound());
        mvc.perform(post("/api/eventos/{e}/asistentes/{a}", 999999, a))
            .andExpect(status().isNotFound());
    }

    @Test
    void rechazaJsonFechaEIdentificadorInvalidos() throws Exception {
        mvc.perform(get("/api/eventos/no-es-numero")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/eventos").contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/eventos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Java\",\"fecha\":\"mañana\",\"lugar\":\"Aula\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/asistentes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ana\",\"correo\":\"ana@example.com\",\"campoDesconocido\":1}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void inicioYSaludSonPublicos() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("En funcionamiento"));
        mvc.perform(get("/actuator/health")).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void actualizarDatosConservaLaInscripcion() throws Exception {
        long e = crearEvento("Java");
        long a = crearAsistente("Ana", "ana@example.com");
        inscribir(e, a);
        mvc.perform(put("/api/eventos/{id}", e).contentType(MediaType.APPLICATION_JSON)
                .content(eventoJson("Java avanzado")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.asistentesIds", contains((int) a)));
        mvc.perform(put("/api/asistentes/{id}", a).contentType(MediaType.APPLICATION_JSON)
                .content(asistenteJson("Ana María", "ana@example.com")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.eventosIds", contains((int) e)));
    }

    private long crearEvento(String nombre) throws Exception {
        ResultActions respuesta = mvc.perform(post("/api/eventos").contentType(MediaType.APPLICATION_JSON)
            .content(eventoJson(nombre))).andExpect(status().isCreated());
        JsonNode body = mapper.readTree(respuesta.andReturn().getResponse().getContentAsString());
        long id = body.get("id").asLong();
        respuesta.andExpect(header().string("Location", "/api/eventos/" + id));
        return id;
    }

    private long crearAsistente(String nombre, String correo) throws Exception {
        ResultActions respuesta = mvc.perform(post("/api/asistentes").contentType(MediaType.APPLICATION_JSON)
            .content(asistenteJson(nombre, correo))).andExpect(status().isCreated());
        JsonNode body = mapper.readTree(respuesta.andReturn().getResponse().getContentAsString());
        long id = body.get("id").asLong();
        respuesta.andExpect(header().string("Location", "/api/asistentes/" + id));
        return id;
    }

    private void inscribir(long eventoId, long asistenteId) throws Exception {
        mvc.perform(post("/api/eventos/{e}/asistentes/{a}", eventoId, asistenteId))
            .andExpect(status().isCreated());
    }

    private String eventoJson(String nombre) {
        return "{\"nombre\":\"%s\",\"fecha\":\"2027-01-15T09:00:00\",\"lugar\":\"Auditorio\"}"
            .formatted(nombre);
    }

    private String asistenteJson(String nombre, String correo) {
        return "{\"nombre\":\"%s\",\"correo\":\"%s\",\"telefono\":\"3001234567\"}"
            .formatted(nombre, correo);
    }
}
