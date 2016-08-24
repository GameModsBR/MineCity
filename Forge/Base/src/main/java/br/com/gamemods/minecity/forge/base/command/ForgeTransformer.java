package br.com.gamemods.minecity.forge.base.command;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.command.MessageTransformer;
import net.minecraft.command.ICommandSender;

public abstract class ForgeTransformer extends MessageTransformer
{
    public abstract void send(Message message, ICommandSender commandSender);
    public abstract void send(Message[] messages, ICommandSender commandSender);
}
