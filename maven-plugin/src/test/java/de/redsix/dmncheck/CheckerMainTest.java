package de.redsix.dmncheck;

import de.redsix.dmncheck.validators.InputEntryTypeValidator;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtendWith(TempDirectory.class)
class CheckerMainTest {

    final private CheckerMain testee = new CheckerMain();

    @BeforeEach
    void setUp() {
        final MavenProject mavenProject = new MavenProject();
        mavenProject.setArtifacts(Collections.emptySet());
        testee.setProject(mavenProject);
    }

    @Test
    void readsAllDmnFilesRecursively(@TempDirectory.TempDir Path temporaryFolder) throws IOException {
        Path folder1 = temporaryFolder.resolve("folder1");
        Path folder1_1 = folder1.resolve("folder1-1");
        Path folder2 = temporaryFolder.resolve("folder2");

        final List<String> dmnFileNames = Arrays.asList(temporaryFolder.toAbsolutePath() + File.separator + "file1.dmn",
                folder1.toAbsolutePath() + File.separator + "file1.dmn",
                folder1.toAbsolutePath() + File.separator + "file2.dmn",
                folder1_1.toAbsolutePath() + File.separator + "file2.dmn",
                folder2.toAbsolutePath() + File.separator + "file2.dmn");

        final List<String> txtFileNames = Arrays.asList(temporaryFolder.toAbsolutePath() + File.separator + "file1.txt",
                folder1.toAbsolutePath() + File.separator + "file1.txt",
                folder1.toAbsolutePath() + File.separator + "file2.txt",
                folder1_1.toAbsolutePath() + File.separator + "file2.txt",
                folder2.toAbsolutePath() + File.separator + "file2.txt");

        final List<String> allFileNames = Stream.concat(dmnFileNames.stream(), txtFileNames.stream()).collect(Collectors.toList());

        for (String fileName : allFileNames) {
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            Assertions.assertTrue(file.createNewFile());
        }

        // additional empty directory
        Files.createDirectory(folder1.resolve("folder1-0"));

        final List<String> result = testee
                .getFileNames("dmn", Collections.singletonList(temporaryFolder));

        MatcherAssert.assertThat(result, Matchers.containsInAnyOrder(dmnFileNames.toArray()));
    }

    @Test
    void shouldDetectSimpleDuplicateInFile() {
        final MojoExecutionException assertionError = Assertions.assertThrows(MojoExecutionException.class,
                () -> testee.testFiles(Collections.singletonList(getFile("duplicate_unique.dmn"))));
        Assertions.assertTrue(assertionError.getMessage().contains("Some files are not valid, see previous logs."));
    }

    @Test
    void shouldSkipFileIfItsExcluded() {
        final String ignoredFilename = "duplicate_unique.dmn";
        testee.setExcludes(new String[] { ignoredFilename });
        final List<File> filesToTest = testee.fetchFilesToTestFromSearchPaths(Collections.singletonList(Paths.get("")));

        Assertions.assertTrue(filesToTest.stream().noneMatch(file -> file.getName().equals(ignoredFilename)));
    }

    @Test
    void shouldSkipFileIfIsNotOnSearchPath() throws Exception {
        testee.setSearchPaths(new String[] {"src/main/java"});
        testee.execute();
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

    @Test
    void shouldSkipFileIfHitpolicyIsCollect() throws Exception {
        testee.testFiles(Collections.singletonList(getFile("duplicate_collect.dmn")));
    }

    @Test
    void shouldAcceptDishDecisionExample() throws Exception {
        testee.testFiles(Collections.singletonList(getFile("dish-decision.dmn")));
    }

    @Test
    void shouldAcceptDishDecisionDmnStandard13Example() throws MojoExecutionException {
        testee.testFiles(Collections.singletonList(getFile("dish-decision-1-3.dmn")));
    }

    @Test
    void shouldHandleInvalidDMNFiles() {
        final MojoExecutionException assertionError = Assertions.assertThrows(MojoExecutionException.class,
                () -> testee.testFiles(Collections.singletonList(getFile("empty.dmn"))));
        Assertions.assertTrue(assertionError.getMessage().contains("Some files are not valid, see previous logs."));
    }

    @Test
    void shouldLoadNoValidatorFromConfig() throws MojoExecutionException {
        testee.setValidatorPackages(new String[] { });

        testee.testFiles(Collections.singletonList(getFile("duplicate_unique.dmn")));
    }

    @Test
    void shouldLoadDuplicateRuleValidatorFromConfig() {
        testee.setValidatorClasses(new String[] {InputEntryTypeValidator.class.getCanonicalName()});
        testee.setValidatorPackages(new String[] {InputEntryTypeValidator.class.getPackage().getName()});

        final MojoExecutionException assertionError = Assertions.assertThrows(MojoExecutionException.class,
                () -> testee.testFiles(Collections.singletonList(getFile("duplicate_unique.dmn"))));
        Assertions.assertTrue(assertionError.getMessage().contains("Some files are not valid, see previous logs."));
    }

    @Test
    void shouldAcceptDishDecisionRequirementGraphExample() throws Exception {
        testee.testFiles(Collections.singletonList(getFile("decision-requirement-diagram.dmn")));
    }

    @Test
    void shouldDetectCyclesInRequirementGraphs() {
        final MojoExecutionException assertionError = Assertions.assertThrows(MojoExecutionException.class,
                () -> testee.testFiles(Collections.singletonList(getFile("cyclic-diagram.dmn"))));
        Assertions.assertTrue(assertionError.getMessage().contains("Some files are not valid, see previous logs."));
    }

    private File getFile(final String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        final URL url = classLoader.getResource(filename);
        Assertions.assertNotNull(url, String.format("No such file %s", filename));
        return new File(url.getFile());
    }

}
