package br.com.gamemods.minecity.forge.command;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.command.MessageTransformer;
import net.minecraft.util.IChatComponent;
import org.w3c.dom.Element;

import java.util.Optional;

public class ForgeTransformer extends MessageTransformer
{
    private IChatComponent toForgeComponent(Element tag, Object[] args)
    {
        throw new UnsupportedOperationException();
    }

    private IChatComponent toForgeComponent(String message, Object[] args)
    {
        if(message.startsWith("<msg>"))
            return toForgeComponent(compile(message), args);

        throw new UnsupportedOperationException();
    }

    public IChatComponent toForgeComponent(Message message)
    {
        Optional<Element> element = super.getElement(message.getId());
        if(element.isPresent())
            return toForgeComponent(element.get(), message.getArgs());
        else
            return toForgeComponent(message.getFallback(), message.getArgs());
    }
}
