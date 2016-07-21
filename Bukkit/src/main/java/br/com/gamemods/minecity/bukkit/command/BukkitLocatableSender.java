package br.com.gamemods.minecity.bukkit.command;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

public class BukkitLocatableSender<S extends CommandSender> extends BukkitCommandSender<S>
{
    public BukkitLocatableSender(MineCityBukkit plugin, S sender)
    {
        super(plugin, sender);
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
    public void send(Message message)
    {
        sender.sendMessage(plugin.mineCity.messageTransformer.toLegacy(message));
    }
}
