package de.redsix.dmncheck;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DmnCheckGradlePlugin implements Plugin<Project> {

    public static final String DMN_CHECK_EXTENSION = "dmnCheck";
    public static final String CHECK_DMN_TASK = "checkDmn";

    @Override
    public void apply(Project project) {
        final DmnCheckExtension dmnCheckExtension =
                project.getExtensions().create(DMN_CHECK_EXTENSION, DmnCheckExtension.class);
        final DmnCheckTask dmnCheckTask = project.getTasks().create(CHECK_DMN_TASK, DmnCheckTask.class);

        dmnCheckTask.getExtensions().add(DMN_CHECK_EXTENSION, dmnCheckExtension);
        dmnCheckTask.setGroup("verification");
    }
}
