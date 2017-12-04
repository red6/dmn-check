package de.redsix.dmncheck;

import de.redsix.dmncheck.result.PrettyPrintValidationResults;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.result.ValidationResultType;
import de.redsix.dmncheck.validators.*;
import de.redsix.dmncheck.validators.core.GenericValidator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "check-dmn")
class CheckerMain extends AbstractMojo {

    private final static List<GenericValidator> validators = Arrays.asList(DuplicateRuleValidator.instance,
            InputExpressionTypeValidator.instance, OutputTypeValidator.instance, AggregationValidator.instance,
            AggregationOutputTypeValidator.instance, ConflictingRuleValidator.instance);

    @Parameter
    private String[] excludes;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final List<String> fileNames = getFileNames(".dmn", Paths.get(""));
        final List<File> collect = fileNames.stream().map(name -> new File(name)).collect(Collectors.toList());

        testFiles(collect);

    }

    void testFiles(final List<File> files) {
        for (File file : files) {
            if (getExcludeList().contains(file.getName())) {
                getLog().info("Skipped File: " + file);
            } else {
                final DmnModelInstance dmnModelInstance = Dmn.readModelFromFile(file);
                final Map<ModelElementInstance, Map<ValidationResultType, List<ValidationResult>>> validationResults = validators.stream()
                        .flatMap(validator -> (Stream<ValidationResult>) (validator.apply(dmnModelInstance)).stream()).
                                collect(Collectors.groupingBy(ValidationResult::getElement,
                                        Collectors.groupingBy(ValidationResult::getValidationResultType)));

                    if (!validationResults.isEmpty()) {
                        throw new AssertionError(PrettyPrintValidationResults.prettify(file, validationResults));
                    }
            }
        }
    }

    private List<String> getExcludeList() {
        if (excludes != null) {
            return Arrays.asList(excludes);
        } else {
            return new ArrayList<>();
        }
    }

    protected List<String> getFileNames(String suffix, Path dir) {
        try {
            return Files.walk(dir).filter(Files::isRegularFile).map(path -> path.toAbsolutePath().toString())
                    .filter(absolutePath -> absolutePath.endsWith(suffix)).collect(Collectors.toList());
        }
        catch (IOException e) {
            throw new RuntimeException("Could not determine DMN files.", e);
        }
    }

    void setExcludes(final String[] excludes) {
        this.excludes = excludes;
    }

}
