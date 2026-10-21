package com.rincon.auditoria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de arranque. Vive en la raíz del paquete para que el escaneo de componentes
 * alcance los adaptadores; el dominio y los casos de uso no dependen de esta clase.
 */
@SpringBootApplication
public class AuditoriaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditoriaApplication.class, args);
    }
}
