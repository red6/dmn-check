package de.redsix.dmncheck;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.DrgElement;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "check-dmn")
class CheckerMain extends AbstractMojo {

    @Parameter
    private String[] excludes;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final List<String> fileNames = getFileNames(".dmn", Paths.get(""));
        final List<File> collect = fileNames.stream().map(name -> new File(name)).collect(Collectors.toList());

        testFiles(collect);

    }

    void testFiles(final List<File> files) {
        for (File file : files) {
            if (getExcludeList().contains(file.getName())) {
                getLog().info("Skipped File: " + file);
            } else {
                final DmnModelInstance dmnModelInstance = Dmn.readModelFromFile(file);
                final Collection<DrgElement> drgElements = dmnModelInstance.getDefinitions().getDrgElements();

                for (DrgElement drgElement : drgElements) {
                    DecisionTable decisionTable = (DecisionTable) ((Decision) drgElement).getExpression();
                    testDmnDuplicates(decisionTable);
                }
            }
        }
    }

    private List<String> getExcludeList() {
        if (excludes != null) {
            return Arrays.asList(excludes);
        } else {
            return new ArrayList<>();
        }
    }

    private void testDmnDuplicates(DecisionTable decisionTable) {
        if (!HitPolicy.COLLECT.equals(decisionTable.getHitPolicy())) {

            final Collection<Rule> rules = decisionTable.getRules();
            final List<List<String>> expressions = new ArrayList<>();
            final List<String> result = new ArrayList<>();

            for (Rule rule : rules) {
                final List<String> rowElements = rule.getInputEntries().stream().
                        map(ModelElementInstance::getTextContent).collect(Collectors.toList());
                if (!expressions.contains(rowElements)) {
                    expressions.add(rowElements);
                } else {
                    result.add("Rule is defined more than once " + rowElements);
                }
            }
            if (!result.isEmpty()) {
                throw new AssertionError(result.toString());
            }
        }

    }

    protected List<String> getFileNames(String suffix, Path dir) {
        try {
            return Files.walk(dir).filter(Files::isRegularFile).map(path -> path.toAbsolutePath().toString())
                    .filter(absolutePath -> absolutePath.endsWith(suffix)).collect(Collectors.toList());
        }
        catch (IOException e) {
            throw new RuntimeException("Could not determine DMN files.", e);
        }
    }

    void setExcludes(final String[] excludes) {
        this.excludes = excludes;
    }

}
