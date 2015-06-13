package org.jenkins.ci.plugins.pluginstats;

import hudson.Extension;
import hudson.PluginWrapper;
import hudson.matrix.MatrixProject;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.model.RootAction;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import org.jenkins.ci.plugins.pluginstats.model.InstalledPlugin;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public final class PluginStatsAction implements RootAction {

    private static final boolean DEBUG = false;
    private static final Logger LOG = Logger.getLogger(PluginStatsAction.class.getName());
    private Hashtable<String, InstalledPlugin> installedPluginSet = new Hashtable<String, InstalledPlugin>();

    private int numberOfJobs = 0;
    private int numberOfInstalledPlugins = 0;

    private String levelType;

    public void doClear(final StaplerRequest request, final StaplerResponse response)
            throws ServletException, IOException {
        LOG.log(Level.INFO, "doClear");
        installedPluginSet.clear();
        response.sendRedirect(Hudson.getInstance().getRootUrl());
    }

    private String addJob(Project job, Class<?> context, Hashtable<String, InstalledPlugin> installedPluginSet) {
        File plugin = new File(Utils.findPathJar(context));
        String pluginName = plugin.getName();
        if (installedPluginSet.containsKey(pluginName)) {
            LOG.log(Level.INFO, "<FOUND> " + pluginName);
            InstalledPlugin installedPlugin = installedPluginSet.get(pluginName);
            installedPlugin.addJob(new org.jenkins.ci.plugins.pluginstats.model.Job(job.getName(), job.getUrl()));
            installedPluginSet.put(pluginName, installedPlugin);
        } else {
            LOG.log(Level.INFO, pluginName);
        }
        return pluginName;
    }

    private void buildersParser (Project job, Hashtable<String, InstalledPlugin> installedPluginSet) {
        if (job != null) {
            // Query Builders
            if (job.getBuilders() != null && job.getBuilders().size() > 0) {
                for (int i = 0; i < job.getBuilders().size(); i++) {
                    LOG.log(Level.INFO, "getBuilders " + addJob(job, job.getBuilders().get(i).getClass(), installedPluginSet));
                }
            } else {
                LOG.log(Level.INFO, "getBuilders is empty");
            }
        }
    }

    private void buildWrappersParser (Project job, Hashtable<String, InstalledPlugin> installedPluginSet) {
        if (job != null) {

            if (job.getBuildWrappersList() != null && job.getBuildWrappersList().size() > 0) {
                DescribableList<BuildWrapper, Descriptor<BuildWrapper>> wrappersList = job.getBuildWrappersList();
                Iterator<BuildWrapper> buildWrapperIterator = wrappersList.iterator();
                while (buildWrapperIterator.hasNext()) {
                    BuildWrapper buildWrapper = buildWrapperIterator.next();
                    LOG.log(Level.INFO, "getBuildWrappers " + addJob(job, buildWrapper.getClass(), installedPluginSet));
                }

            } else {
                LOG.log(Level.INFO, "getBuildWrappers is empty");
            }
        }
    }

    private void publishersParser (Project job, Hashtable<String, InstalledPlugin> installedPluginSet) {
        if (job != null) {
            if (job.getPublishersList() != null && job.getPublishersList().size() > 0) {
                DescribableList<Publisher, Descriptor<Publisher>> publisherList = job.getPublishersList();
                Iterator<Publisher> publisherIterator = publisherList.iterator();
                while (publisherIterator.hasNext()) {
                    Publisher publisher = publisherIterator.next();
                    LOG.log(Level.INFO, "getPublishersList " + addJob(job, publisher.getClass(), installedPluginSet));
                }
            } else {
                LOG.log(Level.INFO, "getPublishersList is empty");
            }
        }
    }

    private void scmParser (Project job, Hashtable<String, InstalledPlugin> installedPluginSet) {
        if (job != null) {
            if (job.getScm() != null) {
                LOG.log(Level.INFO, "getScm " + addJob(job, job.getScm().getClass(), installedPluginSet));
            } else {
                LOG.log(Level.INFO, "getScm is empty");
            }
        }
    }

    private void propertiesParser (Project job, Hashtable<String, InstalledPlugin> installedPluginSet) {
        if (job != null) {
            // TODO: Query Properties
            if (job.getProperties() !=null && job.getProperties().size() > 0){
                LOG.log(Level.INFO, "getProperties is " + job.getProperties().size());
                for (int j = 0; j < job.getProperties().size(); j++) {
                    LOG.log(Level.INFO, "getProperties " +job.getProperties().get(j));//+ /+ addJob(job, job.getProperties().get(j).getClass(), installedPluginSet));
                }
            }else {
                LOG.log(Level.INFO, "getProperties is empty");
            }
        }
    }

    private void queryJob(Project job, Hashtable<String, InstalledPlugin> installedPluginSet) {
        if (job != null) {
            buildersParser(job, installedPluginSet);
            buildWrappersParser(job, installedPluginSet);
            publishersParser(job, installedPluginSet);
            scmParser(job, installedPluginSet);
            propertiesParser(job, installedPluginSet);
        } else {
            LOG.log(Level.INFO, "JOB is null");
        }
    }

    private Hashtable<String, InstalledPlugin> generateInstalledPluginSet (List<PluginWrapper> pluginList, int numberOfJobs) {
        if (pluginList != null) {
            Hashtable<String, InstalledPlugin> installedPluginSet = new Hashtable<String, InstalledPlugin>();

            for (PluginWrapper plugin : pluginList) {
                installedPluginSet.put(plugin.getShortName(),
                        new InstalledPlugin(plugin.getShortName(), plugin.getUrl(),
                                plugin.getVersion(), true, numberOfJobs));
            }
            return installedPluginSet;
        } else {
            return null;
        }
    }

    public void doQuery(final StaplerRequest request, final StaplerResponse response) throws ServletException, IOException {
        installedPluginSet.clear();
        response.forwardToPreviousPage(request);
        levelType = request.getParameter("levelType");
        LOG.log(Level.INFO, "doQuery " + levelType);

        // Global Statistics
        numberOfInstalledPlugins = Hudson.getInstance().getPluginManager().getPlugins().size();
        numberOfJobs = Hudson.getInstance().getItems().size();

        // Query Plugins
        installedPluginSet = generateInstalledPluginSet(Hudson.getInstance().getPluginManager().getPlugins(), numberOfJobs);

        // Query Projects
        for (Project job : Hudson.getInstance().getAllItems(Project.class)) {
            LOG.log(Level.INFO, "queryJob " + job);
            queryJob(job, installedPluginSet);
        }

        // TODO: <maven2-moduleset>
        // TODO: <matrix-project>

    }

    public String getDisplayName() {
        return Messages.DisplayName();
    }

    public String getIconFileName() {
        return Messages.IconFileName();
    }

    public Hashtable<String, InstalledPlugin> getInstalledPluginSet() {
        return installedPluginSet;
    }

    public String getUrlName() {
        return Messages.UrlName();
    }

    public boolean isPluginsAvailable() {
        return installedPluginSet.size() > 0;
    }

    public int getNumberOfJobs() {
        return numberOfJobs;
    }

    public int getNumberOfInstalledPlugins() {
        return numberOfInstalledPlugins;
    }

    public boolean isJobsAvailable() {
        return levelType.equals("jobs");
    }

    public String getLevelType() {
        return levelType;
    }

}
