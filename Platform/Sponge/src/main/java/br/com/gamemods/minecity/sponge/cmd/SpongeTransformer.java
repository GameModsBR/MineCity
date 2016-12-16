package br.com.gamemods.minecity.sponge.cmd;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.command.MessageTransformer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpongeTransformer extends MessageTransformer
{
    public Text toText(Message message)
    {
        return TextSerializers.JSON.deserialize(toJson(message));
    }

    public Text[] toMultilineText(Message message)
    {
        Component component = toComponent(message);
        component.apply(Locale.getDefault(), message.getArgs());
        List<Component> split = new ArrayList<>(1);
        split.add(component);
        component.splitNewLines(split);
        return split.stream().map(this::toJson).map(TextSerializers.JSON::deserialize).toArray(Text[]::new);
    }
}
