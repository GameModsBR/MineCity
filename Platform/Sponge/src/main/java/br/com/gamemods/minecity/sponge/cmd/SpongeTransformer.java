package br.com.gamemods.minecity.sponge.cmd;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.command.MessageTransformer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class SpongeTransformer extends MessageTransformer
{
    public Text toText(Message message)
    {
        return TextSerializers.JSON.deserialize(toJson(message));
    }
}
