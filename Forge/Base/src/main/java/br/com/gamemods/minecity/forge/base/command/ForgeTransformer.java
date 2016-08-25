package br.com.gamemods.minecity.forge.base.command;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.command.MessageTransformer;
import br.com.gamemods.minecity.forge.base.accessors.ICommander;

public abstract class ForgeTransformer extends MessageTransformer
{
    public abstract void send(Message message, ICommander commandSender);
    public abstract void send(Message[] messages, ICommander commandSender);
}
