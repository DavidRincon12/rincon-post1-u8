package com.rincon.auditoria.domain.model;

/**
 * Criticidad del hallazgo. Cada nivel fija el plazo máximo (en días) que se acepta
 * para comprometer la remediación, de modo que el SLA es una regla del dominio y no de la API.
 */
public enum Severidad {
    BAJA(90),
    MEDIA(30),
    ALTA(15),
    CRITICA(7);

    private final int diasPlazoRemediacion;

    Severidad(int diasPlazoRemediacion) {
        this.diasPlazoRemediacion = diasPlazoRemediacion;
    }

    public int diasPlazoRemediacion() {
        return diasPlazoRemediacion;
    }
}
