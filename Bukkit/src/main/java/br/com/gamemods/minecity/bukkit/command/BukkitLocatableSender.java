package br.com.gamemods.minecity.bukkit.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

public class BukkitLocatableSender implements br.com.gamemods.minecity.api.command.CommandSender
{
    public final MineCityBukkit plugin;
    public final CommandSender sender;

    public BukkitLocatableSender(MineCityBukkit plugin, CommandSender sender)
    {
        this.plugin = plugin;
        this.sender = sender;
    }

    @Override
    public BlockPos getPosition()
    {
        if(sender instanceof BlockCommandSender)
            return plugin.blockPos(((BlockCommandSender) sender).getBlock().getLocation());
        if(sender instanceof Entity)
            return plugin.blockPos(((Entity) sender).getLocation());
        throw new UnsupportedOperationException("Sender: "+sender);
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
    public void send(Message message)
    {
        sender.sendMessage(message.toString());
    }
}
