import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mojo( name = "check-dmn")
public class CheckerMain extends AbstractMojo {


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final ArrayList arrayList = new ArrayList();
        final List<String> fileNames = getFileNames(".dmn", arrayList, Paths.get("src/main/resources/")); //make as a paremeter (list)
        for(String filename : fileNames) {
            getLog().info(filename);
            try {
                testDmnDuplicates(filename);
            } catch (JDOMException e) {
                getLog().error("Error while processing file: "+filename, e);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void testDmnDuplicates(String filename) throws JDOMException, IOException {
        final File file = new File(filename);
        final SAXBuilder builder = new SAXBuilder();
        final Namespace ns = Namespace.getNamespace("http://www.omg.org/spec/DMN/20151101/dmn11.xsd");
        final Document document = builder.build(file);
        final Element rootNode = document.getRootElement();

        final Element decision = rootNode.getChild("decision", ns);
        final Element decisionTable = decision.getChild("decisionTable", ns);
        final List<Element> rules = decisionTable.getChildren("rule", ns);
        final List<List<String>> expressions = new ArrayList<>();
        final List<String> result= new ArrayList<>();

        for (Element rule : rules) {
            final List<Element> inputEntries = rule.getChildren("inputEntry", ns);
            final List<String> rowElements = new ArrayList<>();
            for (Element child : inputEntries) {
                if (child.getAttribute("id").getValue().toLowerCase().contains("test")) {
                    final Element text = child.getChild("text", ns);
                    rowElements.add(text.getValue());
                }
            }
            if (!expressions.contains(rowElements)) {
                expressions.add(rowElements);
            } else {
                result.add("Rule is defined more than once " + rowElements + " in File:" + filename);
            }
        }
        Assert.assertTrue(result.toString(), result.isEmpty());

    }


    /*https://stackoverflow.com/questions/2534632/list-all-files-from-a-directory-recursively-with-java*/
    private List<String> getFileNames(String suffix, List<String> result, Path dir) {
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if(path.toFile().isDirectory()) {
                    getFileNames(suffix, result, path);
                } else {
                    if(path.toFile().getName().endsWith(suffix)) {
                        result.add(path.toAbsolutePath().toString());
                    }
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
