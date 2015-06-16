package org.jenkins.ci.plugins.pluginstats.model;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Installed Plugin class.
 * @author Victor Martinez
 */
public final class InstalledPlugin implements Comparable<InstalledPlugin> {
    private final SortedSet<Job> jobSet = new TreeSet<Job>();
    private String name;
    private String url;
    private String version;
    private String absolutePath;
    private boolean globalConfigBased;
    // I don't like it ... but
    private int numberOfTotalJobs;

    public InstalledPlugin(final String name, final String url, final String version,
                           final boolean globalConfigBased, final int numberOfTotalJobs) {
        super();
        this.name = name;
        this.url = url;
        this.version = version;
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

    public boolean isGlobalConfigBased() {
        return globalConfigBased;
    }

    public void setGlobalConfigBased(final boolean globalConfigBased) {
        this.globalConfigBased = globalConfigBased;
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

    public float getPercentage() {
        if (numberOfTotalJobs > 0 && jobSet != null) {
            return ((jobSet.size() * 100 / numberOfTotalJobs));
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
