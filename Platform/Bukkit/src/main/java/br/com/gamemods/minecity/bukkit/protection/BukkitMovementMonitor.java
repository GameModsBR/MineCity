package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.protection.MovementMonitor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Optional;

public class BukkitMovementMonitor extends MovementMonitor<Entity, MineCityBukkit>
{
    public BukkitMovementMonitor(MineCityBukkit plugin, Entity entity, BukkitMovementListener listener)
    {
        super(plugin, entity, plugin.blockPos(entity.getLocation()), listener);
    }

    public Optional<Message> checkPosition(Location location)
    {
        return super.checkPosition(server.world(location.getWorld()), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Location lastLocation()
    {
        BlockPos pos = lastPosition();
        return server.location(pos).orElseGet(()-> new Location(entity.getWorld(), pos.x+0.5, pos.y+0.5, pos.z+0.5));
    }

}
