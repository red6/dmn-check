import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
            getLog().info(file.getName());
            try {
                if (getExcludeList().contains(file.getName())) {
                    getLog().info("Skipped File: " + file);
                } else {
                    testDmnDuplicates(file);
                }
            } catch (JDOMException e) {
                getLog().error("Error while processing file: " + file, e);
            } catch (IOException e) {
                e.printStackTrace();
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

    private void testDmnDuplicates(File file) throws JDOMException, IOException {

        final SAXBuilder builder = new SAXBuilder();
        final Namespace ns = Namespace.getNamespace("http://www.omg.org/spec/DMN/20151101/dmn11.xsd");
        final Document document = builder.build(file);
        final Element rootNode = document.getRootElement();

        final Element decision = rootNode.getChild("decision", ns);
        final Element decisionTable = decision.getChild("decisionTable", ns);
        final Attribute hitPolicy = decisionTable.getAttribute("hitPolicy");

        if (hitPolicy == null || !hitPolicy.getValue().equalsIgnoreCase("collect")) {
            final List<Element> rules = decisionTable.getChildren("rule", ns);
            final List<List<String>> expressions = new ArrayList<>();
            final List<String> result = new ArrayList<>();

            for (Element rule : rules) {
                final List<Element> inputEntries = rule.getChildren("inputEntry", ns);
                final List<String> rowElements = new ArrayList<>();
                for (Element child : inputEntries) {
                    final Element text = child.getChild("text", ns);
                    rowElements.add(text.getValue());
                }
                if (!expressions.contains(rowElements)) {
                    expressions.add(rowElements);
                } else {
                    result.add("Rule is defined more than once " + rowElements + " in File:" + file.getAbsolutePath());
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
