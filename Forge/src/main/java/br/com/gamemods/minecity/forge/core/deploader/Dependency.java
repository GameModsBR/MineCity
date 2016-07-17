package br.com.gamemods.minecity.forge.core.deploader;

import cpw.mods.fml.common.versioning.ComparableVersion;

public class Dependency
{
    private String url;
    private VersionedFile file;
    private boolean coreLib;

    public Dependency(String url, VersionedFile file, boolean coreLib)
    {
        this.url = url;
        this.file = file;
        this.coreLib = coreLib;
    }

    public String getUrl()
    {
        return url;
    }

    public VersionedFile getFile()
    {
        return file;
    }

    public ComparableVersion getVersion()
    {
        return file.getVersion();
    }

    public String getName()
    {
        return file.getName();
    }

    public boolean isCoreLib()
    {
        return coreLib;
    }

    public void setCoreLib(boolean isCoreLib)
    {
        this.coreLib = isCoreLib;
    }
}