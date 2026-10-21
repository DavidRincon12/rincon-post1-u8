package com.rincon.auditoria.infrastructure.config;

import com.rincon.auditoria.usecase.port.in.CerrarHallazgoUseCase;
import com.rincon.auditoria.usecase.port.in.ConsultarHallazgosUseCase;
import com.rincon.auditoria.usecase.port.in.EnviarAVerificacionUseCase;
import com.rincon.auditoria.usecase.port.in.IniciarRemediacionUseCase;
import com.rincon.auditoria.usecase.port.in.RechazarVerificacionUseCase;
import com.rincon.auditoria.usecase.port.in.RegistrarHallazgoUseCase;
import com.rincon.auditoria.usecase.port.out.HallazgoRepositoryPort;
import com.rincon.auditoria.usecase.port.out.UnidadDeTrabajoPort;
import com.rincon.auditoria.usecase.service.CerrarHallazgoService;
import com.rincon.auditoria.usecase.service.ConsultarHallazgosService;
import com.rincon.auditoria.usecase.service.EnviarAVerificacionService;
import com.rincon.auditoria.usecase.service.IniciarRemediacionService;
import com.rincon.auditoria.usecase.service.RechazarVerificacionService;
import com.rincon.auditoria.usecase.service.RegistrarHallazgoService;
import java.time.Clock;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Raíz de composición: único lugar donde se conectan los casos de uso con sus adaptadores.
 * Los servicios no llevan anotaciones de Spring; se registran aquí como beans.
 */
@Configuration
public class AuditoriaConfiguration {

    @Bean
    public Clock reloj() {
        return Clock.systemUTC();
    }

    @Bean
    public UnidadDeTrabajoPort unidadDeTrabajo(PlatformTransactionManager transactionManager) {
        TransactionTemplate plantilla = new TransactionTemplate(transactionManager);
        return new UnidadDeTrabajoPort() {
            @Override
            public <T> T ejecutar(Supplier<T> operacion) {
                return plantilla.execute(estado -> operacion.get());
            }
        };
    }

    @Bean
    public RegistrarHallazgoUseCase registrarHallazgoUseCase(HallazgoRepositoryPort repositorio,
                                                             UnidadDeTrabajoPort unidadDeTrabajo, Clock reloj) {
        return new RegistrarHallazgoService(repositorio, unidadDeTrabajo, reloj);
    }

    @Bean
    public IniciarRemediacionUseCase iniciarRemediacionUseCase(HallazgoRepositoryPort repositorio,
                                                               UnidadDeTrabajoPort unidadDeTrabajo, Clock reloj) {
        return new IniciarRemediacionService(repositorio, unidadDeTrabajo, reloj);
    }

    @Bean
    public EnviarAVerificacionUseCase enviarAVerificacionUseCase(HallazgoRepositoryPort repositorio,
                                                                 UnidadDeTrabajoPort unidadDeTrabajo, Clock reloj) {
        return new EnviarAVerificacionService(repositorio, unidadDeTrabajo, reloj);
    }

    @Bean
    public RechazarVerificacionUseCase rechazarVerificacionUseCase(HallazgoRepositoryPort repositorio,
                                                                   UnidadDeTrabajoPort unidadDeTrabajo, Clock reloj) {
        return new RechazarVerificacionService(repositorio, unidadDeTrabajo, reloj);
    }

    @Bean
    public CerrarHallazgoUseCase cerrarHallazgoUseCase(HallazgoRepositoryPort repositorio,
                                                       UnidadDeTrabajoPort unidadDeTrabajo, Clock reloj) {
        return new CerrarHallazgoService(repositorio, unidadDeTrabajo, reloj);
    }

    @Bean
    public ConsultarHallazgosUseCase consultarHallazgosUseCase(HallazgoRepositoryPort repositorio) {
        return new ConsultarHallazgosService(repositorio);
    }
}
