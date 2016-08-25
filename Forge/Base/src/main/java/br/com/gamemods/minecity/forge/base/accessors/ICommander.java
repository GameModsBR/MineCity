package br.com.gamemods.minecity.forge.base.accessors;

import net.minecraft.command.ICommandSender;

public interface ICommander
{
    default ICommandSender getCommandSender()
    {
        return (ICommandSender) this;
    }

    default String getName()
    {
        return getCommandSender().getName();
    }
}
