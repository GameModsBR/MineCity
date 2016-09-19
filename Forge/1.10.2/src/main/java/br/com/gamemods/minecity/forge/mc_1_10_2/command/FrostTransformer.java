package br.com.gamemods.minecity.forge.mc_1_10_2.command;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.forge.base.accessors.ICommander;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.command.ForgeTransformer;
import net.minecraft.util.text.ITextComponent;

public class FrostTransformer extends ForgeTransformer
{
    @Override
    public void send(Message message, ICommander commander)
    {
        if(commander instanceof IEntityPlayerMP && !((IEntityPlayerMP) commander).hasNetHandler())
            return;

        commander.getForgeSender().addChatMessage(ITextComponent.Serializer.jsonToComponent(
                toJson(message)
        ));
    }

    @Override
    public void send(Message[] message, ICommander commander)
    {
        if(commander instanceof IEntityPlayerMP && !((IEntityPlayerMP) commander).hasNetHandler())
            return;
        
        commander.getForgeSender().addChatMessage(ITextComponent.Serializer.jsonToComponent(
                toJson(Message.list(message, Message.LINE_BREAK))
        ));
    }
}
