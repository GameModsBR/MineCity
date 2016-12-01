package br.com.gamemods.minecity.sponge;

import br.com.gamemods.minecity.economy.DelayedEconomy;
import br.com.gamemods.minecity.economy.EconomyProvider;

public class SpongeDelayedProviders
{
    public static final EconomyProvider ECONOMY = mineCity -> new DelayedEconomy(mineCity, SpongeProviders.ECONOMY);

    private SpongeDelayedProviders(){}
}
