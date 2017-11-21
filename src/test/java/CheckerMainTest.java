import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CheckerMainTest {

    final private CheckerMain testee = new CheckerMain();

    @Test
    public void shouldDetectSimpleDuplicateInFile() {
        AssertionError assertionError = assertThrows(AssertionError.class,
                () -> testee.testFiles(Collections.singletonList(getFile("duplicate_unique.dmn"))));
        assertTrue(assertionError.getMessage().contains("Rule is defined more than once"));
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

    private File getFile(final String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        final URL url = classLoader.getResource(filename);
        assertNotNull(url, String.format("No such file %s", filename));
        return new File(url.getFile());
    }

}
