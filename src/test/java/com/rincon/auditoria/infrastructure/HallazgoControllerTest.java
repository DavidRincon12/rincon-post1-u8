package com.rincon.auditoria.infrastructure;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rincon.auditoria.infrastructure.web.HallazgoController;
import com.rincon.auditoria.infrastructure.web.ManejadorGlobalExcepciones;
import com.rincon.auditoria.soporte.Fakes.HistorialEnMemoria;
import com.rincon.auditoria.soporte.Fakes.RelojMutable;
import com.rincon.auditoria.soporte.Fakes.RepositorioEnMemoria;
import com.rincon.auditoria.soporte.Fakes.UnidadDeTrabajoDirecta;
import com.rincon.auditoria.usecase.service.CerrarHallazgoService;
import com.rincon.auditoria.usecase.service.ConsultarHallazgosService;
import com.rincon.auditoria.usecase.service.ConsultarHistorialService;
import com.rincon.auditoria.usecase.service.EnviarAVerificacionService;
import com.rincon.auditoria.usecase.service.IniciarRemediacionService;
import com.rincon.auditoria.usecase.service.ObtenerDashboardAuditoriaService;
import com.rincon.auditoria.usecase.service.RechazarVerificacionService;
import com.rincon.auditoria.usecase.service.RegistrarHallazgoService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Prueba del adaptador web con MockMvc en modo standalone: no levanta el contexto de Spring
 * y los casos de uso reales trabajan sobre puertos en memoria.
 */
class HallazgoControllerTest {

    private static final String BASE = "/api/v1/hallazgos";
    private static final ObjectMapper JSON = new ObjectMapper();

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
        HistorialEnMemoria historial = new HistorialEnMemoria();
        UnidadDeTrabajoDirecta uow = new UnidadDeTrabajoDirecta();
        RelojMutable reloj = new RelojMutable(Instant.now());
        HallazgoController controller = new HallazgoController(
                new RegistrarHallazgoService(repositorio, historial, uow, reloj),
                new IniciarRemediacionService(repositorio, historial, uow, reloj),
                new EnviarAVerificacionService(repositorio, historial, uow, reloj),
                new RechazarVerificacionService(repositorio, historial, uow, reloj),
                new CerrarHallazgoService(repositorio, historial, uow, reloj),
                new ConsultarHallazgosService(repositorio),
                new ConsultarHistorialService(repositorio, historial),
                new ObtenerDashboardAuditoriaService(repositorio));
        mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ManejadorGlobalExcepciones()).build();
    }

    private String registrarHallazgo() throws Exception {
        String respuesta = mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("""
                        {"titulo":"Backups sin cifrar","descripcion":"Las copias se guardan sin cifrado","severidad":"ALTA"}"""))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(BASE + "/")))
                .andReturn().getResponse().getContentAsString();
        JsonNode nodo = JSON.readTree(respuesta);
        return nodo.get("id").asText();
    }

    @Test
    void registrar_devuelve201ConElHallazgoAbierto() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("""
                        {"titulo":"Backups sin cifrar","descripcion":"Las copias se guardan sin cifrado","severidad":"ALTA"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ABIERTO"))
                .andExpect(jsonPath("$.severidad").value("ALTA"));
    }

    @Test
    void cerrarUnHallazgoAbierto_devuelve400PorTransicionInvalida() throws Exception {
        String id = registrarHallazgo();

        mvc.perform(post(BASE + "/" + id + "/cierre").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comentarioCierre\":\"Cierre prematuro\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Transición de estado inválida"))
                .andExpect(jsonPath("$.estadoOrigen").value("ABIERTO"))
                .andExpect(jsonPath("$.estadoDestino").value("CERRADO"));
    }

    @Test
    void planFueraDelSla_devuelve400() throws Exception {
        String id = registrarHallazgo();
        String fecha = LocalDate.now(ZoneOffset.UTC).plusDays(60).toString();

        mvc.perform(post(BASE + "/" + id + "/remediacion").contentType(MediaType.APPLICATION_JSON).content(
                        "{\"responsable\":\"Luis Gómez\",\"accion\":\"Activar cifrado en el almacenamiento\",\"fechaCompromiso\":\"" + fecha + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("ALTA")));
    }

    @Test
    void hallazgoInexistente_devuelve404() throws Exception {
        mvc.perform(get(BASE + "/2f4c1f0e-6a7b-4c1d-9c3e-0a1b2c3d4e5f"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Hallazgo no encontrado"));
    }

    @Test
    void identificadorMalFormado_devuelve400() throws Exception {
        mvc.perform(get(BASE + "/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void cuerpoConCamposVacios_devuelve400ConErroresPorCampo() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"descripcion\":\"\",\"severidad\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.titulo").exists())
                .andExpect(jsonPath("$.errors.severidad").exists());
    }

    @Test
    void jsonConEnumInvalido_devuelve400() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(
                        "{\"titulo\":\"Backups sin cifrar\",\"descripcion\":\"Las copias se guardan sin cifrado\",\"severidad\":\"EXTREMA\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filtroPorEstadoInvalido_devuelve400() throws Exception {
        mvc.perform(get(BASE).param("estado", "INEXISTENTE")).andExpect(status().isBadRequest());
    }

    @Test
    void historial_devuelveLosCambiosEnOrden() throws Exception {
        String id = registrarHallazgo();
        String fecha = LocalDate.now(ZoneOffset.UTC).plusDays(5).toString();
        mvc.perform(post(BASE + "/" + id + "/remediacion").contentType(MediaType.APPLICATION_JSON).content(
                        "{\"responsable\":\"Luis Gómez\",\"accion\":\"Activar cifrado en el almacenamiento\",\"fechaCompromiso\":\"" + fecha + "\"}"))
                .andExpect(status().isOk());

        mvc.perform(get(BASE + "/" + id + "/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].estadoNuevo").value("ABIERTO"))
                .andExpect(jsonPath("$[1].estadoAnterior").value("ABIERTO"))
                .andExpect(jsonPath("$[1].estadoNuevo").value("EN_REMEDIACION"));
    }

    @Test
    void historialDeHallazgoInexistente_devuelve404() throws Exception {
        mvc.perform(get(BASE + "/2f4c1f0e-6a7b-4c1d-9c3e-0a1b2c3d4e5f/historial")).andExpect(status().isNotFound());
    }

    @Test
    void dashboard_seResuelveComoRutaLiteralYNoComoIdentificador() throws Exception {
        registrarHallazgo();

        mvc.perform(get(BASE + "/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHallazgos").value(1))
                .andExpect(jsonPath("$.porEstado.ABIERTO").value(1))
                .andExpect(jsonPath("$.porSeveridad.ALTA").value(1))
                .andExpect(jsonPath("$.tiempoPromedioCierre", hasSize(4)));
    }
}
