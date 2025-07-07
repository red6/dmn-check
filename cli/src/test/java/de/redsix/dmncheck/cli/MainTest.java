package de.redsix.dmncheck.cli;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class MainTest {

    CommandLine cmd;

    @BeforeEach
    void setupCommand() {
        Main app = new Main();
        cmd = new CommandLine(app);
    }

    @Test
    void shouldPrintHelp() {
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("--help");
        assertEquals(0, exitCode);
        assertTrue(sw.toString().startsWith("Usage:"));
    }

    @Test
    void shouldSetExcludeList() {
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("--excludeList=some-file.dmn");
        Main main = cmd.getCommand();
        assertEquals(0, exitCode);
        assertEquals(List.of("some-file.dmn"), main.getExcludeList());
    }

    @Test
    void shouldSetSearchPath() {
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        cmd.execute("--searchPath=model/");
        Main main = cmd.getCommand();
        assertEquals(List.of("model/"), main.getSearchPathList());
    }

    @Test
    void shouldSetValidatorPackages() {
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        cmd.execute("--validatorPackages=com.red6.dmn");
        Main main = cmd.getCommand();
        assertArrayEquals(
            List.of("com.red6.dmn").toArray(new String[0]),
            main.getValidatorPackages()
        );
    }

    @Test
    void shouldSetValidatorClasses() {
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        cmd.execute("--validatorClasses=FancyValidator,DreamValidator");
        Main main = cmd.getCommand();
        assertArrayEquals(
            List.of("FancyValidator", "DreamValidator").toArray(new String[0]),
            main.getValidatorClasses()
        );
    }

    @Test
    void shouldSetFailOnWarning() {
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        cmd.execute("--failOnWarning");
        Main main = cmd.getCommand();
        assertTrue(main.failOnWarning());
    }
}
