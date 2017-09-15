
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CheckerMainTest {

    final private CheckerMain testee = new CheckerMain();

    @Test
    public void shouldDetectSimpleDuplicateInFile() {
        try {
            testee.testFiles(Arrays.asList(getFile("duplicate_unique.dmn")));
        } catch (AssertionError assertionError) {
            assertThat(assertionError.getMessage().contains("Rule is defined more than once"), is(true));
        }
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
