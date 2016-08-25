package br.com.gamemods.minecity.forge.base.accessors;

import net.minecraft.server.MinecraftServer;

public interface IMinecraftServer
{
    default MinecraftServer getServer()
    {
        return (MinecraftServer) this;
    }

    default IPlayerList getIPlayerList()
    {
        return (IPlayerList) ((MinecraftServer) this).getPlayerList();
    }
}
