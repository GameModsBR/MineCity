package br.com.gamemods.minecity.bukkit;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.command.MessageTransformer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.stream.Stream;

public class BukkitTransformer extends MessageTransformer
{
    public boolean useTellRaw = true;

    public void send(Player player, Message message)
    {
        if(useTellRaw)
        {
            Server server = player.getServer();
            server.dispatchCommand(server.getConsoleSender(), "tellraw "+player.getName()+" "+toJson(message));
        }
        else
        {
            player.sendMessage(toLegacy(message));
        }
    }

    public void send(Player player, Message[] messages)
    {
        if(useTellRaw)
            for(Message message: messages)
                send(player, message);
        else
            player.sendMessage(Stream.of(messages).map(this::toLegacy).toArray(String[]::new));
    }
}
