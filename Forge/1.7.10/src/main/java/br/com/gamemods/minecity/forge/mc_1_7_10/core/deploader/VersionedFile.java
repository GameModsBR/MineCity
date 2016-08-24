package br.com.gamemods.minecity.forge.mc_1_7_10.core.deploader;

import cpw.mods.fml.common.versioning.ComparableVersion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionedFile
{
    private final Pattern pattern;
    private final String filename;
    private final ComparableVersion version;
    private final String name;

    public VersionedFile(String filename, Pattern pattern)
    {
        this.pattern = pattern;
        this.filename = filename;
        Matcher m = pattern.matcher(filename);
        if(m.matches())
        {
            name = m.group(1);
            version = new ComparableVersion(m.group(2));
        }
        else
        {
            name = null;
            version = null;
        }
    }

    public Pattern getPattern()
    {
        return pattern;
    }

    public String getFilename()
    {
        return filename;
    }

    public String getName()
    {
        return name;
    }

    public ComparableVersion getVersion()
    {
        return version;
    }

    public boolean matches()
    {
        return name != null;
    }
}