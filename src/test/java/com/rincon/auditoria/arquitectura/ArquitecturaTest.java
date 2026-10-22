package com.rincon.auditoria.arquitectura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Convierte la regla de dependencias de Clean Architecture en una prueba que falla si alguien la rompe. */
@AnalyzeClasses(packages = "com.rincon.auditoria", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaTest {

    private static final String[] FRAMEWORKS = {
            "org.springframework..", "jakarta..", "javax..", "org.hibernate..", "com.fasterxml.."};

    @ArchTest
    static final ArchRule dominioNoDependeDeFrameworks = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(FRAMEWORKS)
            .because("el círculo del dominio debe ser Java puro");

    @ArchTest
    static final ArchRule casosDeUsoNoDependenDeFrameworks = noClasses().that().resideInAPackage("..usecase..")
            .should().dependOnClassesThat().resideInAnyPackage(FRAMEWORKS)
            .because("los casos de uso solo conocen el dominio y sus propios puertos");

    @ArchTest
    static final ArchRule dominioNoConoceCircuitosExternos = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..usecase..", "..infrastructure..");

    @ArchTest
    static final ArchRule casosDeUsoNoConocenLaInfraestructura = noClasses().that().resideInAPackage("..usecase..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..");

    @ArchTest
    static final ArchRule persistenciaYWebNoSeConocenEntreSi = noClasses().that().resideInAPackage("..infrastructure.web..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure.persistence..");

    @ArchTest
    static final ArchRule sinCiclosEntreCapas = slices().matching("com.rincon.auditoria.(*)..").should().beFreeOfCycles();
}
