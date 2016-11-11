package br.com.gamemods.minecity;

import br.com.gamemods.minecity.api.permission.SimpleFlagHolder;

import java.util.Locale;

public final class MineCityConfig implements Cloneable
{
    public String dbUrl = "jdbc:mysql://localhost/minecity?autoReconnect=true";
    public String dbUser;
    public byte[] dbPass;
    public Locale locale;
    public SimpleFlagHolder defaultNatureFlags = new SimpleFlagHolder();
    public SimpleFlagHolder defaultCityFlags = new SimpleFlagHolder();
    public SimpleFlagHolder defaultPlotFlags = new SimpleFlagHolder();
    public SimpleFlagHolder defaultReserveFlags = new SimpleFlagHolder();
    public boolean defaultNatureDisableCities;
    public boolean useTitle = true;
    public String economy = "none";
    public Costs costs = new Costs();
    public Limits limits = new Limits();

    @Override
    public MineCityConfig clone()
    {
        try
        {
            MineCityConfig clone = (MineCityConfig) super.clone();
            clone.dbPass = dbPass.clone();
            return clone;
        }
        catch(CloneNotSupportedException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static class Costs
    {
        public double cityCreation = 1000;
        public double islandCreation = 500;
        public double claim = 25;
    }

    public static class Limits
    {
        public int cities = -1;
        public int claims = -1;
        public int islands = -1;
    }
}
