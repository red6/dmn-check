package de.redsix.dmncheck;

import de.redsix.dmncheck.result.PrettyPrintValidationResults;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.ValidationResultType;
import de.redsix.dmncheck.validators.core.GenericValidator;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import de.redsix.dmncheck.validators.core.Validator;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "check-dmn", requiresProject = false)
class CheckerMain extends AbstractMojo {

    private static final String VALIDATOR_PACKAGE = "de.redsix.dmncheck.validators";

    @Parameter
    @SuppressWarnings("nullness")
    private String[] excludes;

    @Parameter
    @SuppressWarnings("nullness")
    private String[] searchPaths;

    @Parameter
    @SuppressWarnings("nullness")
    private String[] validators;

    @Override
    public void execute() throws MojoExecutionException {
        final List<Path> searchPathObjects = getSearchPathList().stream().map(Paths::get).collect(Collectors.toList());
        final List<File> filesToTest = fetchFilesToTestFromSearchPaths(searchPathObjects);

        testFiles(filesToTest);
    }

    void testFiles(final List<File> files) throws MojoExecutionException {
        boolean encounteredError = false;
        for (File file : files) {
            encounteredError |= testFile(file);
        }

        if (encounteredError) {
            throw new MojoExecutionException("Some files are not valid, see previous logs.");
        }
    }

    private boolean testFile(final File file) {
        boolean encounteredError = false;

        try {
            final DmnModelInstance dmnModelInstance = Dmn.readModelFromFile(file);
            final List<ValidationResult> validationResults = runValidators(dmnModelInstance);

            if (!validationResults.isEmpty()) {
                PrettyPrintValidationResults.logPrettified(file, validationResults, getLog());
                encounteredError = validationResults.stream()
                        .anyMatch(result -> ValidationResultType.ERROR.equals(result.getValidationResultType()));
            }
        }
        catch (Exception e) {
            getLog().error(e);
            encounteredError = true;
        }

        return encounteredError;
    }

    private List<ValidationResult> runValidators(final DmnModelInstance dmnModelInstance) {
        return getValidators().stream()
                .flatMap(validator -> validator.apply(dmnModelInstance).stream())
                .collect(Collectors.toList());
    }

    List<File> fetchFilesToTestFromSearchPaths(final List<Path> searchPaths) {
        final List<String> fileNames = getFileNames(".dmn", searchPaths);
        final List<File> files = fileNames.stream().map(File::new).collect(Collectors.toList());
        return files.stream().filter(file -> {
            if (getExcludeList().contains(file.getName())) {
                getLog().info("Skipped File: " + file);
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.toList());
    }

    List<String> getFileNames(final String suffix, final List<Path> dirs) {
        return dirs.stream().flatMap(dir -> {
            try {
                return Files.walk(dir)
                        .filter(Files::isRegularFile)
                        .map(path -> path.toAbsolutePath().toString())
                        .filter(absolutePath -> absolutePath.endsWith(suffix));
            } catch (IOException e) {
                throw new RuntimeException("Could not determine DMN files.", e);
            }
        }).collect(Collectors.toList());
    }

    private List<String> getExcludeList() {
        if (excludes != null) {
            return Arrays.asList(excludes);
        } else {
            return new ArrayList<>();
        }
    }

    private List<String> getSearchPathList() {
        if (searchPaths != null) {
            return Arrays.asList(searchPaths);
        } else {
            return Collections.singletonList("");
        }
    }

    private List<Validator> getValidators() {
        final String[] scanSpec;
        if (validators != null) {
            scanSpec = validators;
        } else {
            scanSpec = new String[] {VALIDATOR_PACKAGE};
        }

        final List<Class<? extends Validator>> validatorClasses = new ArrayList<>();
        new FastClasspathScanner(scanSpec)
                .disableRecursiveScanning()
                .strictWhitelist()
                .matchSubclassesOf(GenericValidator.class, validatorClasses::add)
                .matchSubclassesOf(SimpleValidator.class, validatorClasses::add)
                .matchClassesImplementing(Validator.class, validatorClasses::add)
                .scan();
        return validatorClasses.stream()
                .filter(validatorClass -> !Modifier.isAbstract(validatorClass.getModifiers()))
                .filter(validatorClass -> !Modifier.isInterface(validatorClass.getModifiers()))
                .map(this::instantiateValidator)
                .collect(Collectors.toList());
    }

    private Validator instantiateValidator(final Class<? extends Validator> validator) {
        try {
            return validator.newInstance();
        }
        catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to load validator " + validator, e);
        }
    }

    void setExcludes(final String[] excludes) {
        this.excludes = excludes;
    }

    void setSearchPaths(final String[] searchPaths) {
        this.searchPaths = searchPaths;
    }

    void setValidators(final String[] validators) {
        this.validators = validators;
    }

}
