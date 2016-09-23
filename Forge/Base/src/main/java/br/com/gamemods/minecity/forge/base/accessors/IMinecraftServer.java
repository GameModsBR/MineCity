package br.com.gamemods.minecity.forge.base.accessors;

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
}
