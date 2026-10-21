package com.rincon.auditoria.usecase.port.out;

import java.util.function.Supplier;

/**
 * Unit of Work: delimita la transacción sin que el caso de uso conozca el framework.
 * Todo lo que se ejecute dentro se confirma o se revierte en bloque.
 */
public interface UnidadDeTrabajoPort {

    <T> T ejecutar(Supplier<T> operacion);
}
