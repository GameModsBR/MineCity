package br.com.gamemods.minecity.bukkit.command;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitLocatableSender<S extends CommandSender> extends BukkitCommandSender<S>
{
    public BukkitLocatableSender(MineCityBukkit plugin, S sender)
    {
        super(plugin, sender);
    }

    @Override
    public EntityPos getPosition()
    {
        if(sender instanceof BlockCommandSender)
            return plugin.entityPos(((BlockCommandSender) sender).getBlock().getLocation());
        if(sender instanceof Entity)
            return plugin.entityPos(((Entity) sender).getLocation());
        return null;
    }

    @Nullable
    @Override
    public Message teleport(@NotNull EntityPos pos)
    {
        if(sender instanceof Entity)
        {
            Entity entity = (Entity) sender;
            Location current = entity.getLocation();

            Location location = plugin.location(pos).orElse(null);
            if(location == null)
                return new Message("action.teleport.world-not-found",
                        "The destiny world ${name} was not found or is not loaded",
                        new Object[]{"name",pos.world.name()}
                );

            location.setPitch(current.getPitch());
            location.setYaw(current.getYaw());

            if(entity.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND))
                return null;

            return new Message("action.teleport.cancelled", "The teleport were cancelled");
        }

        return super.teleport(pos);
    }

    @Override
    public Direction getCardinalDirection()
    {
        return getPosition().getCardinalDirection();
    }

    @Override
    public void send(Message[] messages)
    {
        String[] strings = new String[messages.length];
        for(int i = 0; i < messages.length; i++)
            strings[i] = plugin.mineCity.messageTransformer.toLegacy(messages[i]);
        sender.sendMessage(strings);
    }

    @Override
    public void send(Message message)
    {
        sender.sendMessage(plugin.mineCity.messageTransformer.toLegacy(message));
    }
}
