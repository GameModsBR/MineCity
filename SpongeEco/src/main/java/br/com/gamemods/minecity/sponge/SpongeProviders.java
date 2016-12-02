package br.com.gamemods.minecity.sponge;

import br.com.gamemods.minecity.economy.EconomyProvider;
import br.com.gamemods.minecity.economy.EconomyProxy;
import br.com.gamemods.minecity.permission.PermissionProvider;
import br.com.gamemods.minecity.permission.PermissionProxy;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.function.Supplier;

public class SpongeProviders
{
    public static final Supplier<?> PLUGIN = () -> Sponge.getPluginManager().getPlugin("minecitysponge")
            .flatMap(PluginContainer::getInstance).map(SpongeProxy.class::cast)
            .orElseThrow(() -> new IllegalStateException("MineCity Sponge is not loaded!"));

    public static final EconomyProvider ECONOMY = mineCity -> (EconomyProxy) PLUGIN.get();
    public static final PermissionProvider PERMISSION = mineCity -> (PermissionProxy) PLUGIN.get();

    private SpongeProviders(){}
}
