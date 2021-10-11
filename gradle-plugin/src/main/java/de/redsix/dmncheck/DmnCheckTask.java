package de.redsix.dmncheck;

import de.redsix.dmncheck.plugin.PluginBase;
import de.redsix.dmncheck.plugin.PrettyPrintValidationResults;
import de.redsix.dmncheck.util.ProjectClassLoader;
import de.redsix.dmncheck.validators.core.Validator;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.*;
import org.gradle.internal.impldep.org.junit.Ignore;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DmnCheckTask extends DefaultTask implements PluginBase, VerificationTask {

    public static final String MSG_FAILED_TO_CONSTRUCT_PROJECT_CLASS_LOADER = "Failed to construct project class loader.";
    public static final String MSG_SOME_FILES_ARE_NOT_VALID_SEE_PREVIOUS_LOGS = "Some files are not valid, see previous logs.";

    private boolean ignoreFailures = false;

    @TaskAction
    public void validateFiles() {
        loadProjectclasspath();

        if (validate()) {
            if (ignoreFailures) {
                getLogger().error(MSG_SOME_FILES_ARE_NOT_VALID_SEE_PREVIOUS_LOGS);
            } else {
                throw new GradleException(MSG_SOME_FILES_ARE_NOT_VALID_SEE_PREVIOUS_LOGS);
            }
        }
    }

    @Override
    @Internal
    public PrettyPrintValidationResults.PluginLogger getPluginLogger() {
        return new PrettyPrintValidationResults.PluginLogger(c -> getLogger().info((String) c), c -> getLogger().warn((String) c),
                                                             c -> getLogger().error((String) c));
    }

    @Input
    @Optional
    @Override
    public List<String> getExcludeList() {
        return Objects.requireNonNullElse(getDmnCheckExtension().excludeList, Collections.emptyList());
    }

    @Input
    @Optional
    @Override
    public List<String> getSearchPathList() {
        return Objects.requireNonNullElseGet(getDmnCheckExtension().searchPathList, () -> Collections.singletonList(""));
    }

    @Input
    @Optional
    @Override
    public String[] getValidatorPackages() {
        if (getDmnCheckExtension().validatorPackages != null) {
            return getDmnCheckExtension().validatorPackages.toArray(new String[] {});
        } else {
            return null;
        }
    }

    @Input
    @Optional
    @Override
    public String[] getValidatorClasses() {
        if (getDmnCheckExtension().validatorClasses != null) {
            return getDmnCheckExtension().validatorClasses.toArray(new String[] {});
        } else {
            return null;
        }
    }

    public boolean failOnWarning() {
        return false;
    }

    public void loadProjectclasspath() {
        final Set<File> files = getProject().getConfigurations().getByName("compileClasspath").getFiles();

        final URL[] classpathURLs = files.stream()
                                         .map(File::toURI)
                                         .map(uri -> {
            try {
                return uri.toURL();
            }
            catch (MalformedURLException e) {
                if (ignoreFailures) {
                    getLogger().error(MSG_FAILED_TO_CONSTRUCT_PROJECT_CLASS_LOADER);
                    return null;
                } else {
                    throw new GradleException(MSG_FAILED_TO_CONSTRUCT_PROJECT_CLASS_LOADER);
                }
            }
        }).filter(Objects::nonNull).toArray(URL[]::new);
        ProjectClassLoader.INSTANCE.classLoader = new URLClassLoader(classpathURLs);
    }

    private DmnCheckExtension getDmnCheckExtension() {
        return (DmnCheckExtension) getExtensions().getByName(DmnCheckGradlePlugin.DMN_CHECK_EXTENSION);
    }

    @Override
    public void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures;
    }

    @Input
    @Override
    public boolean getIgnoreFailures() {
        return ignoreFailures;
    }

    @Internal
    @Override
    public List<Validator> getValidators()  {
        return PluginBase.super.getValidators();
    }
}
