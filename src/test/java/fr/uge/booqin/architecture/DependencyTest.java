package fr.uge.booqin.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.INTERFACES;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class DependencyTest {
    @Test
    public void infraDoesNotDependOnApp() {
        var classes = new ClassFileImporter().importPackages("fr.uge.booqin");
        var rule = noClasses()
                .that().resideInAPackage("..infra..")
                .and().resideOutsideOfPackage("..fixtures..")
                .should().dependOnClassesThat().resideInAPackage("..app..");
        rule.check(classes);
    }

    @Test
    public void domainDoesNotDependOnApp() {
        var classes = new ClassFileImporter().importPackages("fr.uge.booqin");
        var rule = noClasses()
                .that().resideInAPackage("..domain..")
                .and().resideOutsideOfPackage("..fixtures..")
                .should().dependOnClassesThat().resideInAPackage("..app..");
        rule.check(classes);
    }

    @Test
    public void infraDoesNotDependOnConcreteClassOfDomain() {
        var classes = new ClassFileImporter().importPackages("fr.uge.booqin");
        var rule = noClasses()
                .that().resideInAPackage("..infra..")
                .and().resideOutsideOfPackage("..fixtures..")
                .and().resideOutsideOfPackage("..infra.config..")
                .and().resideOutsideOfPackage("..infra.persistence..")
                .should().dependOnClassesThat(
                        JavaClass.Predicates.resideInAPackage("..domain..")
                                .and(are(not(INTERFACES)))
                );
        rule.check(classes);
    }

    @Test
    public void domainDoesNotDependOnInfra() {
        var classes = new ClassFileImporter().importPackages("fr.uge.booqin");
        var rule = noClasses()
                .that().resideInAPackage("..domain..")
                .and().resideOutsideOfPackage("..fixtures..")
                .should().dependOnClassesThat().resideInAPackage("..infra..");
        rule.check(classes);
    }
}
