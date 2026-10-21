package com.rincon.auditoria.domain.exception;

/** Se lanza cuando el plan de remediación incumple el plazo que exige la severidad del hallazgo. */
public class PlanRemediacionInvalidoException extends DomainException {

    private static final long serialVersionUID = 1L;

    public PlanRemediacionInvalidoException(String mensaje) {
        super(mensaje);
    }
}
