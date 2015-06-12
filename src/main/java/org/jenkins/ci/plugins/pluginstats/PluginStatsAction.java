package org.jenkins.ci.plugins.pluginstats;

import hudson.Extension;
import hudson.PluginWrapper;
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
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public final class PluginStatsAction implements RootAction {

    private static final Logger LOG = Logger.getLogger(PluginStatsAction.class.getName());
    private final Hashtable<String, InstalledPlugin> installedPluginSet = new Hashtable<String, InstalledPlugin>();

    private int numberOfJobs = 0;
    private int numberOfInstalledPlugins = 0;

    private String levelType;

    public void doClear(final StaplerRequest request, final StaplerResponse response)
            throws ServletException, IOException {
        LOG.log(Level.INFO, "doClear");
        installedPluginSet.clear();
        response.sendRedirect(Hudson.getInstance().getRootUrl());
    }

    private String findPathJar(Class<?> context) throws IllegalStateException {
        URL location = context.getResource('/' + context.getName().replace(".", "/") + ".class");
        String jarPath = location.getPath();
        if (jarPath != null && !jarPath.equals("")) {
            try {
                return jarPath.substring("file:".length(), jarPath.lastIndexOf("!")).replaceAll("/WEB-INF.*", "");
            } catch (java.lang.StringIndexOutOfBoundsException e) {
                // it is based on class
                LOG.log(Level.WARNING, "Cannot be converted '" + context.getName() + "' (" + jarPath + ")");
                return jarPath.substring("file:".length(), jarPath.lastIndexOf("/WEB-INF"));
            }
        } else {
            return "";
        }
    }

    private String addJob(Project job, Class<?> context, Hashtable<String, InstalledPlugin> installedPluginSet) {
        File plugin = new File(findPathJar(context));
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

    private void queryJob(Project job, Hashtable<String, InstalledPlugin> installedPluginSet) {
        if (job != null) {
            if (job.getBuilders() != null) {
                for (int i = 0; i < job.getBuilders().size(); i++) {
                    LOG.log(Level.INFO, "getBuilders " + addJob(job, job.getBuilders().get(i).getClass(), installedPluginSet));
                }
            } else {
                LOG.log(Level.INFO, "getBuilders are null");
            }
            if (job.getBuildWrappersList() != null) {
                DescribableList<BuildWrapper, Descriptor<BuildWrapper>> wrappersList = job.getBuildWrappersList();
                Iterator<BuildWrapper> buildWrapperIterator = wrappersList.iterator();
                while (buildWrapperIterator.hasNext()) {
                    BuildWrapper buildWrapper = buildWrapperIterator.next();
                    LOG.log(Level.INFO, "getBuildWrappers " + addJob(job, buildWrapper.getClass(), installedPluginSet));
                }

            } else {
                LOG.log(Level.INFO, "getBuildWrappers are null");
            }
            if (job.getPublishersList() != null) {
                DescribableList<Publisher, Descriptor<Publisher>> publisherList = job.getPublishersList();
                Iterator<Publisher> publisherIterator = publisherList.iterator();
                while (publisherIterator.hasNext()) {
                    Publisher publisher = publisherIterator.next();
                    LOG.log(Level.INFO, "getPublishersList " + addJob(job, publisher.getClass(), installedPluginSet));
                }
            } else {
                LOG.log(Level.INFO, "getPublishersList are null");
            }
            if (job.getScm() != null) {
                LOG.log(Level.INFO, "getScm " + addJob(job, job.getScm().getClass(), installedPluginSet));
            } else {
                LOG.log(Level.INFO, "getScm are null");
            }
        } else {
            LOG.log(Level.INFO, "It is null");
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
        for (PluginWrapper temp : Hudson.getInstance().getPluginManager().getPlugins()) {
            String folderName = temp.getShortName();
            installedPluginSet.put(temp.getShortName(),
                    new InstalledPlugin(temp.getShortName(),
                            temp.getUrl(),
                            temp.getVersion(),
                            folderName,
                            true,
                            numberOfJobs));
        }

        // Query Jobs
        for (Project job : Hudson.getInstance().getAllItems(Project.class)) {
            queryJob(job, installedPluginSet);
            /**  InstalledPlugin plugin = new InstalledPlugin(job.getName(), job.getUrl(), "",  "", false);
             plugin.addJob(new org.jenkins.ci.plugins.pluginstats.model.Job(job.getName(), job.getUrl()));
             installedPluginSet.put(job.getName(), plugin);*/
        }

        // DEBUG
        for (String key : installedPluginSet.keySet()) {
            LOG.log(Level.INFO, installedPluginSet.get(key).toString());
        }
    }

    public String getDisplayName() {
        return Messages.DisplayName();
    }

    public String getIconFileName() {
        return "/images/32x32/plugin.png";
    }

    public Hashtable<String, InstalledPlugin> getInstalledPluginSet() {
        return installedPluginSet;
    }

    public String getUrlName() {
        return "/plugin-stats";
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

    public boolean isBuildsAvailable() {
        return levelType.equals("builds");
    }

    public String getLevelType() {
        return levelType;
    }

}
