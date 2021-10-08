package de.redsix.dmncheck;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

class CheckerMainTest {

    final private CheckerMain testee = new CheckerMain();

    @BeforeEach
    void setUp() {
        final MavenProject mavenProject = new MavenProject();
        mavenProject.setArtifacts(Collections.emptySet());
        testee.setProject(mavenProject);
    }

    @Test
    void shouldSkipFileIfItsExcluded() {
        final String ignoredFilename = "empty-as-well.dmn";
        testee.setExcludes(new String[] { ignoredFilename });
        final List<File> filesToTest = testee.fetchFilesToTestFromSearchPaths(Collections.singletonList(Paths.get("")));

        Assertions.assertTrue(filesToTest.stream().noneMatch(file -> file.getName().equals(ignoredFilename)));
    }

    @Test
    void shouldSkipFileIfIsNotOnSearchPath() {
        final List<File> filesToTest = testee.fetchFilesToTestFromSearchPaths(Collections.singletonList(Paths.get("src/main/java")));
        Assertions.assertTrue(filesToTest.isEmpty());
    }

    @Test
    void shouldDetectIfFileIsOnSearchPath() {
        testee.setSearchPaths(new String[] {"src/"});
        final MojoExecutionException assertionError = Assertions.assertThrows(MojoExecutionException.class, testee::execute);
        Assertions.assertTrue(assertionError.getMessage().contains("Some files are not valid, see previous logs."));
    }

    @Test
    void shouldDetectIfFileIsOnSearchPathWithMultiplePaths() {
        testee.setSearchPaths(new String[] {"src/main/java","src/"});
        final MojoExecutionException assertionError = Assertions.assertThrows(MojoExecutionException.class, testee::execute);
        Assertions.assertTrue(assertionError.getMessage().contains("Some files are not valid, see previous logs."));
    }

}
