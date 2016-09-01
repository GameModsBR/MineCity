package br.com.gamemods.minecity.forge.mc_1_10_2.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IPlayerList;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostInterfaceTransformer;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.Teleporter;

import java.util.List;

@Referenced(at = FrostInterfaceTransformer.class)
public interface FrostPlayerList extends IPlayerList
{
    default PlayerList getForgePlayerList()
    {
        return (PlayerList) this;
    }

    @Override
    default List<EntityPlayerMP> getPlayerEntities()
    {
        return ((PlayerList) this).getPlayerList();
    }

    @Override
    @SuppressWarnings("unchecked")
    default List<IEntityPlayerMP> getIPlayers()
    {
        return (List) ((PlayerList) this).getPlayerList();
    }

    @Override
    default void transferToDimension(IEntityPlayerMP player, int dimension, Teleporter teleporter)
    {
        ((PlayerList) this).transferPlayerToDimension((EntityPlayerMP) player, dimension, teleporter);
    }

    @Override
    default boolean isOp(GameProfile profile)
    {
        return ((PlayerList) this).canSendCommands(profile);
    }
}
