package io.jenkins.plugins.appam;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.verb.POST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Level3XssBuilder extends Builder {

    private String description;
    private String url;
    private String title;
    private String slug;

    @DataBoundConstructor
    public Level3XssBuilder() {
    }

    @DataBoundSetter
    public void setDescription(String description) {
        this.description = Util.fixEmptyAndTrim(description);
    }

    public String getDescription() {
        return description;
    }

    @DataBoundSetter
    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @DataBoundSetter
    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        // do nothing
        return true;
    }

    @Extension
    @Symbol("level3")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public String getDisplayName() {
            return "[appam] Level 3 XSS";
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @POST
        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doPreviewDescription(@AncestorInPath Item item, @QueryParameter String description, StaplerResponse rsp) {
            if (item == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Job.CONFIGURE);
            }

            description = Util.fixEmptyAndTrim(description);

            if (description == null) {
                return FormValidation.ok();
            }

            rsp.setHeader("script", "document.getElementById('preview').innerText='" + description + "'");
            return FormValidation.ok();
        }

        @POST
        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doCheckTitle(@AncestorInPath Item item, @QueryParameter String title) {
            if (item == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Job.CONFIGURE);
            }

            if (title.length() > 5) {
                return FormValidation.okWithMarkup("The title is good: " + title);
            } else {
                return FormValidation.error("The title is too short");
            }
        }

        @POST
        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doCheckSlug(@AncestorInPath Item item, @QueryParameter String value) {
            if (item == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Job.CONFIGURE);
            }
            value = Util.fixEmptyAndTrim(value);

            if (value == null) {
                return FormValidation.ok();
            }

            String slugified = slugify(value);

            if (slugified.equals(value)) {
                return FormValidation.okWithMarkup("The slug is valid: " + slugified);
            } else {
                return FormValidation.errorWithMarkup("The slug is not valid: " + slugified);
            }
        }

        @POST
        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doCheckUrl(@AncestorInPath Item item, @QueryParameter String value) {
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
                return FormValidation.errorWithMarkup(e.getMessage());
            }

            return FormValidation.ok();
        }

        @POST
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

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            int status = con.getResponseCode();
            if (status < 400) {
                return FormValidation.okWithMarkup("Content is: " + content);
            } else {
                return FormValidation.error("Error during call: " + status);
            }
        }

        public static String slugify(String value) {
            return Util.fixNull(value).replaceAll("[ '\"]", "-");
        }
    }
}
