package io.jenkins.plugins.appam;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Level1MissingPermissionBuilder extends Builder {

    private String version;
    private String targetProjectName;
    private String url;

    @DataBoundConstructor
    public Level1MissingPermissionBuilder() {
    }

    @DataBoundSetter
    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    @DataBoundSetter
    public void setTargetProjectName(String targetProjectName) {
        this.targetProjectName = targetProjectName;
    }

    public String getTargetProjectName() {
        return targetProjectName;
    }

    @DataBoundSetter
    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        // do nothing
        return true;
    }

    @Extension
    @Symbol("level1")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public String getDisplayName() {
            return "[appam] Level 1 Missing Permission";
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @RequirePOST
        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doCheckVersion(@QueryParameter String value) throws IOException {
            if (!value.matches("([^.]*?)\\.([^.]*?)\\.([^.]*?)")) {
                return FormValidation.error("The version must follow the pattern x.y.z, received: " + value);
            }
            return FormValidation.ok("Version is well formated.");
        }

        @POST
        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doCheckTargetProjectName(@QueryParameter String value) throws IOException {
            Jenkins.get().hasPermission(Jenkins.ADMINISTER);

            TopLevelItem project = Jenkins.get().getItem(value);
            if (project == null) {
                return FormValidation.warning("There is no project with the name " + value);
            }
            return FormValidation.ok("Project found: " + project.getFullDisplayName());
        }

        @POST
        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doValidateTargetProjectName(@QueryParameter String targetProjectName) throws IOException {
            try (var acl = ACL.as2(ACL.SYSTEM2)) {
                TopLevelItem project = Jenkins.get().getItem(targetProjectName);
                if (project == null) {
                    return FormValidation.warning("There is no project with the name " + targetProjectName);
                }
                return FormValidation.ok("Project found: " + project.getFullDisplayName());
            }
        }

        @POST
        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doCheckUrl(@QueryParameter String value) throws IOException {
            if (value.isBlank()) {
                return FormValidation.warning("Target URL should not be blank");
            }

            try {
                new URI(value);
            } catch (URISyntaxException e) {
                return FormValidation.error(e, "Target URL is malformed");
            }

            return FormValidation.ok();
        }

        @POST
        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doValidateUrl(@QueryParameter String url) throws IOException {
            Jenkins.get().checkPermission(Job.CONFIGURE);

            URL urlObject = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlObject.openConnection();
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            con.connect();

            int status = con.getResponseCode();
            if (status < 400) {
                return FormValidation.ok();
            } else {
                return FormValidation.error("Error during call: " + status);
            }
        }
    }
}
