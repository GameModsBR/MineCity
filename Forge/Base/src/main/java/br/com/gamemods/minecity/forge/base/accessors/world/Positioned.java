package br.com.gamemods.minecity.forge.base.accessors.world;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface Positioned
{
    IWorldServer getIWorld();

    @NotNull
    default PlayerID createPlayerId(UUID uuid)
    {
        IWorldServer world = getIWorld();
        IEntityPlayerMP player = world.getPlayerByUUID(uuid);
        if(player != null)
            return player.identity();

        return world.getServer().getPlayerId(uuid);
    }
}
