package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.IPlayerList;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.ServerConfigurationManagerTransformer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.ServerConfigurationManager;

import java.util.List;

@Referenced(at = ServerConfigurationManagerTransformer.class)
public interface SevenPlayerList extends IPlayerList
{
    default ServerConfigurationManager getForgePlayerList()
    {
        return (ServerConfigurationManager) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    default List<EntityPlayerMP> getPlayerEntities()
    {
        return getForgePlayerList().playerEntityList;
    }

    @Override
    @SuppressWarnings("unchecked")
    default List<IEntityPlayerMP> getIPlayers()
    {
        return getForgePlayerList().playerEntityList;
    }
}
