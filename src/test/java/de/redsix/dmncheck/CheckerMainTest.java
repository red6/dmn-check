package de.redsix.dmncheck;

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
    public void readsAllDmnFilesRecursively() throws IOException {
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

        final List<String> result = testee.getFileNames("dmn", temporaryFolder.getRoot().getAbsoluteFile().toPath());

        MatcherAssert.assertThat(result, Matchers.containsInAnyOrder(dmnFileNames.toArray()));
    }

    @Test
    public void shouldDetectSimpleDuplicateInFile() {
        AssertionError assertionError = Assertions.assertThrows(AssertionError.class,
                () -> testee.testFiles(Collections.singletonList(getFile("duplicate_unique.dmn"))));
        Assertions.assertTrue(assertionError.getMessage().contains("Rule is defined more than once"));
    }

    @Test
    public void shouldSkipFileIfItsExcluded() {
        testee.setExcludes(new String[] {"duplicate_unique.dmn"});
        testee.testFiles(Collections.singletonList(getFile("duplicate_unique.dmn")));
    }

    @Test
    public void shouldSkipFileIfHitpolicyIsCollect() {
        testee.testFiles(Collections.singletonList(getFile("duplicate_collect.dmn")));
    }

    @Test
    public void shouldAcceptDishDecisionExample() {
        testee.testFiles(Collections.singletonList(getFile("dish-decision.dmn")));
    }

    private File getFile(final String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        final URL url = classLoader.getResource(filename);
        Assertions.assertNotNull(url, String.format("No such file %s", filename));
        return new File(url.getFile());
    }

}
