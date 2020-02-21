package de.redsix.dmncheck;

import de.redsix.dmncheck.result.PrettyPrintValidationResults;
import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.ProjectClassLoader;
import de.redsix.dmncheck.util.ValidatorLoader;
import de.redsix.dmncheck.validators.core.Validator;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mojo(name = "check-dmn", requiresProject = false, requiresDependencyResolution = ResolutionScope.TEST)
class CheckerMain extends AbstractMojo {

    @Parameter
    @SuppressWarnings("nullness")
    private String[] excludes;

    @Parameter
    @SuppressWarnings("nullness")
    private String[] searchPaths;

    @Parameter
    @SuppressWarnings("nullness")
    private String[] validatorPackages;

    @Parameter
    @SuppressWarnings("nullness")
    private String[] validatorClasses;

    @Parameter( defaultValue = "${project}", readonly = true )
    @SuppressWarnings("nullness")
    private MavenProject project;

    private @MonotonicNonNull List<Validator> validators;

    @Override
    public void execute() throws MojoExecutionException {
        loadProjectclasspath();

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
                        .anyMatch(result -> Severity.ERROR.equals(result.getSeverity()));
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
        if (validators != null) {
            return validators;
        }

        validators = ValidatorLoader.getValidators(validatorPackages, validatorClasses);

        return validators;
    }

    private void loadProjectclasspath() throws MojoExecutionException {
        final List<URL> listUrl = new ArrayList<>();

        Set<Artifact> deps = project.getArtifacts();
        for (Artifact artifact : deps) {
            final URL url;
            try {
                url = artifact.getFile().toURI().toURL();
                listUrl.add(url);
            }
            catch (MalformedURLException e) {
                throw new MojoExecutionException("Failed to construct project class loader.");
            }
        }

        ProjectClassLoader.instance.classLoader = new URLClassLoader(listUrl.toArray(new URL[0]));
    }

    void setExcludes(final String[] excludes) {
        this.excludes = excludes;
    }

    void setSearchPaths(final String[] searchPaths) {
        this.searchPaths = searchPaths;
    }

    void setValidatorPackages(String[] validatorPackages) {
        this.validatorPackages = validatorPackages;
    }

    void setValidatorClasses(String[] validatorClasses) {
        this.validatorClasses = validatorClasses;
    }

    void setProject(MavenProject project) {
        this.project = project;
    }
}
