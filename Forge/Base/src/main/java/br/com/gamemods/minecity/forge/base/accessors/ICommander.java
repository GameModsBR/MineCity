package br.com.gamemods.minecity.forge.base.accessors;

import net.minecraft.command.ICommandSender;

public interface ICommander
{
    default ICommandSender getForgeSender()
    {
        return (ICommandSender) this;
    }

    default String getName()
    {
        return getForgeSender().getName();
    }
}
