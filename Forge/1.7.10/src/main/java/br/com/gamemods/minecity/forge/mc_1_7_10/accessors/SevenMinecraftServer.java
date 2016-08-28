package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IMinecraftServer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.server.MinecraftServer;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenMinecraftServer extends IMinecraftServer
{
    @Override
    default SevenPlayerList getIPlayerList()
    {
        return (SevenPlayerList) ((MinecraftServer) this).getConfigurationManager();
    }
}
