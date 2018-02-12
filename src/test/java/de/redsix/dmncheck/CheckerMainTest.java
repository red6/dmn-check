package de.redsix.dmncheck;

import org.apache.maven.plugin.MojoExecutionException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EnableRuleMigrationSupport
public class CheckerMainTest {

    final private CheckerMain testee = new CheckerMain();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    void readsAllDmnFilesRecursively() throws IOException {
        File folder1_1 = temporaryFolder.newFolder("folder1-0", "folder1-1");
        File folder1 = folder1_1.getParentFile();
        File folder2 = temporaryFolder.newFolder("folder2-0");

        final List<String> dmnFileNames = Arrays.asList(temporaryFolder.getRoot() + File.separator + "file1.dmn",
                folder1.getAbsolutePath() + File.separator + "file1.dmn", folder1.getAbsolutePath() + File.separator + "file2.dmn",
                folder1_1.getAbsolutePath() + File.separator + "file2.dmn",
                folder2.getAbsolutePath() + File.separator + "file2.dmn");

        final List<String> txtFileNames = Arrays.asList(temporaryFolder.getRoot() + File.separator + "file1.txt",
                folder1.getAbsolutePath() + File.separator + "file1.txt", folder1.getAbsolutePath() + File.separator + "file2.txt",
                folder1_1.getAbsolutePath() + File.separator + "file2.txt",
                folder2.getAbsolutePath() + File.separator + "file2.txt");

        final List<String> allFileNames = Stream.concat(dmnFileNames.stream(), txtFileNames.stream()).collect(Collectors.toList());

        for (String fileName : allFileNames) {
            File file = new File(fileName);
            Assertions.assertTrue(file.createNewFile());
        }

        final List<String> result = testee
                .getFileNames("dmn", Collections.singletonList(temporaryFolder.getRoot().getAbsoluteFile().toPath()));

        MatcherAssert.assertThat(result, Matchers.containsInAnyOrder(dmnFileNames.toArray()));
    }

    @Test
    void shouldDetectSimpleDuplicateInFile() {
        final MojoExecutionException assertionError = Assertions.assertThrows(MojoExecutionException.class,
                () -> testee.testFiles(Collections.singletonList(getFile("duplicate_unique.dmn"))));
        Assertions.assertTrue(assertionError.getMessage().contains("Some files are not valid, see previous logs."));
    }

    @Test
    void shouldSkipFileIfItsExcluded() throws Exception {
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
    void shouldHandleInvalidDMNFiles() {
        final MojoExecutionException assertionError = Assertions.assertThrows(MojoExecutionException.class,
                () -> testee.testFiles(Collections.singletonList(getFile("empty.dmn"))));
        Assertions.assertTrue(assertionError.getMessage().contains("Some files are not valid, see previous logs."));
    }

    private File getFile(final String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        final URL url = classLoader.getResource(filename);
        Assertions.assertNotNull(url, String.format("No such file %s", filename));
        return new File(url.getFile());
    }

}
