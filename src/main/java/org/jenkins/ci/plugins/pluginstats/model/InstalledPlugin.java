package org.jenkins.ci.plugins.pluginstats.model;

import java.util.SortedSet;
import java.util.TreeSet;

public final class InstalledPlugin implements Comparable<InstalledPlugin> {
    private String name;
    private String url;
    private String version;
    private String absolutePath;
    private String folderName;
    private boolean globalConfigBased;
    private final SortedSet<Job> jobSet = new TreeSet<Job>();

    // I don't like it ... but
    private int numberOfTotalJobs;

    public InstalledPlugin(final String name, final String url, final String version,
                           final String folderName, final boolean globalConfigBased,
                           final int numberOfTotalJobs) {
        super();
        this.name = name;
        this.url = url;
        this.version = version;
        this.folderName = folderName;
        this.globalConfigBased = globalConfigBased;
        this.numberOfTotalJobs = numberOfTotalJobs;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(final String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public void setGlobalConfigBased(final boolean globalConfigBased) {
        this.globalConfigBased = globalConfigBased;
    }

    public boolean isGlobalConfigBased() {
        return globalConfigBased;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(final String folderName) {
        this.folderName = folderName;
    }

    public SortedSet<Job> getJobSet() {
        return jobSet;
    }

    public void addJob(Job job) {
        jobSet.add(job);
    }

    public boolean isJobAvailable() {
        return jobSet.size() > 0;
    }

    public double getPercentage() {
        if (numberOfTotalJobs > 0 && jobSet != null) {
            return ((jobSet.size() / numberOfTotalJobs) * 100);
        } else {
            return 0;
        }
    }

    public int compareTo(final InstalledPlugin other) {
        if (this == other) {
            return 0;
        }
        return name.compareTo(other.getName());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof InstalledPlugin)) {
            return false;
        }

        return name.equals(((InstalledPlugin) obj).getName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder().append("InstalledPlugin: ").append(name)
                .append(", ").append(url).append(", ").append(version).append(", ")
                .append(numberOfTotalJobs).append(", ")
                .append(getPercentage()).append(", ")
                .append(jobSet).toString();
    }
}
