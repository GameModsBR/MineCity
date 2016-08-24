package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.bukkit.command.BukkitPlayer;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Nature;
import br.com.gamemods.minecity.structure.Plot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LeashedEntityMonitor extends BukkitRunnable implements BukkitMovementListener
{
    private final BukkitMovementMonitor mov;
    private final BukkitPlayer player;
    private final MineCityBukkit plugin;
    private final Location location, vehicleLocation;
    public LeashedEntityMonitor(MineCityBukkit bukkit, BukkitPlayer player, LivingEntity entity)
    {
        this.player = player;
        this.plugin = bukkit;
        location = entity.getLocation();
        vehicleLocation = location.clone();
        mov = new BukkitMovementMonitor(bukkit, entity, this);
    }

    private void teleport(BlockPos pos)
    {
        mov.entity.teleport(plugin.reuseLocation(location, pos));
    }

    @Override
    public void run()
    {
        LivingEntity entity = (LivingEntity) mov.entity;
        if(!entity.isValid() || !entity.isLeashed() || !entity.getLeashHolder().getUniqueId().equals(player.getUniqueId()))
        {
            cancel();
            return;
        }

        Optional<Message> message = mov.checkPosition(entity.getLocation(location));
        if(message.isPresent())
        {
            if(mov.messageWait > 0 && mov.messageWait % 5 == 0)
                entity.damage(1);

            if(mov.messageWait == 0)
            {
                player.send(new Message("","<msg><red>${msg}</red></msg>", new Object[]{"msg", message.get()}));
                mov.messageWait = (byte) 20*3;
            }

            Entity vehicle = entity.getVehicle();
            if(vehicle == null)
                teleport(new BlockPos(mov.lastClaim.chunk.world, mov.lastX, mov.lastY, mov.lastZ));
            else
            {
                Location vLoc = vehicle.getLocation(vehicleLocation);
                Optional<World> world = plugin.world(mov.lastClaim.chunk.world);
                if(!world.isPresent())
                    teleport(new BlockPos(mov.lastClaim.chunk.world, mov.lastX, mov.lastY, mov.lastZ));
                else
                if(!vehicle.teleport(new Location(world.get(), mov.lastX+0.5, mov.lastY+0.5, mov.lastZ+0.5, vLoc.getYaw(), vLoc.getPitch())))
                {
                    Entity passenger = vehicle.getPassenger();
                    vehicle.eject();
                    teleport(new BlockPos(mov.lastClaim.chunk.world, mov.lastX, mov.lastY, mov.lastZ));
                    if(vehicle.teleport(new Location(world.get(), mov.lastX+0.5, mov.lastY+0.5, mov.lastZ+0.5, vLoc.getYaw(), vLoc.getPitch())))
                        player.getServer().callSyncMethod(()-> vehicle.setPassenger(passenger));
                }
            }
        }
    }

    @Override
    public Optional<Message> onCityChange(@NotNull City city, @Nullable Plot plot)
    {
        return player.onCityChange(city, plot);
    }

    @Override
    public Optional<Message> onPlotEnter(@NotNull Plot plot)
    {
        return player.onPlotEnter(plot);
    }

    @Override
    public Optional<Message> onPlotLeave(@NotNull City city)
    {
        return player.onPlotLeave(city);
    }

    @Override
    public Optional<Message> onCityLeave(@NotNull Nature nature)
    {
        return player.onCityLeave(nature);
    }

    @Override
    public Optional<Message> onNatureChange(@NotNull Nature nature)
    {
        return player.onNatureChange(nature);
    }
}
