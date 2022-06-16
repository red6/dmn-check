package de.redsix.dmncheck;

import de.redsix.dmncheck.plugin.PluginBase;
import de.redsix.dmncheck.plugin.PrettyPrintValidationResults;
import de.redsix.dmncheck.util.ProjectClassLoader;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

@Mojo(name = "check-dmn", requiresProject = false, requiresDependencyResolution = ResolutionScope.TEST)
public class CheckerMain extends AbstractMojo implements PluginBase {

    @Parameter
    @SuppressWarnings("nullness")
    String[] excludes;

    @Parameter
    @SuppressWarnings("nullness")
    String[] searchPaths;

    @Parameter
    @SuppressWarnings("nullness")
    String[] validatorPackages;

    @Parameter
    @SuppressWarnings("nullness")
    String[] validatorClasses;

    @Parameter( defaultValue = "${project}", readonly = true )
    @SuppressWarnings("nullness")
    MavenProject project;

    @Parameter( defaultValue = "false", readonly = true )
    @SuppressWarnings("nullness")
    Boolean failOnWarning;

    @Parameter(defaultValue = "${classpaths}")
    @SuppressWarnings("nullness")
    String[] classpath;

    @Override
    public void execute() throws MojoExecutionException {
        loadClasspath();
        if(validate()) {
            throw new MojoExecutionException("Some files are not valid, see previous logs.");
        }
    }

    @Override
    public PrettyPrintValidationResults.PluginLogger getPluginLogger() {
        return new PrettyPrintValidationResults.PluginLogger(getLog()::info, getLog()::warn, getLog()::error);
    }

    @Override
    public List<String> getExcludeList() {
        if (excludes != null) {
            return Arrays.asList(excludes);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getSearchPathList() {
        if (searchPaths != null) {
            return Arrays.asList(searchPaths);
        } else {
            return Collections.singletonList("");
        }
    }

    @Override
    public String[] getValidatorPackages() {
        return validatorPackages;
    }

    @Override
    public String[] getValidatorClasses() {
        return validatorClasses;
    }

    @Override
    public boolean failOnWarning() {
        return this.failOnWarning;
    }

    void loadClasspath() throws MojoExecutionException {
        if (classpath != null && classpath.length != 0) {
            loadProjectClasspath(Arrays.asList(classpath));
        } else {
            loadProjectclasspath();
        }
    }

    void loadProjectclasspath() throws MojoExecutionException {
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

        ProjectClassLoader.INSTANCE.classLoader = new URLClassLoader(listUrl.toArray(new URL[0]));
    }
}
