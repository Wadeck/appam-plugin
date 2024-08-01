package io.jenkins.plugins.appam;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Level5PathTraversalBuilder extends Builder {

    private String filePath;

    @DataBoundConstructor
    public Level5PathTraversalBuilder() {
    }

    @DataBoundSetter
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        Path fullPath = Paths.get(build.getRootDir().getAbsolutePath(), filePath);
        String result = null;
        try {
            List<String> lines = Files.readAllLines(fullPath);
            result = String.join("\n", lines);
        } catch (IOException e) {
            listener.getLogger().println("Impossible to read the file: " + e.getMessage());
        }

        listener.getLogger().println("Content of the file: " + fullPath.toAbsolutePath() + "\n" + result);
        return true;
    }

    @Extension
    @Symbol("level5")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public String getDisplayName() {
            return "[appam] Level 5 Path Traversal";
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return jobType.equals(FreeStyleProject.class);
        }

        @POST
        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doPreviewFile(@AncestorInPath FreeStyleProject freeStyleProject, @QueryParameter String filePath) {
            String jobFolder;
            if (freeStyleProject == null) {
                return FormValidation.error("Not available outside a project");
            } else {
                freeStyleProject.checkPermission(Job.CONFIGURE);
                FreeStyleBuild lastBuild = freeStyleProject.getLastBuild();
                if (lastBuild == null) {
                    return FormValidation.warning("No build yet");
                }

                jobFolder = lastBuild.getRootDir().getAbsolutePath();
            }

            String result = null;
            try {
                List<String> lines = Files.readAllLines(Paths.get(jobFolder, filePath));
                result = String.join("\n", lines);
            } catch (IOException e) {
                return FormValidation.error(e, "Unable to read file");
            }

            return FormValidation.ok("Found: " + result);
        }
    }
}
