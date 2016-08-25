package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.IPlayerList;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.ServerConfigurationManagerTransformer;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.Teleporter;

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

    @Override
    default void transferToDimension(IEntityPlayerMP player, int dimension, Teleporter teleporter)
    {
        getForgePlayerList().transferPlayerToDimension((EntityPlayerMP) player, dimension, teleporter);
    }

    @Override
    default boolean isOp(GameProfile profile)
    {
        return getForgePlayerList().func_152596_g(profile);
    }
}
