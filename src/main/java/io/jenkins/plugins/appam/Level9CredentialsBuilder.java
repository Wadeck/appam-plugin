package io.jenkins.plugins.appam;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.*;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

public class Level9CredentialsBuilder extends Builder {

    private String url;
    private String credentialsId;

    @DataBoundConstructor
    public Level9CredentialsBuilder() {
    }

    @DataBoundSetter
    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        // do nothing
        return true;
    }

    @Extension
    @Symbol("xss")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public String getDisplayName() {
            return "[appam] Level 9 Credentials";
        }

        @POST
        @Restricted(DoNotUse.class) // only used by Jelly
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
            if (item == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Job.CONFIGURE);
            }

            StandardListBoxModel result = new StandardListBoxModel();
            return result
                    .includeEmptyValue()
                    .includeAs(ACL.SYSTEM2, Jenkins.get(), UsernamePasswordCredentialsImpl.class)
                    .includeCurrentValue(credentialsId);
        }

        @POST
        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doValidateUrlAndCredentialsId(@AncestorInPath Item item, @QueryParameter String url, @QueryParameter String credentialsId) throws IOException {
            if (item == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Job.CONFIGURE);
            }

            url = Util.fixEmptyAndTrim(url);

            StandardUsernamePasswordCredentials credentials = CredentialsMatchers.firstOrNull(
                    CredentialsProvider.lookupCredentialsInItemGroup(UsernamePasswordCredentialsImpl.class, null, ACL.SYSTEM2, Collections.emptyList()),
                    CredentialsMatchers.withId(credentialsId));

            URL urlObject = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlObject.openConnection();
            con.setRequestMethod("GET");
            if (credentials != null) {
                String login = credentials.getUsername();
                String password = credentials.getPassword().getPlainText();
                con.setRequestProperty("Authentication", Base64.getEncoder().encodeToString((login + ":" + password).getBytes(StandardCharsets.UTF_8)));
            }
            con.setDoOutput(true);
            con.connect();

            int status = con.getResponseCode();
            if (status < 400) {
                return FormValidation.ok();
            } else {
                return FormValidation.error("Error during call: " + status);
            }
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
