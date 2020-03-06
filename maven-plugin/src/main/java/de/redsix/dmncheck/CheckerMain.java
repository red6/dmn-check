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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Mojo(name = "check-dmn", requiresProject = false, requiresDependencyResolution = ResolutionScope.TEST)
class CheckerMain extends AbstractMojo implements PluginBase {

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

    @Override
    public void execute() throws MojoExecutionException {
        loadProjectclasspath();
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
    public void loadProjectclasspath() throws MojoExecutionException {
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
