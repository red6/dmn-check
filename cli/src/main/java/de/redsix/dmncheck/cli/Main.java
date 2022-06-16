package de.redsix.dmncheck.cli;

import de.redsix.dmncheck.plugin.PluginBase;
import de.redsix.dmncheck.plugin.PrettyPrintValidationResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

@Command(mixinStandardHelpOptions = true)
public class Main implements PluginBase, Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Option(names = {"--excludeList"})
    @SuppressWarnings("nullness")
    private List<String> excludeList;

    @Option(names = {"--searchPath"})
    @SuppressWarnings("nullness")
    private List<String> searchPath;

    @Option(names = {"--validatorPackages"})
    @SuppressWarnings("nullness")
    private String[] validatorPackages;

    @Option(names = {"--validatorClasses"})
    @SuppressWarnings("nullness")
    private String[] validatorClasses;

    @Option(names = {"--failOnWarning"})
    @SuppressWarnings("nullness")
    private boolean failOnWarning;

    @Option(names = {"--projectClasspath"})
    @SuppressWarnings("nullness")
    private String projectClasspath;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public PrettyPrintValidationResults.PluginLogger getPluginLogger() {
        return new PrettyPrintValidationResults.PluginLogger((t) -> logger.info(t.toString()),
                (t) -> logger.warn(t.toString()), (t) -> logger.error(t.toString()));
    }

    @Override
    public List<String> getExcludeList() {
        if (Objects.isNull(excludeList)) {
            return Collections.emptyList();
        } else {
            return excludeList;
        }
    }

    @Override
    public List<String> getSearchPathList() {
        if (Objects.isNull(searchPath) || searchPath.isEmpty()) {
            return Collections.singletonList("");
        } else {
            return searchPath;
        }
    }

    @Override
    public String[] getValidatorPackages() {
        return validatorPackages;
    }

    @Override
    public String[] getValidatorClasses() {
        return validatorClasses;
    }

    @Override
    public boolean failOnWarning() {
        return failOnWarning;
    }

    @Override
    public Integer call() {
        if (Objects.nonNull(projectClasspath)) {
            loadProjectClasspath(Arrays.asList(projectClasspath.split(":")));
        }

        return validate() ? 1 : 0;
    }

}
