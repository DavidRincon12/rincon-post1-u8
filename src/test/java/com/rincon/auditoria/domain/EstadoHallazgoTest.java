package com.rincon.auditoria.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rincon.auditoria.domain.exception.TransicionInvalidaException;
import com.rincon.auditoria.domain.model.EstadoHallazgo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EstadoHallazgoTest {

    @ParameterizedTest(name = "{0} -> {1} permitida = {2}")
    @CsvSource({
            "ABIERTO,ABIERTO,false", "ABIERTO,EN_REMEDIACION,true", "ABIERTO,EN_VERIFICACION,false", "ABIERTO,CERRADO,false",
            "EN_REMEDIACION,ABIERTO,false", "EN_REMEDIACION,EN_REMEDIACION,false",
            "EN_REMEDIACION,EN_VERIFICACION,true", "EN_REMEDIACION,CERRADO,false",
            "EN_VERIFICACION,ABIERTO,false", "EN_VERIFICACION,EN_REMEDIACION,true",
            "EN_VERIFICACION,EN_VERIFICACION,false", "EN_VERIFICACION,CERRADO,true",
            "CERRADO,ABIERTO,false", "CERRADO,EN_REMEDIACION,false",
            "CERRADO,EN_VERIFICACION,false", "CERRADO,CERRADO,false"
    })
    void puedeTransicionarA_respetaLaTablaDeTransiciones(EstadoHallazgo origen, EstadoHallazgo destino, boolean esperado) {
        assertThat(origen.puedeTransicionarA(destino)).isEqualTo(esperado);
    }

    @Test
    void puedeTransicionarA_conDestinoNulo_esFalso() {
        assertThat(EstadoHallazgo.ABIERTO.puedeTransicionarA(null)).isFalse();
    }

    @Test
    void validarTransicionA_transicionProhibida_lanzaExcepcionConOrigenYDestino() {
        assertThatThrownBy(() -> EstadoHallazgo.ABIERTO.validarTransicionA(EstadoHallazgo.CERRADO))
                .isInstanceOfSatisfying(TransicionInvalidaException.class, e -> {
                    assertThat(e.getOrigen()).isEqualTo(EstadoHallazgo.ABIERTO);
                    assertThat(e.getDestino()).isEqualTo(EstadoHallazgo.CERRADO);
                    assertThat(e.getMessage()).contains("ABIERTO", "CERRADO", "EN_REMEDIACION");
                });
    }

    @Test
    void validarTransicionA_transicionPermitida_noLanzaNada() {
        EstadoHallazgo.EN_VERIFICACION.validarTransicionA(EstadoHallazgo.CERRADO);
    }

    @Test
    void esTerminal_soloCerrado() {
        assertThat(EstadoHallazgo.CERRADO.esTerminal()).isTrue();
        assertThat(EstadoHallazgo.ABIERTO.esTerminal()).isFalse();
        assertThat(EstadoHallazgo.EN_REMEDIACION.esTerminal()).isFalse();
        assertThat(EstadoHallazgo.EN_VERIFICACION.esTerminal()).isFalse();
    }

    @Test
    void transicionesPermitidas_noSePuedeModificarDesdeFuera() {
        assertThatThrownBy(() -> EstadoHallazgo.ABIERTO.transicionesPermitidas().add(EstadoHallazgo.CERRADO))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
