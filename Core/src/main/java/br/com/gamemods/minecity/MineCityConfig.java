package br.com.gamemods.minecity;

import br.com.gamemods.minecity.api.permission.SimpleFlagHolder;
import br.com.gamemods.minecity.economy.Tax;

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
    public String permission = "none";
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
        public Tax cityTax = new Tax(100, 0.03);
        public Tax cityTaxApplied = new Tax(-1, 0.05);
        public Tax plotTaxApplied = new Tax(0, 0);
        public double cityChangeSpawn = 50;
        public double plotChangeSpawn = 50;
        public double goToCity = 5;
        public double goToPlot = 15;
    }

    public static class Limits
    {
        public int cities = -1;
        public int claims = -1;
        public int islands = -1;
    }
}
