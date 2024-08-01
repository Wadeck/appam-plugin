package io.jenkins.plugins.appam;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;

public class Level7DenyOfServiceBuilder extends Builder {

    private int iterations;
    private String regex;

    @DataBoundConstructor
    public Level7DenyOfServiceBuilder() {
    }

    @DataBoundSetter
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getIterations() {
        return iterations;
    }

    @DataBoundSetter
    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        // do nothing
        return true;
    }

    @Extension
    @Symbol("level7")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public String getDisplayName() {
            return "[appam] Level 7 Deny of Service";
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @POST
        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doPreviewIterations(@AncestorInPath Item item, @QueryParameter int iterations) {
            if (item == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Job.CONFIGURE);
            }

            long start = System.currentTimeMillis();
            int temp = 0;
            for (int i = 0; i < iterations; i++) {
                temp = (temp + 1) % 10;
            }
            long end = System.currentTimeMillis();
            return FormValidation.ok((end - start) + " ms");
        }

        @POST
        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doPreviewRegex(@AncestorInPath Item item, @QueryParameter String regex, @QueryParameter String sample) {
            if (item == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Job.CONFIGURE);
            }

            long start = System.currentTimeMillis();
            boolean matched = sample.matches(regex);
            long end = System.currentTimeMillis();
            if (matched) {
                return FormValidation.ok("Matched in " + (end - start) + " ms");
            } else {
                return FormValidation.error("Not matching in " + (end - start) + " ms");
            }
        }
    }
}
