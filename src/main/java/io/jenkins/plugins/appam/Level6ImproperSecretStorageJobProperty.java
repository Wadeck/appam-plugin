package io.jenkins.plugins.appam;

import hudson.Extension;
import hudson.model.*;
import hudson.security.ACL;
import hudson.security.csrf.CrumbExclusion;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import jenkins.model.OptionalJobProperty;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.verb.POST;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Level6ImproperSecretStorageJobProperty extends OptionalJobProperty<Job<?, ?>> {

    private static final String URL_NAME = "build-api";

    private String secret;
    private Secret password;
    private Secret apiToken;

    @DataBoundConstructor
    public Level6ImproperSecretStorageJobProperty() {
    }

    @DataBoundSetter
    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getSecret() {
        return secret;
    }

    @DataBoundSetter
    public void setPassword(Secret password) {
        this.password = password;
    }

    public Secret getPassword() {
        return password;
    }

    @DataBoundSetter
    public void setApiToken(Secret apiToken) {
        this.apiToken = apiToken;
    }

    public Secret getApiToken() {
        return apiToken;
    }

    @Extension
    @Symbol("level6")
    public static final class DescriptorImpl extends OptionalJobPropertyDescriptor {
        public String getDisplayName() {
            return "[appam] Level 6 Improper Secret Storage";
        }
    }

    @Extension
    @Symbol("level6")
    public static final class UnprotectedRootActionImpl implements UnprotectedRootAction {
        @Override
        public String getIconFileName() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getUrlName() {
            return URL_NAME;
        }

        @POST
        public void doDynamic(StaplerRequest req, StaplerResponse rsp,
                              @QueryParameter(fixEmpty = true) String secret,
                              @QueryParameter(fixEmpty = true) String password,
                              @QueryParameter(fixEmpty = true) String apiToken) {
            FreeStyleProject project;
            String restOfPath = req.getRestOfPath();
            // cut the first "/"
            String fullDisplayName = restOfPath.substring(1);
            try (var acl = ACL.as2(ACL.SYSTEM2)) {
                Item item = Jenkins.get().getItemByFullName(fullDisplayName);
                if (!(item instanceof FreeStyleProject)) {
                    throw HttpResponses.error(404, "Job not found");
                }
                project = (FreeStyleProject) item;
                Level6ImproperSecretStorageJobProperty property = project.getProperty(Level6ImproperSecretStorageJobProperty.class);
                if (property == null) {
                    throw HttpResponses.error(404, "Job not found");
                }
                boolean accepted = isAccepted(property, secret, password, apiToken);
                if (!accepted) {
                    throw HttpResponses.error(404, "Job not found");
                }

            }
            project.scheduleBuild(new CauseImpl());
            rsp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }

        private boolean isAccepted(Level6ImproperSecretStorageJobProperty property, String secret, String password, String apiToken) {
            if (property.getSecret() != null && property.getSecret().equals(secret)) {
                return true;
            }
            if (property.getPassword() != null && property.getPassword().getPlainText().equals(password)) {
                return true;
            }
            if (property.getApiToken() != null && property.getApiToken().getPlainText().equals(apiToken)) {
                return true;
            }
            return false;
        }
    }

    /**
     * To allow anyone to call that endpoint, especially unauthenticated users
     * Format: build-api/folder1/job1
     */
    @Extension
    public static class CrumbExclusionImpl extends CrumbExclusion {
        @Override
        public boolean process(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.startsWith("/" + URL_NAME + "/")) {
                chain.doFilter(req, resp);
                return true;
            }
            return false;
        }
    }

    public static class CauseImpl extends Cause {
        @Override
        public String getShortDescription() {
            return "[level 6] API call";
        }
    }
}
