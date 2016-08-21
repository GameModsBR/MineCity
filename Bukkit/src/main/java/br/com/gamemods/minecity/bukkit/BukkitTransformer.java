package br.com.gamemods.minecity.bukkit;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.command.MessageTransformer;
import org.bukkit.entity.Player;

import java.util.stream.Stream;

public class BukkitTransformer extends MessageTransformer
{
    public void send(Player player, Message message)
    {
        player.sendMessage(toLegacy(message));
    }

    public void send(Player player, Message[] messages)
    {
        player.sendMessage(Stream.of(messages).map(this::toLegacy).toArray(String[]::new));
    }
}
