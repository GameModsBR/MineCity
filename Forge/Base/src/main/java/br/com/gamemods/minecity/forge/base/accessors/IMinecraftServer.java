package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.api.PlayerID;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.UsernameCache;

import java.util.Map;
import java.util.UUID;

public interface IMinecraftServer
{
    default MinecraftServer getServer()
    {
        return (MinecraftServer) this;
    }

    default Map<UUID, String> getUsernameCache()
    {
        return UsernameCache.getMap();
    }

    default IPlayerList getIPlayerList()
    {
        return (IPlayerList) ((MinecraftServer) this).getPlayerList();
    }

    default PlayerID getPlayerId(UUID uuid)
    {
        return new PlayerID(uuid, getUsernameCache().getOrDefault(uuid, "???"));
    }
}
