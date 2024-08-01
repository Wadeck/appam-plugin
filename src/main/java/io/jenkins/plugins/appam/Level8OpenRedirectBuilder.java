package io.jenkins.plugins.appam;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.GET;

import java.io.IOException;

public class Level8OpenRedirectBuilder extends Builder {

    private String url;

    @DataBoundConstructor
    public Level8OpenRedirectBuilder() {
    }

    @DataBoundSetter
    public void setUrl(String url) {
        this.url = Util.fixEmptyAndTrim(url);
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        if (url != null) {
            build.addAction(new ActionImpl(url));
        }
        return true;
    }

    @Extension
    @Symbol("level8")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public String getDisplayName() {
            return "[appam] Level 8 Open Redirect";
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }

    public static final class ActionImpl implements Action {
        private final String targetUrl;

        public ActionImpl(String targetUrl) {
            this.targetUrl = targetUrl;
        }

        @Override
        public String getIconFileName() {
            return "symbol-link";
        }

        @Override
        public String getDisplayName() {
            return "External link";
        }

        @Override
        public String getUrlName() {
            return "custom-external-link";
        }

        @GET
        public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
            rsp.sendRedirect2(targetUrl);
        }
    }
}
