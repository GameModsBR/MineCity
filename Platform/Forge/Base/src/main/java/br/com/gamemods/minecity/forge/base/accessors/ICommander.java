package br.com.gamemods.minecity.forge.base.accessors;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public interface ICommander
{
    default ICommandSender getForgeSender()
    {
        return (ICommandSender) this;
    }

    default String getCommandSenderName()
    {
        return getForgeSender().getName();
    }

    default MinecraftServer getMinecraftServer()
    {
        return getForgeSender().getServer();
    }
}
