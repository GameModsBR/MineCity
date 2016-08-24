package br.com.gamemods.minecity.forge.mc_1_10_2.command;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.forge.base.command.ForgeCommandSender;
import br.com.gamemods.minecity.forge.mc_1_10_2.MineCityFrost;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.ITextComponent;

public class FrostCommandSender<S extends ICommandSender> extends ForgeCommandSender<S, MineCityFrost>
{
    public FrostCommandSender(MineCityFrost mod, S sender)
    {
        super(mod, sender);
    }

    @Override
    public void send(Message[] messages)
    {
        sender.addChatMessage(ITextComponent.Serializer.jsonToComponent(mod.transformer.toJson(
                Message.list(messages, Message.LINE_BREAK)
        )));
    }

    @Override
    public void send(Message message)
    {
        sender.addChatMessage(ITextComponent.Serializer.jsonToComponent(mod.transformer.toJson(message)));
    }
}
