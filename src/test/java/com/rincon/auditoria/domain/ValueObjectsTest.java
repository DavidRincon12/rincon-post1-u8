package com.rincon.auditoria.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rincon.auditoria.domain.exception.DatosInvalidosException;
import com.rincon.auditoria.domain.model.HallazgoId;
import com.rincon.auditoria.domain.model.PlanRemediacion;
import com.rincon.auditoria.domain.model.Severidad;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ValueObjectsTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 3, 10);

    @Test
    void hallazgoId_nuevo_generaIdentificadoresDistintos() {
        assertThat(HallazgoId.nuevo()).isNotEqualTo(HallazgoId.nuevo());
    }

    @Test
    void hallazgoId_de_textoValido_conservaElValor() {
        UUID uuid = UUID.randomUUID();
        assertThat(HallazgoId.de("  " + uuid + " ").valor()).isEqualTo(uuid);
        assertThat(HallazgoId.de(uuid.toString())).hasToString(uuid.toString());
    }

    @Test
    void hallazgoId_de_textoInvalido_lanzaDatosInvalidos() {
        assertThatThrownBy(() -> HallazgoId.de("no-es-uuid")).isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> HallazgoId.de(" ")).isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> HallazgoId.de(null)).isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> new HallazgoId(null)).isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void planRemediacion_normalizaEspaciosDeLosTextos() {
        PlanRemediacion plan = new PlanRemediacion("  Ana Pérez ", " Aplicar parche de seguridad ", FECHA);
        assertThat(plan.responsable()).isEqualTo("Ana Pérez");
        assertThat(plan.accion()).isEqualTo("Aplicar parche de seguridad");
    }

    @Test
    void planRemediacion_conDatosFaltantes_lanzaDatosInvalidos() {
        assertThatThrownBy(() -> new PlanRemediacion(" ", "Aplicar parche de seguridad", FECHA))
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("responsable");
        assertThatThrownBy(() -> new PlanRemediacion("Ana", "corto", FECHA))
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("accion");
        assertThatThrownBy(() -> new PlanRemediacion("Ana Pérez", "Aplicar parche de seguridad", null))
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("fechaCompromiso");
    }

    @Test
    void severidad_plazosDecrecenConLaCriticidad() {
        assertThat(Severidad.BAJA.diasPlazoRemediacion()).isGreaterThan(Severidad.MEDIA.diasPlazoRemediacion());
        assertThat(Severidad.MEDIA.diasPlazoRemediacion()).isGreaterThan(Severidad.ALTA.diasPlazoRemediacion());
        assertThat(Severidad.ALTA.diasPlazoRemediacion()).isGreaterThan(Severidad.CRITICA.diasPlazoRemediacion());
    }
}
