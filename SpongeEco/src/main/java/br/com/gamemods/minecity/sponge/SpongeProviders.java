package br.com.gamemods.minecity.sponge;

import br.com.gamemods.minecity.economy.EconomyProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

public class SpongeProviders
{
    public static final EconomyProvider ECONOMY = mineCity -> Sponge.getPluginManager().getPlugin("minecityecosponge")
            .flatMap(PluginContainer::getInstance).map(SpongeEconomy.class::cast)
            .orElseThrow(() -> new IllegalStateException("MineCity Eco Sponge is not loaded!"));

    private SpongeProviders(){}
}
