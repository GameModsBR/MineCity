package br.com.gamemods.minecity.bukkit.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class BukkitCommandSender<S extends CommandSender> implements br.com.gamemods.minecity.api.command.CommandSender
{
    public final MineCityBukkit plugin;
    public final S sender;

    public BukkitCommandSender(MineCityBukkit plugin, S sender)
    {
        this.plugin = plugin;
        this.sender = sender;
    }

    @Override
    public EntityPos getPosition()
    {
        return null;
    }

    @Override
    public boolean isPlayer()
    {
        return false;
    }

    @Override
    public PlayerID getPlayerId()
    {
        return null;
    }

    @Override
    public Direction getCardinalDirection()
    {
        return null;
    }

    @NotNull
    @Override
    public Server getServer()
    {
        return plugin;
    }

    @Override
    public void send(Message[] messages)
    {
        String[] strings = new String[messages.length];
        for(int i = 0; i < messages.length; i++)
            strings[i] = plugin.mineCity.messageTransformer.toSimpleText(messages[i]);
        sender.sendMessage(strings);
    }

    @Override
    public void send(Message message)
    {
        sender.sendMessage(plugin.mineCity.messageTransformer.toSimpleText(message));
    }
}
