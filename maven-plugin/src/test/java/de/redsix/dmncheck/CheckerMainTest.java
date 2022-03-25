package de.redsix.dmncheck;

import de.redsix.dmncheck.util.ProjectClassLoader;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

class CheckerMainTest {

    final private CheckerMain testee = new CheckerMain();

    @Nested
    class TestsWithNoArtifacts {

        @BeforeEach
        void setUp() {
            final MavenProject mavenProject = new MavenProject();
            mavenProject.setArtifacts(Collections.emptySet());
            testee.setProject(mavenProject);
        }

        @Test
        void shouldSkipFileIfItsExcluded() {
            final String ignoredFilename = "empty-as-well.dmn";
            testee.setExcludes(new String[]{ignoredFilename});
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
            testee.setSearchPaths(new String[]{"src/"});
            final MojoExecutionException assertionError = Assertions.assertThrows(MojoExecutionException.class, testee::execute);
            Assertions.assertTrue(assertionError.getMessage().contains("Some files are not valid, see previous logs."));
        }

        @Test
        void shouldDetectIfFileIsOnSearchPathWithMultiplePaths() {
            testee.setSearchPaths(new String[]{"src/main/java", "src/"});
            final MojoExecutionException assertionError = Assertions.assertThrows(MojoExecutionException.class, testee::execute);
            Assertions.assertTrue(assertionError.getMessage().contains("Some files are not valid, see previous logs."));
        }

    }

    @TempDir
    Path tempDir;

    @Test
    void shouldAddArtifactsFromProjectToProjectClassloader() throws IOException {
        final MavenProject mavenProject = new MavenProject();

        final Artifact artifact = Mockito.mock(Artifact.class);

        final Path artifactFile = Files.createFile(tempDir.resolve("artifact-file"));

        when(artifact.getFile()).thenReturn(artifactFile.toFile());

        mavenProject.setArtifacts(Collections.singleton(artifact));
        testee.setProject(mavenProject);

        Assertions.assertDoesNotThrow(testee::loadProjectclasspath);

        Assertions.assertEquals(((URLClassLoader) ProjectClassLoader.INSTANCE.classLoader).getURLs()[0],
                artifactFile.toUri().toURL());
    }

    @Test
    void shouldFailIfArtifactURIIsInvalid() {
        final MavenProject mavenProject = new MavenProject();

        final Artifact artifact = Mockito.mock(Artifact.class);

        final File artifactFile = Mockito.mock(File.class);
        final URI uri = URI.create("htt://foo");
        when(artifactFile.toURI()).thenReturn(uri);
        when(artifact.getFile()).thenReturn(artifactFile);

        mavenProject.setArtifacts(Collections.singleton(artifact));
        testee.setProject(mavenProject);

        Assertions.assertThrows(MojoExecutionException.class, testee::loadProjectclasspath);
    }
}
