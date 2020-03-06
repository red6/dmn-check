package de.redsix.dmncheck.plugin;

import de.redsix.dmncheck.validators.InputEntryTypeValidator;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, TempDirectory.class})
class PluginBaseTest {

    private PluginBase testee = Mockito.spy(PluginBase.class);

    private PrettyPrintValidationResults.CustomLogger emptyLogger = new PrettyPrintValidationResults.CustomLogger(
            __ -> { }, __ -> { }, __ -> { });

    @BeforeEach
    void init() {

        when(testee.getLogger()).thenReturn(emptyLogger);
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

        final List<String> result = testee.getFileNames(Collections.singletonList(temporaryFolder))
                                          .stream()
                                          .map(Path::toAbsolutePath)
                                          .map(Path::toString)
                                          .collect(Collectors.toList());

        MatcherAssert.assertThat(result, Matchers.containsInAnyOrder(dmnFileNames.toArray()));
    }


    @Test
    void shouldDetectSimpleDuplicateInFile() {
        final boolean containsErrors = testee.testFiles(Collections.singletonList(getFile("duplicate_unique.dmn")));
        Assertions.assertTrue(containsErrors);
    }


    @Test
    void shouldAcceptDishDecisionRequirementGraphExample() throws Exception {
        testee.testFiles(Collections.singletonList(getFile("decision-requirement-diagram.dmn")));
    }

    @Test
    void shouldDetectCyclesInRequirementGraphs() {
        final boolean containsErrors = testee.testFiles(Collections.singletonList(getFile("cyclic-diagram.dmn")));
        Assertions.assertTrue(containsErrors);
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
        final boolean containsErrors = testee.testFiles(Collections.singletonList(getFile("empty.dmn")));
        Assertions.assertTrue(containsErrors);
    }

    @Test
    void shouldLoadNoValidatorFromConfig() {
        when(testee.getValidatorClasses()).thenReturn(new String[] { });

        Assertions.assertTrue(testee.testFiles(Collections.singletonList(getFile("duplicate_unique.dmn"))));
    }

    @Test
    void shouldLoadDuplicateRuleValidatorFromConfig() {
        when(testee.getValidatorClasses()).thenReturn(new String[] {InputEntryTypeValidator.class.getCanonicalName()});
        when(testee.getValidatorPackages()).thenReturn(new String[] {InputEntryTypeValidator.class.getPackage().getName()});

        final boolean containsErrors = testee.testFiles(Collections.singletonList(getFile("duplicate_unique.dmn")));
        Assertions.assertTrue(containsErrors);
    }


    private File getFile(final String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        final URL url = classLoader.getResource(filename);
        Assertions.assertNotNull(url, String.format("No such file %s", filename));
        return new File(url.getFile());
    }

}
