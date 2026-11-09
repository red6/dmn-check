package de.redsix.dmncheck;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import de.redsix.dmncheck.validators.core.Validator;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("de.redsix.dmncheck");

    @Test
    void validatorsCanBeLoadedByValidatorLoader() {
        // ValidatorLoader scans these packages non-recursively and instantiates validators via reflection
        classes()
                .that()
                .implement(Validator.class)
                .and()
                .doNotHaveModifier(JavaModifier.ABSTRACT)
                .should()
                .resideInAnyPackage("de.redsix.dmncheck.validators", "de.redsix.dmncheck.validators.core")
                .andShould()
                .bePublic()
                .andShould(haveAPublicNoArgConstructor())
                .check(CLASSES);
    }

    @Test
    void feelDoesNotDependOnValidators() {
        noClasses()
                .that()
                .resideInAPackage("de.redsix.dmncheck.feel..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("de.redsix.dmncheck.validators")
                .check(CLASSES);
    }

    @Test
    void packagesAreFreeOfCycles() {
        // One slice per package, as validators.core lives in the core module and must not merge with validators
        slices().matching("de.redsix.dmncheck.(**)").should().beFreeOfCycles().check(CLASSES);
    }

    private static ArchCondition<JavaClass> haveAPublicNoArgConstructor() {
        return new ArchCondition<>("have a public no-arg constructor") {
            @Override
            public void check(final JavaClass javaClass, final ConditionEvents events) {
                final boolean satisfied = javaClass
                        .tryGetConstructor()
                        .filter(constructor -> constructor.getModifiers().contains(JavaModifier.PUBLIC))
                        .isPresent();
                events.add(new SimpleConditionEvent(
                        javaClass, satisfied, javaClass.getName() + " has no public no-arg constructor"));
            }
        };
    }
}
