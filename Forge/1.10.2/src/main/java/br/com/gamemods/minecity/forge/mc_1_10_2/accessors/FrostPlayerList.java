package br.com.gamemods.minecity.forge.mc_1_10_2.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.IPlayerList;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostPlayerListTransformer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;

import java.util.List;

@Referenced(at = FrostPlayerListTransformer.class)
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
}
