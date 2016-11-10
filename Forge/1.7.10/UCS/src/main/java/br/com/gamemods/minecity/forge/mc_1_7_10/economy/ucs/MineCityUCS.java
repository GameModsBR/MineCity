package br.com.gamemods.minecity.forge.mc_1_7_10.economy.ucs;

import br.com.gamemods.minecity.economy.EconomyLayer;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = "minecity-ucs", name = "MineCity UCS", version = ModEnv.MOD_VERSION, acceptableRemoteVersions = "*",
    dependencies = "after:universalcoins;after:"+ModEnv.MOD_ID
)
public class MineCityUCS
{
    @Mod.EventHandler
    @Optional.Method(modid = "universalcoins")
    public void onInit(FMLInitializationEvent event)
    {
        EconomyLayer.register("ucs", mineCity ->
        {
            try
            {
                // If it's not loaded it will throw an exception, Loader.isLoaded() will not distinct normal UniversalCoins from UniversalCoinsServer
                UniversalCoinsServerAPI.random.nextBoolean();
                return new UniversalCoinsServerEconomy(ModEnv.blockProtections.mod);
            }
            catch(Exception e)
            {
                throw new UnsupportedOperationException("UniversalCoinsServer is not loaded", e);
            }
        });
    }
}
