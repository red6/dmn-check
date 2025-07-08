package de.redsix.dmncheck.util;

import de.redsix.dmncheck.validators.core.Validator;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ValidatorLoader {

    private static final String VALIDATOR_PACKAGE =
        "de.redsix.dmncheck.validators";
    private static final String VALIDATOR_CORE_PACKAGE =
        "de.redsix.dmncheck.validators.core";

    private static int inputParameterHash;
    private static @MonotonicNonNull List<Validator> validators;

    private ValidatorLoader() {}

    public static List<Validator> getValidators() {
        return getValidators(null, null);
    }

    public static List<Validator> getValidators(
        final String@Nullable [] packages,
        final String@Nullable [] classes
    ) {
        if (
            inputParameterHash ==
                Objects.hash(
                    Arrays.hashCode(packages),
                    Arrays.hashCode(classes)
                ) &&
            validators != null
        ) {
            return validators;
        }

        inputParameterHash = Objects.hash(
            Arrays.hashCode(packages),
            Arrays.hashCode(classes)
        );

        try (
            ScanResult scanResult = new ClassGraph()
                .acceptClasses(Validator.class.getName())
                .acceptPackages(VALIDATOR_CORE_PACKAGE)
                .acceptPackagesNonRecursive(
                    packages == null
                        ? new String[] {
                            VALIDATOR_PACKAGE,
                            VALIDATOR_CORE_PACKAGE,
                        }
                        : packages
                )
                .acceptClasses(classes == null ? new String[] {} : classes)
                .scan();
        ) {
            final ClassInfoList allValidatorClasses =
                scanResult.getClassesImplementing(Validator.class.getName());

            validators = allValidatorClasses
                .loadClasses(Validator.class)
                .stream()
                .filter(validatorClass ->
                    !Modifier.isAbstract(validatorClass.getModifiers())
                )
                .filter(validatorClass ->
                    !Modifier.isInterface(validatorClass.getModifiers())
                )
                .map(ValidatorLoader::instantiateValidator)
                .toList();

            return validators;
        }
    }

    private static Validator instantiateValidator(
        final Class<? extends Validator> validator
    ) {
        try {
            return validator.getDeclaredConstructor().newInstance();
        } catch (
            IllegalAccessException
            | InstantiationException
            | NoSuchMethodException
            | InvocationTargetException e
        ) {
            throw new RuntimeException(
                "Failed to load validator " + validator,
                e
            );
        }
    }
}
