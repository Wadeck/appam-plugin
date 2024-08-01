package io.jenkins.plugins.appam;

import groovy.lang.GroovyShell;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Level2CsrfBuilder extends Builder {

    private String version;
    private String targetProjectName;
    private String url;
    private String script;

    @DataBoundConstructor
    public Level2CsrfBuilder() {
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

    @DataBoundSetter
    public void setScript(String script) {
        this.script = script;
    }

    public String getScript() {
        return script;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        // do nothing
        return true;
    }

    @Extension
    @Symbol("level2")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public String getDisplayName() {
            return "[appam] Level 2 CSRF";
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doCheckVersion(@AncestorInPath Item item, @QueryParameter String value) throws IOException {
            if (item == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Job.CONFIGURE);
            }

            if (!value.matches("([^.]*?)\\.([^.]*?)\\.([^.]*?)")) {
                return FormValidation.error("The version must follow the pattern x.y.z, received: " + value);
            }
            return FormValidation.ok("Version is well formated.");
        }

        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doCheckTargetProjectName(@AncestorInPath Item item, @QueryParameter String value) throws IOException {
            if (item == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Job.CONFIGURE);
            }

            TopLevelItem project = Jenkins.get().getItem(value);
            if (project == null) {
                return FormValidation.warning("There is no project with the name " + value);
            }
            return FormValidation.ok("Project found: " + project.getFullDisplayName());
        }

        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doValidateTargetProjectName(@AncestorInPath Item item, @QueryParameter String targetProjectName) throws IOException {
            if (item == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Job.CONFIGURE);
            }

            try (var acl = ACL.as2(ACL.SYSTEM2)) {
                TopLevelItem project = Jenkins.get().getItem(targetProjectName);
                if (project == null) {
                    return FormValidation.warning("There is no project with the name " + targetProjectName);
                }
                return FormValidation.ok("Project found: " + project.getFullDisplayName());
            }
        }

        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doCheckUrl(@AncestorInPath Item item, @QueryParameter String value) throws IOException {
            if (item == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Job.CONFIGURE);
            }

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

        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doValidateUrl(@AncestorInPath Item item, @QueryParameter String url) throws IOException {
            if (item == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Job.CONFIGURE);
            }

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

        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doCompileScript(@AncestorInPath Item item, @QueryParameter String script) throws IOException {
            if (item == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Job.CONFIGURE);
            }

            CompilerConfiguration config = new CompilerConfiguration();

            try {
                new GroovyShell(config).parse(script);
                return FormValidation.ok("Script compiled");
            }
            catch (Exception e) {
                return FormValidation.error(e, "Error during compilation");
            }
        }

        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doExecuteScript(@AncestorInPath Item item, @QueryParameter String script) throws IOException {
            if (item == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Job.CONFIGURE);
            }

            try {
                Object result = new GroovyShell().evaluate(script);
                return FormValidation.ok("Script executed: " + result);
            }
            catch (Exception e) {
                return FormValidation.error(e, "Error during evaluation");
            }
        }
    }
}
