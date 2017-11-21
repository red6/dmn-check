import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CheckerMainTest {

    final private CheckerMain testee = new CheckerMain();

    @Test
    public void shouldDetectSimpleDuplicateInFile() {
        AssertionError assertionError = assertThrows(AssertionError.class,
                () -> testee.testFiles(Arrays.asList(getFile("duplicate_unique.dmn"))));
        assertTrue(assertionError.getMessage().contains("Rule is defined more than once"));
    }

    @Test
    public void shouldSkipFileIfItsExcluded() {
        testee.setExcludes(new String[]{"duplicate_unique.dmn"});
        testee.testFiles(Arrays.asList(getFile("duplicate_unique.dmn")));
    }


    @Test
    public void shouldSkipFileIfHitpolicyIsCollect() {
        testee.testFiles(Arrays.asList(getFile("duplicate_collect.dmn")));
    }

    private File getFile(final String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile());
    }

}
