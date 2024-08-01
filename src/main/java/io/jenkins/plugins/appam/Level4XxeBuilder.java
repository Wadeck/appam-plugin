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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Level4XxeBuilder extends Builder {

    private String xmlContent;

    @DataBoundConstructor
    public Level4XxeBuilder() {
    }

    @DataBoundSetter
    public void setXmlContent(String xmlContent) {
        this.xmlContent = xmlContent;
    }

    public String getXmlContent() {
        return xmlContent;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        // do nothing
        return true;
    }

    @Extension
    @Symbol("level4")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public String getDisplayName() {
            return "[appam] Level 4 XXE";
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @POST
        @Restricted(DoNotUse.class) // only used by Jelly
        public FormValidation doPreviewXmlParsing(@AncestorInPath Item item, @QueryParameter String xmlContent) {
            if (item == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            } else {
                item.checkPermission(Job.CONFIGURE);
            }

            InputStream xmlInput = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));

            Document document = null;
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                document = builder.parse(xmlInput);
            } catch (IOException | ParserConfigurationException | SAXException e) {
                return FormValidation.error(e, "Error during parsing");
            }

            NodeList resultList = document.getElementsByTagName("result");
            if (resultList.getLength() != 1) {
                return FormValidation.error("There should be a unique 'result' element in the XML content");
            }

            Node result = resultList.item(0);
            return FormValidation.ok("Found: " + result.getTextContent());
        }
    }
}
